/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import java.util.List;

import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.stage.Stage;

import com.mtreader.DataTableWindow.DataPoint;

/**
 * 
 * @author Felix Kirchmann
 */
public class LineChartWindow
{
	public static void show(final DataPoint[] points, final String xAxisLabel, final String yAxisLabel)
	{
		final Stage stage = new Stage();
		stage.setTitle("Preview chart");
		
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setForceZeroInRange(false);
		yAxis.setForceZeroInRange(false);
		xAxis.setLabel(xAxisLabel);
		yAxis.setLabel(yAxisLabel);
		
		final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
		lineChart.setLegendVisible(false);
		lineChart.setAnimated(false);
		
		final XYChart.Series<Number, Number> series = new XYChart.Series<>();
		final List<Data<Number, Number>> seriesItems = series.getData();
		
		for (final DataPoint point : points)
		{
			seriesItems.add(new Data<Number, Number>(point.getXBD(), point.getYBD()));
		}
		
		final Scene scene = new Scene(lineChart, 800, 600);
		lineChart.getData().add(series);
		
		stage.setScene(scene);
		stage.show();
	}
}
