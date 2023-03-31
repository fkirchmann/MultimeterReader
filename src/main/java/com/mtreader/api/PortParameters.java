/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader.api;

/**
 * 
 * @author Felix Kirchmann
 */
public final class PortParameters
{
	// Constants
	
	public static final int	BAUDRATE_110	= 110;
	public static final int	BAUDRATE_300	= 300;
	public static final int	BAUDRATE_600	= 600;
	public static final int	BAUDRATE_1200	= 1200;
	public static final int	BAUDRATE_2400	= 2400;
	public static final int	BAUDRATE_4800	= 4800;
	public static final int	BAUDRATE_9600	= 9600;
	public static final int	BAUDRATE_14400	= 14400;
	public static final int	BAUDRATE_19200	= 19200;
	public static final int	BAUDRATE_38400	= 38400;
	public static final int	BAUDRATE_57600	= 57600;
	public static final int	BAUDRATE_115200	= 115200;
	public static final int	BAUDRATE_128000	= 128000;
	public static final int	BAUDRATE_256000	= 256000;
	
	public static final int	DATABITS_5		= 5;
	public static final int	DATABITS_6		= 6;
	public static final int	DATABITS_7		= 7;
	public static final int	DATABITS_8		= 8;
	
	public static final int	STOPBITS_1		= 1;
	public static final int	STOPBITS_2		= 2;
	public static final int	STOPBITS_1_5	= 3;
	
	public static final int	PARITY_NONE		= 0;
	public static final int	PARITY_ODD		= 1;
	public static final int	PARITY_EVEN		= 2;
	public static final int	PARITY_MARK		= 3;
	public static final int	PARITY_SPACE	= 4;
	
	// Constructor
	
	public PortParameters(final int baudRate, final int dataBits, final int stopBits, final int parity,
			final boolean dtr, final boolean rts)
	{
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
		this.dtr = dtr;
		this.rts = rts;
	}
	
	// Instance variables
	
	public final int	baudRate, dataBits, stopBits, parity;
	public final boolean	dtr, rts;
}
