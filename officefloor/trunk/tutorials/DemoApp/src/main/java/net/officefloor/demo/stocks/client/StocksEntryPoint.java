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

import net.officefloor.demo.stocks.client.MockMarket.MockStock;
import net.officefloor.plugin.gwt.comet.api.OfficeFloorComet;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * {@link EntryPoint} for the application.
 * 
 * @author Daniel Sagenschneider
 */
public class StocksEntryPoint implements EntryPoint {

	/*
	 * ==================== EntryPoint ============================
	 */

	@Override
	public void onModuleLoad() {

		// Report errors
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void onUncaughtException(Throwable e) {
				Window.alert("ERROR: " + (e == null ? "NULL" : e.getMessage())
						+ " [" + (e == null ? "null" : e.getClass().getName())
						+ "]");
			}
		});

		// Subscribe when setup
		OfficeFloorComet.setMultipleSubscriptions(true);

		// Add the stock watch widget
		RootPanel watchPanel = RootPanel.get("StockWatch");
		StockWatchWidget stockWatch = new StockWatchWidget(10);
		watchPanel.add(stockWatch);

		// Watch all stocks
		for (MockStock stock : MockMarket.listedStocks) {
			stockWatch.watchStock(new Stock(stock.marketId, stock.name),
					new StockPrice(100, stock.basePrice - 1, 200,
							stock.basePrice, 0));
		}

		// Add the stock graph widget
		RootPanel graphPanel = RootPanel.get("StockGraph");
		Stock[] stocks = new Stock[MockMarket.listedStocks.length];
		for (int i = 0; i < stocks.length; i++) {
			MockStock mockStock = MockMarket.listedStocks[i];
			stocks[i] = new Stock(mockStock.marketId, mockStock.name);
		}
		StockGraphWidget stockGraph = new StockGraphWidget((2 * 60 * 1000),
				(60 * 1000), "27em", "10em", stocks);
		graphPanel.add(stockGraph);

		// Subscribe to stock price events
		OfficeFloorComet.subscribe();
	}

}