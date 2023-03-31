/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.mtreader.api.DataReceiver;
import com.mtreader.api.Measurement;

/**
 * 
 * @author Felix Kirchmann
 */
@SuppressWarnings("unused")
public class DataTableWindow
{
	public static final Comparator<String>	numberComparator	= new Comparator<String>()
																{
																	@Override
																	public int compare(final String o1, final String o2)
																	{
																		return new BigDecimal(o1)
																				.compareTo(new BigDecimal(o2));
																	}
																};
	
	public static DataTableWindow getInstance(final TableDataSource x, final TableDataSource y, final SI.Prefix unitX,
			final SI.Prefix unitY)
	{
		final Stage stage = new Stage();
		final FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ConnectDialog.class.getResource("/"));
		try
		{
			loader.load(ConnectDialog.class.getResourceAsStream("/DataTableWindow.fxml"));
		}
		catch (final IOException e)
		{
			MTReader.exitError(e);
		}
		final Parent root = (Parent) loader.getRoot();
		stage.setTitle("Datalog");
		stage.setScene(new Scene(root, Constants.datalogWindowSize[0], Constants.datalogWindowSize[1]));
		
		final DataTableWindow window = (DataTableWindow) loader.getController();
		window.x = x;
		window.y = y;
		window.unitX = unitX;
		window.unitY = unitY;
		window.stage = stage;
		
		window.initialized();
		stage.show();
		return window;
	}
	
	private TableDataSource			x, y;
	private SI.Prefix				unitX, unitY;
	
	private List<DataPoint>			points	= new ArrayList<>();
	
	private Stage					stage;
	
	@FXML
	private ContextMenu				menu;
	
	@FXML
	private Button					menuButton;
	
	@FXML
	private MenuItem				deviceX, deviceY;
	
	@FXML
	private TableView<DataPoint>	table;
	
	@FXML
	private TableColumn<DataPoint, String>	columnX, columnY;
	
	private void initialized()
	{
		columnX.setCellValueFactory(new PropertyValueFactory<DataPoint, String>("x"));
		columnY.setCellValueFactory(new PropertyValueFactory<DataPoint, String>("y"));
		
		columnX.setComparator(numberComparator);
		columnY.setComparator(numberComparator);
		
		table.getSortOrder().addListener(new ListChangeListener<TableColumn<DataPoint, ?>>()
		{
			@Override
			public void onChanged(final Change<? extends TableColumn<DataPoint, ?>> change)
			{
				if (table.getSortOrder().size() == 0)
				{
					refreshTable();
				}
			}
		});
		
		deviceX.setText("X: " + x.toString());
		deviceY.setText("Y: " + y.toString());
		
	}
	
	private void refreshTable()
	{
		table.getItems().clear();
		table.getItems().addAll(points);
	}
	
	@FXML
	private void onMenu(final Event e)
	{
		menu.show(menuButton, Side.BOTTOM, 0.0, 0.0);
	}
	
	private boolean	firstMeasure	= true;
	private long	lastMeasure		= 0;
	
	@FXML
	private void onMeasure(final Event event)
	{
		if (lastMeasure >= System.currentTimeMillis()) { return; }
		
		final Measurement xData = this.x.getLastData();
		final Measurement yData = this.y.getLastData();
		
		if (xData == null || yData == null) { return; }
		
		final BigDecimal x = SI.convert(xData.getData(), xData.getPrefix(), unitX);
		final BigDecimal y = SI.convert(yData.getData(), yData.getPrefix(), unitY);
		final DataPoint point = new DataPoint(x, y);
		
		points.add(point);
		
		if (firstMeasure)
		{
			table.getItems().add(point);
			this.columnX.setText("X in " + unitX.symbol + xData.getUnit());
			this.columnY.setText("Y in " + unitY.symbol + yData.getUnit());
			firstMeasure = false;
		}
		else
		{
			int newIndex = -1;
			if (table.getSortOrder().size() > 0)
			{
				@SuppressWarnings("unchecked")
				final TableColumn<DataPoint, String> sortColumn = (TableColumn<DataPoint, String>) table.getSortOrder()
						.get(0);
				final SortType sortType = sortColumn.getSortType();
				final Comparator<String> comparator = sortColumn.getComparator();
				final String newItem = sortColumn.getCellData(point);
				
				for (int i = 0; i < table.getItems().size(); i++)
				{
					final String currentItem = sortColumn.getCellData(i);
					
					final int compared = comparator.compare(newItem, currentItem);
					
					if ((compared == 0) || (sortType == SortType.ASCENDING && compared < 0)
							|| (sortType == SortType.DESCENDING && compared > 0))
					{
						table.getItems().add(i, point);
						newIndex = i;
						break;
					}
				}
				if (newIndex == -1)
				{
					table.getItems().add(point);
					newIndex = table.getItems().size() - 1;
				}
			}
			else
			{
				table.getItems().add(point);
				newIndex = table.getItems().size() - 1;
			}
			table.scrollTo(newIndex);
		}
		
		this.x.dataRecorded();
		this.y.dataRecorded();
		
		lastMeasure = System.currentTimeMillis() + 1500;
	}
	
	@FXML
	private void onDataPointDelete(final Event event)
	{
		final DataPoint selected = table.getSelectionModel().getSelectedItem();
		if (selected != null)
		{
			table.getItems().remove(selected);
			points.remove(selected);
		}
	}
	
	@FXML
	private void onCSVExport(final Event event)
	{
		final FileChooser fileChooser = new FileChooser();
		final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Comma-separated values (*.csv)",
				"*.csv");
		fileChooser.getExtensionFilters().add(extFilter);
		final File file = fileChooser.showSaveDialog(stage);
		if (file == null) { return; }
		
		final StringBuilder sb = new StringBuilder();
		
		sb.append(columnX.getText());
		sb.append(";");
		sb.append(columnY.getText());
		sb.append("\r\n");
		
		for (final DataPoint point : table.getItems())
		{
			sb.append(point.getX());
			sb.append(";");
			sb.append(point.getY());
			sb.append("\r\n");
		}
		
		try
		{
			final OutputStream out = new FileOutputStream(file);
			out.write(sb.toString().getBytes(Constants.charset));
			out.flush();
			out.close();
		}
		catch (final IOException e)
		{
			MessageBox.show("Error", "Unable to write file '" + file.getAbsolutePath() + "'. Error details below.\n\n"
					+ Func.toString(e));
		}
	}
	
	@FXML
	private void onPreviewGraph(final Event event)
	{
		LineChartWindow.show(table.getItems().toArray(new DataPoint[0]), columnX.getText(), columnY.getText());
	}
	
	public static class DataPoint
	{
		private final BigDecimal	x, y;
		
		public DataPoint(final BigDecimal x, final BigDecimal y)
		{
			if (x == null || y == null) { throw new NullPointerException(); }
			this.x = x;
			this.y = y;
		}
		
		public String getX()
		{
			return x.toPlainString();
		}
		
		public String getY()
		{
			return y.toPlainString();
		}
		
		public BigDecimal getXBD()
		{
			return x;
		}
		
		public BigDecimal getYBD()
		{
			return y;
		}
	}
	
	public static abstract class TableDataSource
	{
		public abstract Measurement getLastData();
		
		public void dataRecorded()
		{
		}
		
		@Override
		public abstract String toString();
	}
	
	public static class ConnectionDataSource extends TableDataSource implements DataReceiver
	{
		private final DeviceConnection	connection;
		private Measurement				lastData	= null;
		
		public ConnectionDataSource(final DeviceConnection connection)
		{
			this.connection = connection;
			connection.addReceiver(this);
		}
		
		@Override
		public Measurement getLastData()
		{
			return lastData;
		}
		
		@Override
		public void onData(final Measurement data)
		{
			this.lastData = data;
		}
		
		@Override
		public String toString()
		{
			return connection.toString();
		}
		
		public static TableDataSource[] wrapSources(final DeviceConnection... connections)
		{
			final TableDataSource[] wrappers = new TableDataSource[connections.length];
			for (int i = 0; i < connections.length; i++)
			{
				wrappers[i] = new ConnectionDataSource(connections[i]);
			}
			return wrappers;
		}
	}
	
	public static class CounterDataSource extends TableDataSource
	{
		private static final SI.Prefix	prefix	= SI.Prefix.none;
		private static final String		unit	= "#";
		
		private int						counter	= 1;
		
		@Override
		public String toString()
		{
			return "(none)";
		}
		
		@Override
		public Measurement getLastData()
		{
			return new Measurement(BigDecimal.valueOf(counter), unit, prefix);
		}
		
		@Override
		public void dataRecorded()
		{
			counter++;
		}
	}
}
