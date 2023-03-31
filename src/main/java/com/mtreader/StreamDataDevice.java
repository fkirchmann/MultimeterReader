/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.mtreader.api.DataDevice;

/**
 * 
 * @author Felix Kirchmann
 */
public class StreamDataDevice extends DataDevice
{
	private final String		name;
	private final InputStream	in;
	private final OutputStream	out;
	
	public StreamDataDevice(final InputStream in, final OutputStream out, final String name)
	{
		this.in = in;
		this.out = out;
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public int read() throws IOException
	{
		int read;
		do
		{
			read = in.read();
		}
		while (read == 0xFFFFFFFF);
		return read;
	}
	
	@Override
	public byte[] read(final int numBytes) throws IOException
	{
		if (numBytes < 0) { throw new IllegalArgumentException("Can't read a negative amount of bytes"); }
		if (numBytes == 0) { return new byte[0]; }
		
		final byte[] read = new byte[numBytes];
		int bytesRead, bytesReadTotal = 0;
		while (bytesReadTotal < numBytes)
		{
			bytesRead = in.read(read, bytesReadTotal, numBytes - bytesReadTotal);
			if (bytesRead == -1) { throw new EOFException(); }
			bytesReadTotal += bytesRead;
		}
		assert (bytesReadTotal == numBytes);
		return read;
	}
	
	@Override
	public void write(final byte[] bytes) throws IOException
	{
		out.write(bytes);
	}
	
}
