/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.managedobject.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Tests providing the {@link ExecutionStrategy} to the
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutionStrategyForManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Name of the {@link ExecutionStrategy}.
	 */
	private static final String STRATEGY_NAME = "strategy";

	/**
	 * Ensure can configure {@link ExecutionStrategy} for
	 * {@link ManagedObjectSource}.
	 */
	public void testExecutionStrategy() throws Exception {

		// Obtain the office
		String officeName = this.getOfficeName();

		// Create the managed object
		ManagingOfficeBuilder<None> mo = this
				.constructManagedObject("MO", ExecutionStrategyManagedObjectSource.class, officeName)
				.setManagingOffice(officeName);
		mo.linkExecutionStrategy(0, STRATEGY_NAME);

		// Provide the executive
		this.getOfficeFloorBuilder().setExecutive(MockExecutionSource.class);

		// Open the OfficeFloor (should load strategy)
		MockExecutionSource.strategy = new ThreadFactory[] { this.createMock(ThreadFactory.class) };
		MockExecutionSource.thread = null;
		ExecutionStrategyManagedObjectSource.strategy = null;
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Ensure correct strategy loaded
		assertSame("Incorrect thread strategy", MockExecutionSource.strategy,
				ExecutionStrategyManagedObjectSource.strategy);

		// Ensure provided thread factory uses thread decoration
		this.assertThreadUsed(MockExecutionSource.thread);
	}

	@TestSource
	public static class MockExecutionSource extends AbstractExecutiveSource implements Executive, ExecutionStrategy {

		private static Thread thread;

		private static ThreadFactory[] strategy;

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			thread = context.getThreadFactory().newThread(() -> {
			});

			// Run thread (to allow tear down of used thread)
			thread.start();

			return this;
		}

		@Override
		public ExecutionStrategy[] getExcutionStrategies() {
			return new ExecutionStrategy[] { this };
		}

		@Override
		public String getExecutionStrategyName() {
			return STRATEGY_NAME;
		}

		@Override
		public ThreadFactory[] getThreadFactories() {
			return strategy;
		}
	}

	@TestSource
	public static class ExecutionStrategyManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		private static ThreadFactory[] strategy = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);

			// Configure execution strategy
			context.addExecutionStrategy().setLabel("test");
		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context) throws Exception {

			// Obtain the strategy
			strategy = context.getExecutionStrategy(0);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not be invoked");
			return null;
		}
	}

}