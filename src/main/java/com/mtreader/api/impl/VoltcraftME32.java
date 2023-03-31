/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader.api.impl;

import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;

import com.mtreader.SI;
import com.mtreader.api.DataDevice;
import com.mtreader.api.DataSource;
import com.mtreader.api.Measurement;
import com.mtreader.api.PortParameters;

/**
 *
 * @author Felix Kirchmann
 */
public class VoltcraftME32 extends DataSource
{
	private static final PortParameters	portParameters	= new PortParameters(PortParameters.BAUDRATE_600,
																PortParameters.DATABITS_7, PortParameters.STOPBITS_2,
																PortParameters.PARITY_NONE, true, true);
	
	private static final int			pollInterval	= 300;												// ms
	private static final Charset		packetCharset	= Charset.forName("UTF-8");
	
	@Override
	public PortParameters getPortParameters()
	{
		return portParameters;
	}
	
	@Override
	public String[] getSupportedDevices()
	{
		return new String[] { "Voltcraft ME-32" };
	}
	
	private volatile boolean	run	= true;
	
	@Override
	protected void onStart(final DataDevice device)
	{
		try
		{
			// Start the poll thread
			final Thread poll = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while (run)
					{
						try
						{
							device.write(new byte[] { 0x44 }); // 0x44 == ASCII 'D'
							Thread.sleep(pollInterval);
						}
						catch (InterruptedException | IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			});
			poll.setName("Voltcraft ME-32 polling thread");
			poll.setPriority(Thread.MIN_PRIORITY);
			poll.setDaemon(true);
			poll.start();
			
			// Start reading
			final byte[] buffer = new byte[13];
			int pos = 0;
			while (run)
			{
				final byte read = (byte) device.read();
				if (read == -1) // End of file
				{
					throw new EOFException();
				}
				else if (read == 13) // Carriage return
				{
					if (pos == 13)
					{
						decodePacket(buffer);
					}
					pos = 0;
				}
				else if (pos < buffer.length)
				{
					buffer[pos] = read;
					pos++;
				}
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void decodePacket(final byte[] packetBytes)
	{
		// Read the packet as a string
		final String packet = new String(packetBytes, packetCharset);
		
		// Determine the displayed number
		String numberString = packet.substring(3, 9);
		numberString = numberString.replace(".L", "").replace("L", "").replace('O', '0').trim();
		BigDecimal number = null;
		try
		{
			number = new BigDecimal(numberString);
		}
		catch (final NumberFormatException e)
		{
			return;
		}
		
		// SI Prefix
		final String prefixString = packet.substring(9, 10);
		SI.Prefix prefix = null;
		if (prefixString.equals(" "))
		{
			prefix = SI.Prefix.none;
		}
		else
		{
			for (final SI.Prefix p : SI.Prefix.values())
			{
				if (p.symbol.equals(prefixString))
				{
					prefix = p;
					break;
				}
			}
		}
		if (prefix == null) { return; }
		
		String unit = packet.substring(10, 13).trim();
		if (unit.equalsIgnoreCase("ohm"))
		{
			unit = "\u03a9"; // Ohm, uppercase Greek Omega
		}
		else if (unit.equalsIgnoreCase("c"))
		{
			unit = "\u00B0C"; // Degrees Celsius
		}
		
		// Unit
		this.updateData(new Measurement(number, unit, prefix));
	}
	
	@Override
	protected void onStop()
	{
		run = false;
	}
}
