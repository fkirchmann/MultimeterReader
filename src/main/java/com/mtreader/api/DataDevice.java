/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader.api;

import java.io.IOException;

/**
 *
 * @author Felix Kirchmann
 */
public abstract class DataDevice
{
	public abstract String getName();
	
	public int read() throws IOException
	{
		final byte read = read(1)[0];
		if (read < 0)
		{
			return 256 + read;
		}
		else
		{
			return read;
		}
	}
	
	public abstract byte[] read(int numBytes) throws IOException;
	
	public abstract void write(byte[] bytes) throws IOException;
}
