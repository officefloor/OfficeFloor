/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.demo.stocks.client;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import net.officefloor.plugin.gwt.comet.api.OfficeFloorComet;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.Options;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.ScaleType;

/**
 * {@link Widget} for graphing {@link StockPrice}.
 * 
 * @author Daniel Sagenschneider
 */
public class StockGraphWidget extends VerticalPanel {

	/**
	 * Initiate.
	 * 
	 * @param historyPeriod
	 *            Time in milliseconds to display history of prices.
	 * @param redrawPeriod
	 *            Time in milliseconds to allow before complete redraw of the
	 *            graph.
	 * @param width
	 *            Width.
	 * @param height
	 *            Height.
	 * @param stocks
	 *            {@link Stock} instances to graph.
	 */
	public StockGraphWidget(final long historyPeriod, final long redrawPeriod,
			final String width, final String height, Stock... stocks) {

		// Create sorted copy of stocks
		final Stock[] orderedStocks = new Stock[stocks.length];
		for (int i = 0; i < orderedStocks.length; i++) {
			orderedStocks[i] = stocks[i];
		}
		Arrays.sort(orderedStocks, new Comparator<Stock>() {
			@Override
			public int compare(Stock a, Stock b) {
				return String.CASE_INSENSITIVE_ORDER.compare(a.getName(),
						b.getName());
			}
		});

		// Load the chart visualisation
		Runnable loadGraph = new Runnable() {
			@Override
			public void run() {

				// Provide tab panel for graphs
				TabPanel tab = new TabPanel();
				tab.setWidth(width);
				tab.getTabBar().setWidth(width);
				tab.getDeckPanel().setWidth(width);
				StockGraphWidget.this.add(tab);

				// Load each graph onto a tab
				final StockGraph[] graphs = new StockGraph[orderedStocks.length];
				for (int i = 0; i < orderedStocks.length; i++) {
					Stock stock = orderedStocks[i];

					// Create the stock graph
					StockGraph graph = new StockGraph(stock, historyPeriod,
							redrawPeriod, width, height);
					graphs[i] = graph;

					// Add the stock graph
					tab.add(graph.getChart(), stock.getName());
				}

				// Provide the selection handler
				tab.addSelectionHandler(new SelectionHandler<Integer>() {
					@Override
					public void onSelection(SelectionEvent<Integer> event) {

						// Unselect all graphs
						for (StockGraph graph : graphs) {
							graph.setSelected(false);
						}

						// Select the graph for the tab
						Integer tabIndex = event.getSelectedItem();
						graphs[tabIndex.intValue()].setSelected(true);
					}
				});

				// Display first tab selected (also sets up graph)
				tab.selectTab(0);
			}
		};
		VisualizationUtils.loadVisualizationApi(loadGraph,
				AnnotatedTimeLine.PACKAGE);
	}

	/**
	 * Graph of a {@link Stock}.
	 */
	private static class StockGraph {

		/**
		 * {@link Stock} being graphed.
		 */
		private final Stock stock;

		/**
		 * Time in milliseconds to display history of prices.
		 */
		private final long historyPeriod;

		/**
		 * Time in milliseconds to allow before complete redraw of the graph.
		 */
		private final long redrawPeriod;

		/**
		 * {@link DataTable}.
		 */
		private final DataTable data;

		/**
		 * {@link Options}.
		 */
		private final Options options;

		/**
		 * {@link AnnotatedTimeLine}.
		 */
		private final AnnotatedTimeLine chart;

		/**
		 * Flag indicating if selected for display.
		 */
		private boolean isSelected = false;

		/**
		 * Initiate.
		 * 
		 * @param stock
		 *            {@link Stock} to graph.
		 * @param historyPeriod
		 *            Time in milliseconds to display history of prices.
		 * @param redrawPeriod
		 *            Time in milliseconds to allow before complete redraw of
		 *            the graph.
		 * @param width
		 *            Width.
		 * @param height
		 *            Height.
		 */
		public StockGraph(Stock stock, long historyPeriod, long redrawPeriod,
				String width, String height) {
			this.stock = stock;
			this.historyPeriod = historyPeriod;
			this.redrawPeriod = redrawPeriod;

			// Initiate the data for the stocks
			this.data = DataTable.create();
			this.data.addColumn(ColumnType.DATETIME);
			this.data.addColumn(ColumnType.NUMBER, this.stock.getName());

			// Configure the chart
			this.options = Options.create();
			this.options.setDisplayAnnotations(false);
			this.options.setDisplayZoomButtons(false);
			this.options.setOption("dateFormat", "d MMM yyyy");
			this.options.setOption("displayRangeSelector", false);
			this.options.setOption("displayLegendDots", false);
			this.options.setScaleType(ScaleType.MAXIMIZE);
			this.options.setOption("allowRedraw", true);

			// Add the chart
			this.chart = new AnnotatedTimeLine(this.data, this.options, width,
					height);

			// Listen for stock price events
			OfficeFloorComet.subscribe(StockPriceEvents.class,
					new StockPriceEvents() {
						@Override
						public void event(StockPrice stockPrice, Stock stock) {
							StockGraph.this.addPrice(stockPrice.getTimestamp(),
									stockPrice);
						}
					}, new StockFilter(this.stock));
		}

		/**
		 * Flags whether selected.
		 * 
		 * @param isSelected
		 *            Indicates if selected.
		 */
		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;

			// If selected, draw the graph fresh
			if (this.isSelected) {
				this.options.setOption("allowRedraw", false);
				this.chart.draw(this.data, this.options);
			}
		}

		/**
		 * Obtains the {@link AnnotatedTimeLine}.
		 * 
		 * @return {@link AnnotatedTimeLine}.
		 */
		public AnnotatedTimeLine getChart() {
			return this.chart;
		}

		/**
		 * Adds a price to the display.
		 * 
		 * @param priceTimestamp
		 *            {@link StockPrice} time stamp.
		 * @param price
		 *            {@link StockPrice}. May be <code>null</code> if no price
		 *            to allow moving graph to time stamp.
		 */
		private void addPrice(long priceTimestamp, StockPrice price) {

			// Only add price when chart available
			if (this.chart == null) {
				return;
			}

			// By default allow to redraw graph (for dynamic values)
			boolean isAllowRedraw = true;

			// Only truncate if have rows (no rows on first price)
			if (this.data.getNumberOfRows() > 0) {

				// Determine the start time
				long startTime = priceTimestamp - this.historyPeriod;

				// Determine redraw hard time
				long redrawTime = startTime - this.redrawPeriod;

				// Determine if need to truncate display
				Date oldestPriceTimestamp = this.data.getValueDate(0, 0);
				if (oldestPriceTimestamp.getTime() < redrawTime) {

					// Determine number of rows before period of time
					double firstPrice = -1.00;
					int numberOfRowsToRemove = 0;
					NO_FURTHER_ROWS: for (int i = 0; i < this.data
							.getNumberOfRows(); i++) {
						Date rowTimestamp = this.data.getValueDate(i, 0);
						if (rowTimestamp.getTime() < startTime) {

							// Flag to remove this row
							numberOfRowsToRemove++;

							// Keep track of first price for graph
							if (!(this.data.isValueNull(i, 1))) {
								firstPrice = this.data.getValueDouble(i, 1);
							}

						} else {
							break NO_FURTHER_ROWS;
						}
					}

					// Remove rows before period of time
					if (numberOfRowsToRemove > 0) {

						// Remove the rows
						this.data.removeRows(0, numberOfRowsToRemove);

						// Have clean draw of the graph
						isAllowRedraw = false;

						// Ensure has a row for continuous graph appearance
						if (firstPrice >= 0) {
							if (this.data.getNumberOfRows() == 0) {
								this.data.addRow(); // ensure row
								this.data.setValue(0, 0, new Date(
										priceTimestamp));
							}
							this.data.setValue(0, 1, firstPrice);
						}
					}
				}
			}

			// Specify whether dynamically display added price
			this.options.setOption("allowRedraw", isAllowRedraw);

			// Add the price for the stock
			int rowIndex = this.data.addRow();
			this.data.setValue(rowIndex, 0, new Date(priceTimestamp));
			if (price != null) {
				this.data.setValue(rowIndex, 1, price.getAskPrice());
			}

			// Only draw if selected
			if (this.isSelected) {
				// Draw graph for added price
				this.chart.draw(this.data, this.options);
			}
		}
	}

	/**
	 * Filter on the {@link Stock}.
	 */
	public static class StockFilter implements IsSerializable {

		/**
		 * Market identifier for the {@link Stock}.
		 */
		private String marketId;

		/**
		 * Initiate.
		 * 
		 * @param stock
		 *            {@link Stock} of interest.
		 */
		public StockFilter(Stock stock) {
			this.marketId = stock.getMarketId();
		}

		/**
		 * Initiate for {@link IsSerializable}.
		 */
		public StockFilter() {
		}

		/*
		 * ======================== Object =======================
		 */

		@Override
		public boolean equals(Object obj) {

			// Ensure match on stock
			if (!(obj instanceof Stock)) {
				return false;
			}
			Stock that = (Stock) obj;

			// Include if matching stock
			return this.marketId.equals(that.getMarketId());
		}
	}

}