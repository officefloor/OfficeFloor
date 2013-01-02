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
package net.officefloor.demo.stocks;

import net.officefloor.demo.stocks.client.MockMarket;
import net.officefloor.demo.stocks.client.MockMarket.MockStock;
import net.officefloor.demo.stocks.client.Stock;
import net.officefloor.demo.stocks.client.StockPrice;
import net.officefloor.demo.stocks.client.StockPriceEvents;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * <p>
 * Simulates {@link StockPrice} changes in the market.
 * <p>
 * This object should be commented out of the <code>application.objects</code>
 * configuration if intending to use this example with a {@link StockPrice}
 * feed.
 * 
 * @author Daniel Sagenschneider
 */
public class StockPriceSimulatorManagedObjectSource
		extends
		AbstractManagedObjectSource<None, StockPriceSimulatorManagedObjectSource.Flows>
		implements ManagedObject {

	/**
	 * Flow to publish the {@link StockPrice}.
	 */
	public static enum Flows {
		PUBLISH_STOCK_PRICE
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Flows> context;

	/*
	 * ======================= ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required as mock object to simulate stock prices
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Flows> context)
			throws Exception {
		ManagedObjectSourceContext<Flows> mosContext = context
				.getManagedObjectSourceContext();

		// Provide base meta-data
		context.setObjectClass(this.getClass());
		context.setManagedObjectClass(this.getClass());

		// Provide simulation of stock prices
		SimulateStockPriceTask task = new SimulateStockPriceTask();
		context.addFlow(Flows.PUBLISH_STOCK_PRICE, null);
		mosContext.addWork("PUBLISH", task).addTask("TASK", task)
				.setTeam("TEAM");
		mosContext.linkProcess(Flows.PUBLISH_STOCK_PRICE, "PUBLISH", "TASK");

		// Provide dependency to be input managed object
		context.addDependency(StockPriceEvents.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Flows> context)
			throws Exception {
		this.context = context;

		// Trigger first stock price simulation
		this.context.invokeProcess(Flows.PUBLISH_STOCK_PRICE, null, this, 0);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ========================= ManagedObject ==============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this;
	}

	/**
	 * {@link Task} to publish the simulated {@link StockPrice}.
	 */
	private class SimulateStockPriceTask extends
			AbstractSingleTask<SimulateStockPriceTask, None, None> {

		/*
		 * ========================= Task =====================================
		 */

		@Override
		public Object doTask(
				TaskContext<SimulateStockPriceTask, None, None> context)
				throws Throwable {

			// Obtain random numbers for simulation
			double selectionRandomNumber = Math.random();
			double priceRandomNumber = Math.random();

			// Determine stock
			int randomStockIndex = ((int) (selectionRandomNumber * 1000))
					% MockMarket.listedStocks.length;
			MockStock stock = MockMarket.listedStocks[randomStockIndex];

			// Provide random price details for stock
			int bidSize = (int) (selectionRandomNumber * 10000);
			double bidPrice = stock.currentPrice
					+ ((priceRandomNumber / 10) - 0.06);
			if (bidPrice < 0) {
				bidPrice = 0; // ensure not go into negative simulated prices
			}
			int askSize = (int) (priceRandomNumber * 10000);
			double askPrice = bidPrice + 0.01;
			StockPrice stockPrice = new StockPrice(bidSize, bidPrice, askSize,
					askPrice, System.currentTimeMillis());

			// Determine if reset base price
			long currentTime = System.currentTimeMillis();
			if (stock.lastResetTime < 0) {
				stock.lastResetTime = currentTime;
			}
			if ((stock.lastResetTime + (24 * 60 * 60 * 1000)) > currentTime) {
				// Reset price back to base price
				stock.currentPrice = stock.basePrice;
			} else {
				// Update price for stock for next change
				stock.currentPrice = askPrice;
			}

			// Publish the stock price
			JeeStockPricePublisherWorkSource.publishStockPrice(new Stock(
					stock.marketId, stock.name), stockPrice);

			// Trigger next price
			// (+500 to conserve on example's running costs)
			long nextPriceDelay = (long) (priceRandomNumber * 1000) + 500;
			StockPriceSimulatorManagedObjectSource.this.context
					.invokeProcess(Flows.PUBLISH_STOCK_PRICE, null,
							StockPriceSimulatorManagedObjectSource.this,
							nextPriceDelay);

			// Flow complete so no parameter for next task
			return null;
		}
	}

}