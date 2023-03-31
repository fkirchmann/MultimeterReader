/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.stage.Stage;

import javax.swing.JOptionPane;

import com.mtreader.api.DataSource;
import com.mtreader.api.impl.VoltcraftME32;
import com.mtreader.api.impl.VoltcraftVC840;

/**
 * 
 * @author Felix Kirchmann
 */
public class MTReader extends Application
{
	private static Map<String, Class<? extends DataSource>>	sourcePlugins	= Collections
																					.synchronizedMap(new HashMap<String, Class<? extends DataSource>>());
	private static List<DeviceConnection>					connections		= new ArrayList<>();
	private static Object									connectionsSync	= new Object();
	
	// Main application entry point
	public static void main(final String[] args)
	{
		launch(args);
	}
	
	@Override
	public void start(final Stage primaryStage) throws Exception
	{
		try
		{
			loadPlugins();
			ConnectDialog.getInstance();
		}
		catch (final Throwable t)
		{
			MTReader.exitError(t);
		}
	}
	
	private static void loadPlugins()
	{
		loadPlugin(new VoltcraftVC840());
		loadPlugin(new VoltcraftME32());
	}
	
	private static void loadPlugin(final DataSource plugin)
	{
		for (final String supportedDevice : plugin.getSupportedDevices())
		{
			sourcePlugins.put(supportedDevice, plugin.getClass());
		}
	}
	
	/** -------------------------------------------------------------------------- **/
	
	public static void registerConnection(final DeviceConnection connection)
	{
		if (connection == null) { throw new NullPointerException(); }
		synchronized (connectionsSync)
		{
			if (!connections.contains(connection))
			{
				connections.add(connection);
			}
		}
	}
	
	public static void unregisterConnection(final DeviceConnection connection)
	{
		synchronized (connectionsSync)
		{
			connections.remove(connection);
		}
	}
	
	public static DeviceConnection[] getConnections()
	{
		synchronized (connectionsSync)
		{
			return connections.toArray(new DeviceConnection[0]);
		}
	}
	
	/** -------------------------------------------------------------------------- **/
	
	public static void openConnectDialog()
	{
		ConnectDialog.getInstance();
	}
	
	public static String[] getPlugins()
	{
		return sourcePlugins.keySet().toArray(new String[0]);
	}
	
	public static DataSource getPlugin(final String name)
	{
		final Class<? extends DataSource> clazz = sourcePlugins.get(name);
		if (clazz == null) { return null; }
		try
		{
			return clazz.newInstance();
		}
		catch (InstantiationException | IllegalAccessException | ExceptionInInitializerError e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/** -------------------------------------------------------------------------- **/
	
	public static void exitError(final Object error)
	{
		if (error instanceof Throwable)
		{
			((Throwable) error).printStackTrace();
		}
		String message = Constants.productName + " has encountered a fatal error and has to exit.\n";
		message += "Below are error details that may help the application developer to improve application stability.\n\n";
		message += Func.toString(error);
		System.err.println(message);
		try
		{
			JOptionPane.showMessageDialog(null, message, "Fatal error", JOptionPane.ERROR_MESSAGE);
		}
		catch (final Throwable t)
		{
			System.err.println("Additionally, an error occured while trying to display the error message:");
			t.printStackTrace();
		}
		System.exit(1);
	}
}
