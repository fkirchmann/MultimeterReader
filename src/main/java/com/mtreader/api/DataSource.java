/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader.api;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author Felix Kirchmann
 */
public abstract class DataSource
{
	public abstract String[] getSupportedDevices();
	
	public abstract PortParameters getPortParameters();
	
	protected abstract void onStart(DataDevice device);
	
	protected abstract void onStop();
	
	// -------------------
	
	private volatile AtomicBoolean	started	= new AtomicBoolean(false);
	
	public void start(final DataDevice device)
	{
		if (started.compareAndSet(false, true))
		{
			onStart(device);
		}
	}
	
	private volatile AtomicBoolean	stopped	= new AtomicBoolean(false);
	
	public void stop()
	{
		if (started.get() == true && stopped.compareAndSet(false, true))
		{
			onStop();
		}
	}
	
	// -------------------
	
	private final ArrayList<DataReceiver>	receivers		= new ArrayList<>();
	private final Object					receiversSync	= new Object();
	
	protected final void updateData(final Measurement measurement)
	{
		synchronized (receiversSync)
		{
			for (final DataReceiver receiver : receivers)
			{
				receiver.onData(measurement);
			}
		}
	}
	
	public final void addReceiver(final DataReceiver receiver)
	{
		if (receiver == null) { throw new NullPointerException(); }
		
		synchronized (receiversSync)
		{
			if (!receivers.contains(receiver))
			{
				receivers.add(receiver);
			}
		}
	}
	
	public final void removeReceiver(final DataReceiver receiver)
	{
		if (receiver == null) { throw new NullPointerException(); }
		
		synchronized (receiversSync)
		{
			receivers.remove(receiver);
		}
	}
}
