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

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.demo.stocks.client.Stock;
import net.officefloor.demo.stocks.client.StockPrice;
import net.officefloor.demo.stocks.client.StockPriceEvents;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeAwareWorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * <p>
 * Provides ability to publish a {@link StockPrice}.
 * <p>
 * This is provided to allow access for JEE application to publish
 * {@link StockPrice} instances. If building with stand-alone server then may
 * just use dependency injection to obtain the {@link StockPriceEvents} to
 * publish a {@link StockPrice}.
 * 
 * @author Daniel Sagenschneider
 */
public class JeeStockPricePublisherWorkSource
		extends
		AbstractWorkSource<JeeStockPricePublisherWorkSource.JeeStockPricePublisherTask> {

	/**
	 * Use this method to publish a {@link StockPrice} to all clients interested
	 * in the {@link StockPrice} from within JEE application code.
	 * 
	 * @param stock
	 *            {@link Stock}.
	 * @param price
	 *            {@link StockPrice} for the {@link Stock}.
	 */
	public static void publishStockPrice(Stock stock, StockPrice price) {
		try {
			publishTask.invokeTask(new StockUpdate(stock, price));
		} catch (InvalidParameterTypeException ex) {
			// Only invalid if this class is itself invalid
			throw new IllegalStateException(
					"Publish task must be configured for "
							+ StockPrice.class.getSimpleName(), ex);
		}
	}

	/**
	 * {@link Task} reference to allow external invocation to publish the
	 * {@link StockPrice}.
	 */
	private static TaskManager publishTask;

	/**
	 * Dependency to publish the {@link StockPrice}.
	 */
	public static enum Dependency {
		STOCK_UPDATE, STOCK_PRICE_EVENT
	}

	/*
	 * ========================= WorkSource ==============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties as wrapping call to publish event
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<JeeStockPricePublisherTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Configure the task to publish the StockPrice event
		JeeStockPricePublisherTask factory = new JeeStockPricePublisherTask();
		workTypeBuilder.setWorkFactory(factory);
		TaskTypeBuilder<Dependency, None> task = workTypeBuilder.addTaskType(
				"PUBLISH", factory, Dependency.class, None.class);
		task.addObject(StockUpdate.class).setKey(Dependency.STOCK_UPDATE);
		task.addObject(StockPriceEvents.class).setKey(
				Dependency.STOCK_PRICE_EVENT);
		task.setDifferentiator(this);
	}

	/**
	 * {@link Task} to publish the {@link StockPrice}.
	 */
	public class JeeStockPricePublisherTask extends
			AbstractSingleTask<JeeStockPricePublisherTask, Dependency, None>
			implements OfficeAwareWorkFactory<JeeStockPricePublisherTask> {

		/*
		 * ========================== Task ==============================
		 */

		@Override
		public void setOffice(Office office) throws Exception {

			// Find the task within the Office (to call externally)
			for (String workName : office.getWorkNames()) {
				WorkManager work = office.getWorkManager(workName);
				for (String taskName : work.getTaskNames()) {
					TaskManager task = work.getTaskManager(taskName);
					if (task.getDifferentiator() == JeeStockPricePublisherWorkSource.this) {
						// Found the task
						JeeStockPricePublisherWorkSource.publishTask = task;
					}
				}
			}
		}

		@Override
		public Object doTask(
				TaskContext<JeeStockPricePublisherTask, Dependency, None> context)
				throws Throwable {

			// Obtain dependencies
			StockUpdate stockUpdate = (StockUpdate) context
					.getObject(Dependency.STOCK_UPDATE);
			StockPriceEvents events = (StockPriceEvents) context
					.getObject(Dependency.STOCK_PRICE_EVENT);

			// Publish the stock price event
			events.event(stockUpdate.price, stockUpdate.stock);

			// Published
			return null;
		}
	}

	/**
	 * {@link StockPrice} update for a {@link Stock}.
	 */
	public static class StockUpdate {

		/**
		 * {@link Stock}.
		 */
		public final Stock stock;

		/**
		 * {@link StockPrice}.
		 */
		public final StockPrice price;

		/**
		 * Initiate.
		 * 
		 * @param stock
		 *            {@link Stock}.
		 * @param price
		 *            {@link StockPrice}.
		 */
		public StockUpdate(Stock stock, StockPrice price) {
			this.stock = stock;
			this.price = price;
		}
	}

}