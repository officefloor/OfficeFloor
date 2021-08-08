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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Ensures the {@link Executive} can decorate the {@link Thread} invoking a
 * {@link Process} via the {@link ManagedObjectExecuteContext}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ExecutiveDecorateManagedObjectInvokedProcessThreadTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure can decorate the inbound {@link Thread}.
	 */
	@Test
	public void decorateInboundThread() throws Throwable {

		// Obtain the office
		String officeName = this.construct.getOfficeName();

		// Create the managed object
		this.construct.constructManagedObject("MO", ThreadDecorateManagedObjectSource.class, officeName)
				.setManagingOffice(officeName).setInputManagedObjectName("MO");

		// Provide the executive
		this.construct.getOfficeFloorBuilder().setExecutive(MockExecutionSource.class);

		// Open the OfficeFloor
		MockExecutionSource.isOpenning = true;
		OfficeFloor officeFloor = this.construct.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Reset
		MockExecutionSource.executionThread = null;
		MockExecutionSource.markThread.set(null);
		ThreadDecorateManagedObjectSource.isInvokeProcess = false;

		// Invoke the process
		MockExecutionSource.isOpenning = false;
		ThreadDecorateManagedObjectSource.invokeProcess();

		// Ensure registered
		assertNotNull(MockExecutionSource.executionThread, "Should be registered");
		assertSame(Thread.currentThread(), MockExecutionSource.executionThread, "Incorrect inbound thread");

		// Ensure not invoke (as intercepted)
		assertFalse(ThreadDecorateManagedObjectSource.isInvokeProcess, "Should not yet execute managed function");

		// Undertake the execution
		MockExecutionSource.markThread.get().execute();

		// Should now be invoked
		assertTrue(ThreadDecorateManagedObjectSource.isInvokeProcess, "Should now have executed");
	}

	@TestSource
	public static class MockExecutionSource extends DefaultExecutive implements ExecutionStrategy {

		private static volatile boolean isOpenning = true;

		private static Thread executionThread = null;

		private static final ThreadLocal<Execution<? extends Throwable>> markThread = new ThreadLocal<>();

		/*
		 * ================ Executive =======================
		 */

		@Override
		public <T extends Throwable> ProcessManager manageExecution(Execution<T> execution) throws T {

			// Determine if opening
			if (isOpenning) {
				execution.execute();

			} else {
				// Capture the execution thread
				executionThread = Thread.currentThread();

				// Provide detail on the thread
				markThread.set(execution);
			}

			// Should not use process manager
			return () -> fail("Should not cancel process");
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

	public static enum InvokeProcess {
		INVOKE
	}

	@TestSource
	public static class ThreadDecorateManagedObjectSource extends AbstractManagedObjectSource<None, InvokeProcess> {

		/**
		 * Invokes the {@link ProcessState}.
		 */
		private static void invokeProcess() {
			context.invokeProcess(InvokeProcess.INVOKE, null, () -> new Object(), 0, null);
		}

		private static ManagedObjectServiceContext<InvokeProcess> context;

		private static boolean isInvokeProcess = false;

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, InvokeProcess> context) throws Exception {
			context.setObjectClass(Object.class);

			// Configure the process
			context.getManagedObjectSourceContext().addManagedFunction("PROCESS", () -> (c) -> isInvokeProcess = true);
			context.addFlow(InvokeProcess.INVOKE, null);
			context.getManagedObjectSourceContext().getFlow(InvokeProcess.INVOKE).linkFunction("PROCESS");
		}

		@Override
		public void start(ManagedObjectExecuteContext<InvokeProcess> context) throws Exception {
			ThreadDecorateManagedObjectSource.context = new SafeManagedObjectService<>(context);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not be invoked");
			return null;
		}
	}

}
