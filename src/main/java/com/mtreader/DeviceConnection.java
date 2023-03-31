/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.mtreader.api.DataDevice;
import com.mtreader.api.DataReceiver;
import com.mtreader.api.DataSource;
import com.mtreader.api.Measurement;
import com.mtreader.api.PortParameters;

/**
 * 
 * @author Felix Kirchmann
 */
public class DeviceConnection
{
	public static DeviceConnection getInstance(final String portName, final String deviceType)
	{
		final DataSource plugin = MTReader.getPlugin(deviceType);
		if (plugin == null)
		{
			MessageBox.show("Error", "Plugin initialization failed.");
			return null;
		}
		
		InputStream in = null;
		OutputStream out = null;
		SerialPort port = null;
		try
		{
			final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			
			port = (SerialPort) portIdentifier.open("", 2000);
			
			final PortParameters params = plugin.getPortParameters();
			port.setDTR(params.dtr);
			port.setRTS(params.rts);
			port.setSerialPortParams(params.baudRate, params.dataBits, params.stopBits, params.parity);
			
			in = port.getInputStream();
			out = port.getOutputStream();
		}
		catch (final NoSuchPortException e)
		{
			MessageBox.show("Error", "Unable to locate port.");
			return null;
		}
		catch (final PortInUseException e)
		{
			MessageBox.show("Error", "Port is being used by another program.");
			return null;
		}
		catch (final UnsupportedCommOperationException e)
		{
			MessageBox
					.show("Error",
							"This port does not support the port parameters specified by the connected device (baudrate, parity etc).");
			return null;
		}
		catch (final IOException e)
		{
			MessageBox.show("Error",
					"An unknown error occured while trying to connect to the port.\n\n" + Func.toString(e));
			return null;
		}
		
		final DeviceConnection instance = new DeviceConnection();
		instance.portName = portName;
		instance.deviceType = deviceType;
		instance.port = port;
		instance.device = new StreamDataDevice(in, out, portName);
		instance.plugin = plugin;
		
		final Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				plugin.start(instance.device);
			}
		});
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setName("[Plugin] " + deviceType + " @" + portName);
		thread.start();
		
		MTReader.registerConnection(instance);
		
		return instance;
	}
	
	private String		deviceType, portName;
	private DataDevice	device;
	private DataSource	plugin;
	private SerialPort	port;
	private Measurement	lastData	= null;
	
	private DeviceConnection()
	{
	}
	
	public Measurement getLastData()
	{
		return lastData;
	}
	
	public void addReceiver(final DataReceiver receiver)
	{
		plugin.addReceiver(receiver);
	}
	
	public void removeReceiver(final DataReceiver receiver)
	{
		plugin.removeReceiver(receiver);
	}
	
	public void close()
	{
		plugin.stop();
		port.close();
	}
	
	@Override
	public String toString()
	{
		return deviceType + " on " + portName;
	}
}
