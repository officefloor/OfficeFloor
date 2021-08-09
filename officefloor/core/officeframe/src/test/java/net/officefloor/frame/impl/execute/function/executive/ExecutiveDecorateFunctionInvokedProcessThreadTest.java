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

package net.officefloor.frame.impl.execute.function.executive;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensures the {@link Executive} can decorate the {@link Thread} invoking a
 * {@link Process} via the {@link FunctionManager}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ExecutiveDecorateFunctionInvokedProcessThreadTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure can decorate the inbound {@link Thread}.
	 */
	@Test
	public void decorateInboundThread() throws Throwable {

		// Create the function
		TestWork work = new TestWork();
		this.construct.constructFunction(work, "function");

		// Provide the executive
		this.construct.getOfficeFloorBuilder().setExecutive(MockExecutionSource.class);

		// Open the OfficeFloor (allows start up processes to be run)
		MockExecutionSource.isOpeningOfficeFloor = true;
		this.construct.constructOfficeFloor().openOfficeFloor();

		// Reset
		MockExecutionSource.executionThread = null;
		MockExecutionSource.markThread.set(null);

		// Trigger the function
		MockExecutionSource.isOpeningOfficeFloor = false;
		this.construct.triggerFunction("function", null, null);

		// Ensure registered
		assertNotNull(MockExecutionSource.executionThread, "Should be registered");
		assertSame(Thread.currentThread(), MockExecutionSource.executionThread, "Incorrect inbound thread");

		// Ensure not invoke (as intercepted)
		assertFalse(work.isFunctionInvoked, "Should not yet execute function");

		// Undertake the execution
		MockExecutionSource.markThread.get().execute();

		// Should now be invoked
		assertTrue(work.isFunctionInvoked, "Should now have executed");
	}

	@TestSource
	public static class MockExecutionSource extends DefaultExecutive implements ExecutionStrategy {

		private static boolean isOpeningOfficeFloor = true;

		private static Thread executionThread = null;

		private static final ThreadLocal<Execution<? extends Throwable>> markThread = new ThreadLocal<>();

		/*
		 * =============== ExecutiveSource ==================
		 */

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			assertEquals("Executive", context.getLogger().getName(), "Incorrect logger name");
			return this;
		}

		/*
		 * ================ Executive =======================
		 */

		@Override
		public <T extends Throwable> ProcessManager manageExecution(Execution<T> execution) throws T {

			// Determine if opening processes
			if (isOpeningOfficeFloor) {
				return execution.execute();
			}

			// Capture the execution thread
			executionThread = Thread.currentThread();

			// Provide detail on the thread
			markThread.set(execution);

			// Should not use process manager
			return () -> {
				fail("Should not cancel process");
			};
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
