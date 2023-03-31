/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import gnu.io.CommPortIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 
 * @author Felix Kirchmann
 */
@SuppressWarnings("unused")
public class ConnectDialog implements ChangeListener<Number>
{
	public static ConnectDialog getInstance()
	{
		final Stage stage = new Stage();
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ConnectDialog.class.getResource("/"));
		try
		{
			loader.load(ConnectDialog.class.getResourceAsStream("/ConnectDialog.fxml"));
		}
		catch (final IOException e)
		{
			MTReader.exitError(e);
		}
		final Parent root = (Parent) loader.getRoot();
		stage.setTitle("Connect to device");
		stage.setScene(new Scene(root, Constants.connectWindowSize[0], Constants.connectWindowSize[1]));
		
		final ConnectDialog window = (ConnectDialog) loader.getController();
		window.stage = stage;
		window.initialized();
		stage.show();
		return window;
	}
	
	private void initialized()
	{
		serialPort.getSelectionModel().selectedIndexProperty().addListener(this);
		serialPort.getItems().clear();
		
		@SuppressWarnings("unchecked")
		final Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
		
		final List<String> portNames = new ArrayList<>();
		while (ports.hasMoreElements())
		{
			final CommPortIdentifier port = ports.nextElement();
			if (port.getPortType() == CommPortIdentifier.PORT_SERIAL)
			{
				portNames.add(port.getName());
			}
		}
		serialPort.getItems().addAll(portNames);
		
		deviceType.getSelectionModel().selectedIndexProperty().addListener(this);
		deviceType.getItems().clear();
		deviceType.getItems().addAll(MTReader.getPlugins());
	}
	
	private Stage	stage	= null;
	
	@FXML
	private ChoiceBox<String>	serialPort, deviceType;
	
	@FXML
	private Button				connect, cancel;
	
	@FXML
	private void onConnect(final Event event)
	{
		final String port = serialPort.getSelectionModel().getSelectedItem();
		final String device = deviceType.getSelectionModel().getSelectedItem();
		
		final DeviceConnection connection = DeviceConnection.getInstance(port, device);
		if (connection != null)
		{
			final DisplayWindow dialog = new DisplayWindow(connection);
			dialog.setOnHideEventHandler(new EventHandler<WindowEvent>()
			{
				@Override
				public void handle(final WindowEvent event)
				{
					connection.close();
				}
			});
			dialog.show();
			stage.hide();
			MTReader.registerConnection(connection);
		}
	}
	
	@FXML
	private void onCancel(final Event event)
	{
		stage.hide();
	}
	
	// Will be called each time a different menu item is selected
	@Override
	public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue)
	{
		if (serialPort.getSelectionModel().getSelectedIndex() != -1
				&& deviceType.getSelectionModel().getSelectedIndex() != -1)
		{
			connect.setDisable(false);
		}
	}
}
