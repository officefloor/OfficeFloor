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

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.officefloor.plugin.gwt.comet.api.OfficeFloorComet;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.CellTable.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.IsSerializable;
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
	private final ListDataProvider<StockEntry> displayedPrices = new ListDataProvider<StockEntry>();

	/**
	 * {@link StockInterest}.
	 */
	private final StockInterest filter = new StockInterest();

	/**
	 * Template to render the {@link CellTable} headers.
	 */
	static interface HeaderTemplate extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<span class=\"StockWatchHeader\">{0}</span>")
		SafeHtml header(String text);
	}

	private final HeaderTemplate headerTemplate = GWT
			.create(HeaderTemplate.class);

	/**
	 * Template to render the {@link Stock} name.
	 */
	static interface NameTemplate extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<span class=\"StockWatchName\">{0}</span>")
		SafeHtml cell(String text);
	}

	private final NameTemplate nameTemplate = GWT.create(NameTemplate.class);

	/**
	 * Template to render the {@link StockPrice} change.
	 */
	static interface ChangeTemplate extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<span class=\"{0} {2}\">{1}</span>")
		SafeHtml cell(String cssClass, String text, String highlightCssClass);
	}

	private final ChangeTemplate changeTemplate = GWT
			.create(ChangeTemplate.class);

	/**
	 * Template to render the {@link StockPrice} size/price.
	 */
	static interface NumberTemplate extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<span class=\"StockWatchNumber {1}\">{0}</span>")
		SafeHtml cell(String text, String highlightCssClass);
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
		 * @param entry
		 *            {@link StockEntry}.
		 * @return {@link SafeHtml}.
		 */
		SafeHtml getValue(StockEntry entry);
	}

	/**
	 * Initiate.
	 * 
	 * @param pageSize
	 *            Number of {@link Stock} instances displayed per page.
	 */
	public StockWatchWidget(int pageSize) {

		// Locale specific formatters
		final DateTimeFormat timeFormat = DateTimeFormat
				.getFormat(PredefinedFormat.HOUR24_MINUTE_SECOND);
		final NumberFormat sizeFormat = NumberFormat.getDecimalFormat();
		final NumberFormat priceFormat = NumberFormat.getFormat("0.00");
		final NumberFormat percentFormat = NumberFormat.getFormat("0.00");

		// Configure table
		final CellTable<StockEntry> table = new CellTable<StockEntry>(pageSize,
				new StockWatchResource());
		this.addColumn(table, "Name", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockEntry entry) {
				String text = entry.getStock().getName();
				return StockWatchWidget.this.nameTemplate.cell(text);
			}
		});
		this.addColumn(table, "Open", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockEntry entry) {
				String text = priceFormat.format(entry.getOpenPrice());
				return StockWatchWidget.this.numberTemplate.cell(text, "");
			}
		});
		this.addColumn(table, "Time", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockEntry entry) {
				String text = timeFormat.format(new Date(entry.getStockPrice()
						.getTimestamp()));
				return StockWatchWidget.this.numberTemplate.cell(text,
						entry.getHighlightCssClass());
			}
		});
		this.addColumn(table, "Change", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockEntry entry) {
				double change = entry.getOpenPercentageChange() * 100;
				String text = percentFormat.format(change) + "%";
				String cssClass;
				if (change < 0) {
					cssClass = "StockWatchChangeNegative";
				} else {
					text = "+" + text;
					cssClass = "StockWatchChangePositive";
				}
				return StockWatchWidget.this.changeTemplate.cell(cssClass,
						text, entry.getHighlightCssClass());
			}
		});
		this.addColumn(table, "Size", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockEntry entry) {
				String text = sizeFormat.format(Integer.valueOf(entry
						.getStockPrice().getBidSize()));
				return StockWatchWidget.this.numberTemplate.cell(text,
						entry.getHighlightCssClass());
			}
		});
		this.addColumn(table, "Bid", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockEntry entry) {
				String text = priceFormat.format(entry.getStockPrice()
						.getBidPrice());
				return StockWatchWidget.this.numberTemplate.cell(text,
						entry.getHighlightCssClass());
			}
		});
		this.addColumn(table, "Ask", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockEntry entry) {
				String text = priceFormat.format(entry.getStockPrice()
						.getAskPrice());
				return StockWatchWidget.this.numberTemplate.cell(text,
						entry.getHighlightCssClass());
			}
		});
		this.addColumn(table, "Size", new ColumnValue() {
			@Override
			public SafeHtml getValue(StockEntry entry) {
				String text = sizeFormat.format(Integer.valueOf(entry
						.getStockPrice().getAskSize()));
				return StockWatchWidget.this.numberTemplate.cell(text,
						entry.getHighlightCssClass());
			}
		});

		// Add list data provider to manage the data
		this.displayedPrices.addDataDisplay(table);

		// Subscribe to stock price events
		OfficeFloorComet.subscribe(StockPriceEvents.class,
				new StockPriceEvents() {
					@Override
					public void event(StockPrice price, Stock stock) {

						// Remove/Add stock price to add/update it
						List<StockEntry> stockEntries = StockWatchWidget.this.displayedPrices
								.getList();

						// Find the stock entry
						StockEntry stockEntry = null;
						for (StockEntry entry : stockEntries) {
							if (entry.getStock().getMarketId()
									.equals(stock.getMarketId())) {
								stockEntry = entry;
								break; // found entry
							}
						}
						if (stockEntry == null) {
							return; // stock not being watched
						}

						// Update the price
						stockEntry.setCurrentPrice(price);

						// Refresh the rows for the new price
						StockWatchWidget.this.displayedPrices.refresh();

						// Sort the rows
						StockWatchWidget.this.sortRows();
					}
				}, this.filter);

		// Add the table
		this.add(table);
	}

	/**
	 * Flags to watch the particular {@link Stock}.
	 * 
	 * @param stock
	 *            {@link Stock}.
	 * @param openPrice
	 *            Open {@link StockPrice} for the {@link Stock}.
	 */
	public void watchStock(Stock stock, StockPrice openPrice) {

		// Add stock to display
		this.displayedPrices.getList().add(new StockEntry(stock, openPrice));
		this.sortRows();

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
		for (Iterator<StockEntry> iterator = this.displayedPrices.getList()
				.iterator(); iterator.hasNext();) {
			StockEntry entry = iterator.next();
			if (entry.getStock().equals(stock)) {
				iterator.remove();
			}
		}
	}

	/**
	 * Sorts the rows.
	 */
	private void sortRows() {
		Collections.sort(this.displayedPrices.getList(),
				new Comparator<StockEntry>() {
					@Override
					public int compare(StockEntry a, StockEntry b) {
						return String.CASE_INSENSITIVE_ORDER.compare(a
								.getStock().getName(), b.getStock().getName());
					}
				});
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
	private Column<StockEntry, SafeHtml> addColumn(CellTable<StockEntry> table,
			String columnName, final ColumnValue columnValue) {

		// Create the column
		Column<StockEntry, SafeHtml> column = new Column<StockEntry, SafeHtml>(
				new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(StockEntry entry) {
				return columnValue.getValue(entry);
			}
		};

		// Add header for the column
		SafeHtml header = this.headerTemplate.header(columnName);

		// Add and return the column
		table.addColumn(column, header);
		return column;
	}

	/**
	 * Interest in the {@link Stock} used to subscribe to only the specified
	 * {@link Stock} instances.
	 */
	public static class StockInterest implements IsSerializable {

		/**
		 * Market identifiers that have interest in.
		 */
		private Set<String> marketIdentifiers = new HashSet<String>();

		/**
		 * Adds a market identifier.
		 * 
		 * @param marketId
		 *            Market identifier.
		 */
		public void addMarketId(String marketId) {
			this.marketIdentifiers.add(marketId);
		}

		/**
		 * Removes the market identifier.
		 * 
		 * @param marketId
		 *            Market identifier.
		 */
		public void removeMarketId(String marketId) {
			this.marketIdentifiers.remove(marketId);
		}

		/*
		 * ====================== Object ======================
		 */

		@Override
		public boolean equals(Object obj) {

			// Should be matched against a Stock for interest filtering
			if (!(obj instanceof Stock)) {
				return false;
			}
			Stock stock = (Stock) obj;

			// Watch all stocks if none specified
			if (this.marketIdentifiers.size() == 0) {
				return true;
			}

			// Equal if stock is of interest
			return this.marketIdentifiers.contains(stock.getMarketId());
		}
	}

	/**
	 * {@link Stock} entry within the {@link CellTable}.
	 */
	private class StockEntry {

		/**
		 * {@link Stock}.
		 */
		private final Stock stock;

		/**
		 * Open {@link StockPrice}.
		 */
		private final StockPrice openPrice;

		/**
		 * Last {@link StockPrice}.
		 */
		private StockPrice lastPrice;

		/**
		 * Current {@link StockPrice}.
		 */
		private StockPrice currentPrice;

		/**
		 * Highlight CSS class.
		 */
		private String highlightCssClass = "";

		/**
		 * Initiate.
		 * 
		 * @param stock
		 *            {@link Stock}.
		 * @param openPrice
		 *            Open {@link StockPrice}.
		 */
		public StockEntry(Stock stock, StockPrice openPrice) {
			this.stock = stock;
			this.openPrice = openPrice;
			this.lastPrice = openPrice;
			this.currentPrice = openPrice;
		}

		/**
		 * Updates the current {@link StockPrice}.
		 * 
		 * @param price
		 *            Current {@link StockPrice}.
		 */
		public void setCurrentPrice(StockPrice price) {
			this.lastPrice = this.currentPrice;
			this.currentPrice = price;

			// Provide highlight for change
			double lastPriceChange = this.getLastPriceChange();
			this.highlightCssClass = (lastPriceChange >= 0 ? "StockWatchHighlightPositive"
					: "StockWatchHighlightNegative");

			// Clear highlight after a moment
			new Timer() {
				@Override
				public void run() {
					// Reset highlight
					StockEntry.this.highlightCssClass = "";

					// Refresh display for no highlight
					StockWatchWidget.this.displayedPrices.refresh();
				}
			}.schedule(1000);
		}

		/**
		 * Obtains the highlight CSS class.
		 * 
		 * @return Highlight CSS class.
		 */
		public String getHighlightCssClass() {
			return this.highlightCssClass;
		}

		/**
		 * Obtains the {@link Stock}.
		 * 
		 * @return {@link Stock}.
		 */
		public Stock getStock() {
			return this.stock;
		}

		/**
		 * Obtains the open ask price.
		 * 
		 * @return Open ask price.
		 */
		public double getOpenPrice() {
			return this.openPrice.getAskPrice();
		}

		/**
		 * Obtains the percentage change in ask price since market open.
		 * 
		 * @return Percentage change in ask price since market open.
		 */
		public double getOpenPercentageChange() {
			StockPrice price = this.getStockPrice();
			double openAskPrice = this.openPrice.getAskPrice();
			double change = price.getAskPrice() - openAskPrice;
			double percentageChange = change / openAskPrice;
			return percentageChange;
		}

		/**
		 * Obtains the change in ask price since last price.
		 * 
		 * @return Change in ask price since last price.
		 */
		public double getLastPriceChange() {
			return (this.currentPrice.getAskPrice() - this.lastPrice
					.getAskPrice());
		}

		/**
		 * Obtains the {@link StockPrice}.
		 * 
		 * @return {@link StockPrice}.
		 */
		public StockPrice getStockPrice() {
			return this.currentPrice;
		}
	}

	/**
	 * Allow CSS styling of the {@link CellTable}.
	 */
	private static class StockWatchResource implements Resources, ImageResource {

		/**
		 * {@link StockWatchStyle}.
		 */
		private final Style style = new StockWatchStyle();

		/*
		 * ================== Resources =======================
		 */

		@Override
		public ImageResource cellTableFooterBackground() {
			return this;
		}

		@Override
		public ImageResource cellTableHeaderBackground() {
			return this;
		}

		@Override
		public ImageResource cellTableLoading() {
			return this;
		}

		@Override
		public ImageResource cellTableSelectedBackground() {
			return this;
		}

		@Override
		public ImageResource cellTableSortAscending() {
			return this;
		}

		@Override
		public ImageResource cellTableSortDescending() {
			return this;
		}

		@Override
		public Style cellTableStyle() {
			return this.style;
		}

		/*
		 * ================== ImageResource ===========================
		 */

		@Override
		public String getName() {
			return "";
		}

		@Override
		public int getHeight() {
			return 0;
		}

		@Override
		public int getLeft() {
			return 0;
		}

		@Override
		public SafeUri getSafeUri() {
			return null;
		}

		@Override
		public int getTop() {
			return 0;
		}

		@Override
		public String getURL() {
			return "";
		}

		@Override
		public int getWidth() {
			return 0;
		}

		@Override
		public boolean isAnimated() {
			return false;
		}
	}

	/**
	 * Stock watch {@link Style}.
	 */
	private static class StockWatchStyle implements Style {

		/**
		 * CSS class for <code>th</code>.
		 */
		private static final String TH_CSS_CLASS = "StockWatchTh";

		/**
		 * CSS class for <code>td</code>.
		 */
		private static final String TD_CSS_CLASS = "StockWatchTd";

		/**
		 * CSS class for the first column.
		 */
		private static final String FIRST_COLUMN_CSS_CLASS = "StockWatchFirstColumn";

		/**
		 * CSS class for odd row.
		 */
		private static final String ODD_ROW_CSS_CLASS = "StockWatchOddRow";

		/**
		 * CSS class for even row.
		 */
		private static final String EVEN_ROW_CSS_CLASS = "StockWatchEvenRow";

		/*
		 * ===================== Style ==========================
		 */

		@Override
		public boolean ensureInjected() {
			return false;
		}

		@Override
		public String getText() {
			return TD_CSS_CLASS;
		}

		@Override
		public String getName() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableCell() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableEvenRow() {
			return EVEN_ROW_CSS_CLASS;
		}

		@Override
		public String cellTableEvenRowCell() {
			return EVEN_ROW_CSS_CLASS;
		}

		@Override
		public String cellTableFirstColumn() {
			return FIRST_COLUMN_CSS_CLASS;
		}

		@Override
		public String cellTableFirstColumnFooter() {
			return FIRST_COLUMN_CSS_CLASS;
		}

		@Override
		public String cellTableFirstColumnHeader() {
			return FIRST_COLUMN_CSS_CLASS;
		}

		@Override
		public String cellTableFooter() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableHeader() {
			return TH_CSS_CLASS;
		}

		@Override
		public String cellTableHoveredRow() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableHoveredRowCell() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableKeyboardSelectedCell() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableKeyboardSelectedRow() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableKeyboardSelectedRowCell() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableLastColumn() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableLastColumnFooter() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableLastColumnHeader() {
			return TH_CSS_CLASS;
		}

		@Override
		public String cellTableLoading() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableOddRow() {
			return ODD_ROW_CSS_CLASS;
		}

		@Override
		public String cellTableOddRowCell() {
			return ODD_ROW_CSS_CLASS;
		}

		@Override
		public String cellTableSelectedRow() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableSelectedRowCell() {
			return TD_CSS_CLASS;
		}

		@Override
		public String cellTableSortableHeader() {
			return TH_CSS_CLASS;
		}

		@Override
		public String cellTableSortedHeaderAscending() {
			return TH_CSS_CLASS;
		}

		@Override
		public String cellTableSortedHeaderDescending() {
			return TH_CSS_CLASS;
		}

		@Override
		public String cellTableWidget() {
			return "StockWatchTable";
		}
	}

}