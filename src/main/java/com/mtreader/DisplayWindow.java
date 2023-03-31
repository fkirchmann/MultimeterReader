/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import com.mtreader.api.DataReceiver;
import com.mtreader.api.Measurement;

/**
 * 
 * @author Felix Kirchmann
 */
public class DisplayWindow implements DataReceiver
{
	private final Stage						stage		= new Stage();
	private final AnchorPane				root		= new AnchorPane();
	private final Scene						scene		= new Scene(root, Constants.displayWindowSize[0],
																Constants.displayWindowSize[1], Color.DARKGRAY);
	private final ContextMenu				contextMenu	= new ContextMenu();
	private final Label						data		= new Label(), unit = new Label();
	private final EventHandler<MouseEvent>	displayClickedHandler;
	private ProgressIndicator				wait		= new ProgressIndicator(
																ProgressIndicator.INDETERMINATE_PROGRESS);
	
	public DisplayWindow(final DeviceConnection connection)
	{
		stage.setTitle(connection.toString());
		stage.setResizable(false);
		
		configureContextMenu();
		
		final Image backgroundImage = new Image(this.getClass().getResourceAsStream("/displaybg.png"));
		final ImageView background = new ImageView(backgroundImage);
		
		AnchorPane.setLeftAnchor(background, 0.0);
		AnchorPane.setRightAnchor(background, 0.0);
		AnchorPane.setTopAnchor(background, 0.0);
		AnchorPane.setBottomAnchor(background, 0.0);
		
		unit.setFont(Font.font("System", 40));
		
		AnchorPane.setRightAnchor(unit, 20.0);
		AnchorPane.setBottomAnchor(unit, 10.0);
		
		data.setFont(Font.font("System", 70));
		
		AnchorPane.setRightAnchor(data, 80.0);
		AnchorPane.setBottomAnchor(data, 5.0);
		
		final BoxBlur antialias = new BoxBlur();
		antialias.setWidth(1);
		antialias.setHeight(1);
		antialias.setIterations(6);
		
		unit.setEffect(antialias);
		data.setEffect(antialias);
		
		unit.setOpacity(0.0);
		data.setOpacity(0.0);
		
		wait.setRotate(-6);
		AnchorPane.setLeftAnchor(wait, 0.0);
		AnchorPane.setRightAnchor(wait, 0.0);
		AnchorPane.setTopAnchor(wait, 20.0);
		AnchorPane.setBottomAnchor(wait, 20.0);
		
		root.getChildren().addAll(background, unit, data, wait);
		
		stage.setScene(scene);
		
		displayClickedHandler = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(final MouseEvent e)
			{
				if (e.getButton() == MouseButton.SECONDARY)
				{
					contextMenu.show(background, e.getScreenX(), e.getScreenY());
				}
				else
				{
					contextMenu.hide();
				}
			}
		};
		background.setOnMouseClicked(displayClickedHandler);
		data.setOnMouseClicked(displayClickedHandler);
		unit.setOnMouseClicked(displayClickedHandler);
		wait.setOnMouseClicked(displayClickedHandler);
		
		connection.addReceiver(this);
	}
	
	private void configureContextMenu()
	{
		final MenuItem newWindow = new MenuItem("Open new window");
		newWindow.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(final ActionEvent e)
			{
				MTReader.openConnectDialog();
			}
		});
		
		final MenuItem beginDatalog = new MenuItem("Log measured data");
		beginDatalog.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(final ActionEvent e)
			{
				DataTableSetupWindow.getInstance();
			}
		});
		
		contextMenu.getItems().addAll(newWindow, beginDatalog);
	}
	
	public void show()
	{
		stage.show();
	}
	
	public void hide()
	{
		stage.hide();
	}
	
	public void setOnHideEventHandler(final EventHandler<WindowEvent> handler)
	{
		stage.setOnHidden(handler);
	}
	
	private void toDisplayMode()
	{
		final FadeTransition fadeOut = new FadeTransition(Duration.millis(500), wait);
		fadeOut.setFromValue(1.0);
		fadeOut.setToValue(0.0);
		
		final FadeTransition fadeInUnit = new FadeTransition(Duration.millis(200), unit);
		fadeInUnit.setDelay(Duration.millis(200));
		fadeInUnit.setFromValue(0.0);
		fadeInUnit.setToValue(1.0);
		
		final FadeTransition fadeInData = new FadeTransition(Duration.millis(200), data);
		fadeInData.setDelay(Duration.millis(200));
		fadeInData.setFromValue(0.0);
		fadeInData.setToValue(1.0);
		
		fadeInUnit.play();
		fadeInData.play();
		fadeOut.play();
	}
	
	private boolean	firstMeasurement	= true;
	
	@Override
	public void onData(final Measurement measurement)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (firstMeasurement)
				{
					toDisplayMode();
					firstMeasurement = false;
				}
				data.setText(measurement.getData().toPlainString());
				unit.setText(measurement.getPrefix().symbol + measurement.getUnit());
			}
		});
	}
}
