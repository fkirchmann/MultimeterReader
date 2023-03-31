/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import java.io.IOException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import com.mtreader.DataTableWindow.ConnectionDataSource;
import com.mtreader.DataTableWindow.TableDataSource;
import com.mtreader.api.Measurement;

/**
 * 
 * @author Felix Kirchmann
 */
@SuppressWarnings("unused")
public class DataTableSetupWindow
{
	public static DataTableSetupWindow getInstance()
	{
		final Stage stage = new Stage();
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ConnectDialog.class.getResource("/"));
		try
		{
			loader.load(ConnectDialog.class.getResourceAsStream("/DataTableSetupWindow.fxml"));
		}
		catch (final IOException e)
		{
			MTReader.exitError(e);
		}
		final Parent root = (Parent) loader.getRoot();
		stage.setTitle("Datalog Setup");
		stage.setScene(new Scene(root, Constants.datalogSetupWindowSize[0], Constants.datalogSetupWindowSize[1]));
		
		final DataTableSetupWindow window = (DataTableSetupWindow) loader.getController();
		window.stage = stage;
		window.initialized();
		stage.show();
		return window;
	}
	
	private Stage	stage;
	
	@FXML
	private ChoiceBox<SI.Prefix>	xUnit, yUnit;
	
	@FXML
	private ChoiceBox<TableDataSource>	xDevice, yDevice;
	
	private void initialized()
	{
		xUnit.getItems().clear();
		xUnit.getItems().addAll(SI.Prefix.values());
		xUnit.getSelectionModel().select(SI.Prefix.none);
		xUnit.setDisable(true);
		
		yUnit.getItems().clear();
		yUnit.getItems().addAll(SI.Prefix.values());
		yUnit.getSelectionModel().select(SI.Prefix.none);
		
		xDevice.getItems().clear();
		xDevice.getItems().add(new DataTableWindow.CounterDataSource());
		xDevice.getItems().addAll(ConnectionDataSource.wrapSources(MTReader.getConnections()));
		
		yDevice.getItems().clear();
		yDevice.getItems().addAll(ConnectionDataSource.wrapSources(MTReader.getConnections()));
		
		xDevice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TableDataSource>()
		{
			@Override
			public void changed(final ObservableValue<? extends TableDataSource> observable,
					final TableDataSource oldValue, final TableDataSource newValue)
			{
				xUnit.setDisable(newValue instanceof DataTableWindow.CounterDataSource);
			}
		});
		
		xDevice.getSelectionModel().selectedItemProperty().addListener(new UnitSelector(xUnit));
		yDevice.getSelectionModel().selectedItemProperty().addListener(new UnitSelector(yUnit));
		
		xDevice.getSelectionModel().select(0);
		yDevice.getSelectionModel().select(0);
	}
	
	@FXML
	private void onOK(final Event event)
	{
		DataTableWindow.getInstance(xDevice.getSelectionModel().getSelectedItem(), yDevice.getSelectionModel()
				.getSelectedItem(), xUnit.getSelectionModel().getSelectedItem(), yUnit.getSelectionModel()
				.getSelectedItem());
		stage.hide();
	}
	
	@FXML
	private void onCancel(final Event event)
	{
		stage.hide();
	}
	
	private static class UnitSelector implements ChangeListener<TableDataSource>
	{
		private final ChoiceBox<SI.Prefix>	unitBox;
		
		public UnitSelector(final ChoiceBox<SI.Prefix> unitBox)
		{
			this.unitBox = unitBox;
		}
		
		@Override
		public void changed(final ObservableValue<? extends TableDataSource> observable,
				final TableDataSource oldValue, final TableDataSource newValue)
		{
			final Measurement lastData = newValue.getLastData();
			if (lastData == null)
			{
				unitBox.getSelectionModel().select(SI.Prefix.none);
			}
			else
			{
				unitBox.getSelectionModel().select(lastData.getPrefix());
			}
		}
	}
}
