/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.managedobject.executive;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
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
	 * Ensure can configure default {@link ExecutionStrategy} for
	 * {@link ManagedObjectSource}.
	 */
	public void testDefaultExecutionStrategy() throws Exception {

		// Obtain the office
		String officeName = this.getOfficeName();

		// Create the managed object
		this.constructManagedObject("MO", ExecutionStrategyManagedObjectSource.class, officeName)
				.setManagingOffice(officeName);

		// Open the OfficeFloor (should load default strategy)
		ExecutionStrategyManagedObjectSource.strategy = null;
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Ensure have default execution strategy
		assertNotNull("Should load default execution strategy", ExecutionStrategyManagedObjectSource.strategy);
	}

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
		MockExecutionSource.executedThreads.set(0);
		ExecutionStrategyManagedObjectSource.strategy = null;
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Ensure correct strategy loaded
		assertSame("Incorrect thread strategy", MockExecutionSource.strategy,
				ExecutionStrategyManagedObjectSource.strategy);

		// Ensure provided thread factory uses thread decoration
		this.assertThreadUsed(MockExecutionSource.thread);

		// Ensure both threads execute correctly
		this.waitForTrue(() -> MockExecutionSource.executedThreads.get() == 2);
	}

	@TestSource
	public static class MockExecutionSource extends DefaultExecutive implements ExecutionStrategy {

		private static Thread thread;

		private static ThreadFactory[] strategy;

		private static AtomicInteger executedThreads = new AtomicInteger(0);

		/*
		 * =============== ExecutiveSource ==================
		 */

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {

			// Ensure can run thread without executive
			Thread noExecutiveThread = context.createThreadFactory("TEST", null)
					.newThread(() -> executedThreads.incrementAndGet());
			assertEquals("Incorrect no executive thread name", "TEST-1", noExecutiveThread.getName());
			noExecutiveThread.start(); // run to allow tear down

			// Ensure can create thread with executive
			thread = context.createThreadFactory("TEST", this).newThread(() -> executedThreads.incrementAndGet());
			assertEquals("Incorrect thread name", "TEST-1", thread.getName());
			thread.start(); // run to allow tear down

			// Provide executive
			return this;
		}

		/*
		 * ================ Executive =======================
		 */

		@Override
		public ExecutionStrategy[] getExcutionStrategies() {
			return new ExecutionStrategy[] { this };
		}

		/*
		 * ============== ExecutionStrategy =================
		 */

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
