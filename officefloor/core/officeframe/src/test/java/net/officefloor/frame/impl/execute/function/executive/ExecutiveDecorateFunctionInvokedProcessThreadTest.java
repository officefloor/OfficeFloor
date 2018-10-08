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
package net.officefloor.frame.impl.execute.function.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensures the {@link Executive} can decorate the {@link Thread} invoking a
 * {@link Process} via the {@link FunctionManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveDecorateFunctionInvokedProcessThreadTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can decorate the inbound {@link Thread}.
	 */
	public void testDecorateInboundThread() throws Throwable {

		// Create the function
		TestWork work = new TestWork();
		this.constructFunction(work, "function");

		// Provide the executive
		this.getOfficeFloorBuilder().setExecutive(MockExecutionSource.class);

		// Reset
		MockExecutionSource.executionThread = null;
		MockExecutionSource.markThread.set(null);

		// Trigger the function
		this.triggerFunction("function", null, null);

		// Ensure registered
		assertNotNull("Should be registered", MockExecutionSource.executionThread);
		assertSame("Incorrect inbound thread", Thread.currentThread(), MockExecutionSource.executionThread);

		// Ensure not invoke (as intercepted)
		assertFalse("Should not yet execute function", work.isFunctionInvoked);

		// Undertake the execution
		MockExecutionSource.markThread.get().execute();

		// Should now be invoked
		assertTrue("Should now have executed", work.isFunctionInvoked);
	}

	@TestSource
	public static class MockExecutionSource extends AbstractExecutiveSource implements Executive, ExecutionStrategy {

		private static Thread executionThread = null;

		private static final ThreadLocal<Execution<? extends Throwable>> markThread = new ThreadLocal<>();

		/*
		 * =============== ExecutiveSource ==================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			return this;
		}

		/*
		 * ================ Executive =======================
		 */

		@Override
		public <T extends Throwable> void manageExecution(Execution<T> execution) throws T {

			// Capture the execution thread
			executionThread = Thread.currentThread();

			// Provide detail on the thread
			markThread.set(execution);
		}

		@Override
		public ExecutionStrategy[] getExcutionStrategies() {
			return new ExecutionStrategy[] { this };
		}

		/*
		 * ============= ExecutionStrategy ===================
		 */

		@Override
		public String getExecutionStrategyName() {
			return "TEST";
		}

		@Override
		public ThreadFactory[] getThreadFactories() {
			return new ThreadFactory[] { (runnable) -> new Thread(runnable) };
		}
	}

	public class TestWork {

		public volatile boolean isFunctionInvoked = false;

		public void function() {
			this.isFunctionInvoked = true;
		}
	}

}