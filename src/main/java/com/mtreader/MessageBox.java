/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import java.io.IOException;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * 
 * @author Felix Kirchmann
 */
@SuppressWarnings("unused")
public class MessageBox
{
	public static void show(final String title, final String message)
	{
		final Stage stage = new Stage();
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ConnectDialog.class.getResource("/"));
		try
		{
			loader.load(ConnectDialog.class.getResourceAsStream("/MessageBox.fxml"));
		}
		catch (final IOException e)
		{
			MTReader.exitError(e);
		}
		final Parent root = (Parent) loader.getRoot();
		
		stage.setTitle(title);
		stage.setScene(new Scene(root, Constants.messageBoxSize[0], Constants.messageBoxSize[1]));
		final MessageBox box = ((MessageBox) loader.getController());
		box.setMessage(message);
		box.stage = stage;
		stage.show();
	}
	
	private Stage	stage;
	
	private void setMessage(final String message)
	{
		this.message.setText(message);
	}
	
	@FXML
	private Label	message;
	
	@FXML
	private void onOK(final Event event)
	{
		stage.hide();
	}
}
