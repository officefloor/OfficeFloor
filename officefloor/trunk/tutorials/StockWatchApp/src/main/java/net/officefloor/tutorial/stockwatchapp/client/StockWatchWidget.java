/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.tutorial.stockwatchapp.client;

import java.util.Iterator;
import java.util.List;

import net.officefloor.plugin.comet.api.OfficeFloorComet;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

/**
 * <p>
 * {@link Widget} for watching {@link StockPrice}.
 * <p>
 * This provides the hooks for listening on {@link StockPrice} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class StockWatchWidget extends VerticalPanel {

	/**
	 * Displayed {@link StockPrice} instances.
	 */
	private final ListDataProvider<StockPrice> displayedPrices = new ListDataProvider<StockPrice>();

	/**
	 * {@link StockInterest}.
	 */
	private final StockInterest filter = new StockInterest();

	/**
	 * Template to render the {@link CellTable} headers.
	 */
	static interface HeaderTemplate extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<p class=\"stockHeader\">{0}</p>")
		SafeHtml header(String text);
	}

	private final HeaderTemplate headerTemplate = GWT
			.create(HeaderTemplate.class);

	/**
	 * Template to render the {@link Stock} name.
	 */
	static interface NameTemplate extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<p class=\"stockName\">{0}</p>")
		SafeHtml cell(String text);
	}

	private final NameTemplate nameTemplate = GWT.create(NameTemplate.class);

	/**
	 * Template to render the {@link StockPrice} size/price.
	 */
	static interface NumberTemplate extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<p class=\"stockNumber\">{0}</p>")
		SafeHtml cell(String text);
	}

	private final NumberTemplate numberTemplate = GWT
			.create(NumberTemplate.class);

	/**
	 * Extracts the {@link SafeHtml} for the {@link Column}.
	 */
	private interface ColumnValue {

		/**
		 * Obtains the {@link SafeHtml} value.
		 * 
		 * @param price
		 *            {@link StockPrice}.
		 * @return {@link SafeHtml}.
		 */
		SafeHtml getValue(StockPrice price);
	}

	/**
	 * Initiate.
	 */
	public StockWatchWidget() {

		// Locale specific formatters
		final NumberFormat sizeFormat = NumberFormat.getDecimalFormat();
		final NumberFormat priceFormat = NumberFormat.getCurrencyFormat();

		// Configure table
		CellTable<StockPrice> table = new CellTable<StockPrice>();
		this.addColumn(table, "Name", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockPrice price) {
				String text = price.getStock().getName();
				return StockWatchWidget.this.nameTemplate.cell(text);
			}
		});
		this.addColumn(table, "Bid Size", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockPrice price) {
				String text = sizeFormat.format(Integer.valueOf(price
						.getBidSize()));
				return StockWatchWidget.this.numberTemplate.cell(text);
			}
		});
		this.addColumn(table, "Bid Price", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockPrice price) {
				String text = priceFormat.format(price.getBidPrice());
				return StockWatchWidget.this.numberTemplate.cell(text);
			}
		});
		this.addColumn(table, "Ask Price", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockPrice price) {
				String text = priceFormat.format(price.getAskPrice());
				return StockWatchWidget.this.numberTemplate.cell(text);
			}
		});
		this.addColumn(table, "Ask Size", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockPrice price) {
				String text = sizeFormat.format(Integer.valueOf(price
						.getAskSize()));
				return StockWatchWidget.this.numberTemplate.cell(text);
			}
		});

		// Add list data provider to manage the data
		this.displayedPrices.addDataDisplay(table);

		// Subscribe to stock price events
		OfficeFloorComet.subscribe(StockPriceEvents.class,
				new StockPriceEvents() {
					@Override
					public void event(StockPrice stockPrice) {

						// Only add if still watching.
						// (May un-watch while waiting for subscribe response)
						if (!(StockWatchWidget.this.filter.equals(stockPrice
								.getStock()))) {
							return; // Not watching stock
						}

						// Remove/Add stock price to add/update it
						List<StockPrice> stockPrices = StockWatchWidget.this.displayedPrices
								.getList();
						stockPrices.remove(stockPrice);
						stockPrices.add(stockPrice);
					}
				}, null); // TODO add appropriate filtering

		// Add the table
		this.add(table);
	}

	/**
	 * Flags to watch the particular {@link Stock}.
	 * 
	 * @param stock
	 *            {@link Stock}.
	 * @param initialPrice
	 *            Initial {@link StockPrice} to display for the {@link Stock}.
	 */
	public void watchStock(Stock stock, StockPrice initialPrice) {

		// Add stock to display
		this.displayedPrices.getList().add(initialPrice);

		// Take interest in the Stock
		this.filter.addMarketId(stock.getMarketId());
	}

	/**
	 * Flags to stop watching the particular {@link Stock}.
	 * 
	 * @param stock
	 *            {@link Stock} to stop watching.
	 */
	public void unwatchStock(Stock stock) {

		// Stop interest in the Stock
		this.filter.removeMarketId(stock.getMarketId());

		// Remove stock from display
		for (Iterator<StockPrice> iterator = this.displayedPrices.getList()
				.iterator(); iterator.hasNext();) {
			StockPrice price = iterator.next();
			if (price.getStock().equals(stock)) {
				iterator.remove();
			}
		}
	}

	/**
	 * Adds {@link Column} to the {@link CellTable}.
	 * 
	 * @param table
	 *            {@link CellTable}.
	 * @param columnName
	 *            Name of the {@link Column}.
	 * @param columnValue
	 *            {@link ColumnValue} to obtain value for the {@link Column}.
	 * @return Added {@link Column}.
	 */
	private Column<StockPrice, SafeHtml> addColumn(CellTable<StockPrice> table,
			String columnName, final ColumnValue columnValue) {

		// Create the column
		Column<StockPrice, SafeHtml> column = new Column<StockPrice, SafeHtml>(
				new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(StockPrice price) {
				return columnValue.getValue(price);
			}
		};

		// Add header for the column
		SafeHtml header = this.headerTemplate.header(columnName);

		// Add and return the column
		table.addColumn(column, header);
		return column;
	}

}