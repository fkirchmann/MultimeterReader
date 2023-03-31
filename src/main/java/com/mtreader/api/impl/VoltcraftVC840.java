/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader.api.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.mtreader.SI;
import com.mtreader.api.DataDevice;
import com.mtreader.api.DataSource;
import com.mtreader.api.Measurement;
import com.mtreader.api.PortParameters;

/**
 * Reference:
 * http://www.produktinfo.conrad.com/datenblaetter/100000-124999/123295-da-01-en-RS232_Protocol_VOLTCRAFT_VC840_DMM.pdf
 *
 * @author Felix Kirchmann
 */
public class VoltcraftVC840 extends DataSource
{
	private static final PortParameters	portParameters	= new PortParameters(PortParameters.BAUDRATE_2400,
																PortParameters.DATABITS_8, PortParameters.STOPBITS_1,
																PortParameters.PARITY_NONE, true, true);
	
	@Override
	public PortParameters getPortParameters()
	{
		return portParameters;
	}
	
	@Override
	public String[] getSupportedDevices()
	{
		return new String[] { "Voltcraft VC-840" };
	}
	
	private volatile boolean	run	= true;
	
	@Override
	protected void onStart(final DataDevice device)
	{
		try
		{
			int oldPos = -1, currentPos;
			int read = 0;
			final byte[] buffer = new byte[14];
			while (run)
			{
				do
				{
					read = device.read();
				}
				while (read == 0xFFFFFFFF && run);
				
				currentPos = getPos(read);
				if (oldPos == -1)
				{
					if (currentPos == 0)
					{
						oldPos = currentPos;
						buffer[0] = (byte) (read & 0x0F);
					}
				}
				else
				{
					if (oldPos + 1 == currentPos && currentPos >= 0 && currentPos <= 13)
					{
						oldPos = currentPos;
						buffer[currentPos] = (byte) (read & 0x0F);
						
						if (currentPos == 13)
						{
							decode(buffer);
							oldPos = -1;
						}
					}
					else
					{
						oldPos = -1;
					}
				}
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private byte getPos(final int fromByte)
	{
		return (byte) ((fromByte >> 4) - 1);
	}
	
	private void decode(final byte[] read)
	{
		// Ensure that only one SI prefix is set
		if (!isPowerOfTwo((read[9] & 0b1110) | ((read[10] & 0b1010) << 3))) { return; }
		
		// Ensure that only one measurement unit is set
		if (!isPowerOfTwo((read[11] & 0b1100) | ((read[12] & 0b1110) << 3))) { return; }
		
		// Decode displayed digits
		final byte[] chars = new byte[4];
		for (int i = 0; i < chars.length; i++)
		{
			final byte chr = decodeChar(new byte[] { read[(i * 2) + 1], read[(i * 2) + 2] });
			if (chr == -1) { return; }
			chars[i] = chr;
		}
		
		// Find the decimal point
		int dotPosition = chars.length;
		for (int i = 1; i <= 3; i++)
		{
			if (getBitState(read[(i * 2) + 1], 3))
			{
				if (dotPosition == chars.length)
				{
					dotPosition = i;
					break;
				}
				else
				{
					return;
				}
			}
		}
		final boolean negative = getBitState(read[1], 3);
		
		SI.Prefix prefix = SI.Prefix.none;
		if (getBitState(read[9], 1))
		{
			prefix = SI.Prefix.kilo;
		}
		else if (getBitState(read[9], 2))
		{
			prefix = SI.Prefix.nano;
		}
		else if (getBitState(read[9], 3))
		{
			prefix = SI.Prefix.micro;
		}
		else if (getBitState(read[10], 1))
		{
			prefix = SI.Prefix.mega;
		}
		else if (getBitState(read[10], 3))
		{
			prefix = SI.Prefix.milli;
		}
		
		String unit = "";
		if (getBitState(read[11], 2))
		{
			unit = "\u03a9"; // Ohm, uppercase Greek Omega
		}
		else if (getBitState(read[11], 3))
		{
			unit = "F"; // Farad
		}
		else if (getBitState(read[12], 1))
		{
			unit = "Hz"; // Hertz
		}
		else if (getBitState(read[12], 2))
		{
			unit = "V"; // Volt
		}
		else if (getBitState(read[12], 3))
		{
			unit = "A"; // Ampère
		}
		else if (getBitState(read[13], 0)) // For some reason, this pin is undocumented
		{
			unit = "\u00B0C"; // Degrees Celsius
		}
		
		// Turn the read data into a BigDecimal...
		final StringBuilder sb = new StringBuilder();
		if (negative)
		{
			sb.append('-');
		}
		for (int i = 0; i < chars.length; i++)
		{
			if (i == dotPosition)
			{
				sb.append('.');
			}
			sb.append(chars[i]);
		}
		final BigDecimal data = new BigDecimal(sb.toString());
		data.setScale(chars.length - dotPosition, RoundingMode.HALF_UP);
		
		// ... and pass it to the application
		
		this.updateData(new Measurement(data, unit, prefix));
	}
	
	private byte decodeChar(final byte[] read)
	{
		// for the meaning of the letters, see tech. documentation link above
		final boolean a = getBitState(read[0], 0);
		final boolean b = getBitState(read[1], 0);
		final boolean c = getBitState(read[1], 2);
		final boolean d = getBitState(read[1], 3);
		final boolean e = getBitState(read[0], 2);
		final boolean f = getBitState(read[0], 1);
		final boolean g = getBitState(read[1], 1);
		
		if (b && c)
		{
			if (a && d && e && f)
			{
				if (g)
				{
					return 8;
				}
				else
				{
					return 0;
				}
			}
			else if (a)
			{
				if (g)
				{
					if (f && d)
					{
						return 9;
					}
					else if (d) { return 3; }
				}
				else
				{
					return 7;
				}
			}
			else if (f && g)
			{
				return 4;
			}
			else
			{
				return 1;
			}
		}
		else if (a && g && d)
		{
			if (f && c)
			{
				if (e)
				{
					return 6;
				}
				else
				{
					return 5;
				}
			}
			else if (b && e) { return 2; }
		}
		return -1;
	}
	
	private boolean isPowerOfTwo(final int number)
	{
		return (number & (number - 1)) == 0;
	}
	
	private boolean getBitState(final byte fromByte, final int pos)
	{
		return (fromByte & (1 << pos)) > 0;
	}
	
	@Override
	protected void onStop()
	{
		run = false;
	}
}
