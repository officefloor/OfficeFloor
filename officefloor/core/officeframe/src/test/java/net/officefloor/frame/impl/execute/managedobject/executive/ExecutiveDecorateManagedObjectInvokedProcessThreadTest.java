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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensures the {@link Executive} can decorate the {@link Thread} invoking a
 * {@link Process} via the {@link ManagedObjectExecuteContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveDecorateManagedObjectInvokedProcessThreadTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can decorate the inbound {@link Thread}.
	 */
	public void testDecorateInboundThread() throws Throwable {

		// Obtain the office
		String officeName = this.getOfficeName();

		// Create the managed object
		this.constructManagedObject("MO", ThreadDecorateManagedObjectSource.class, officeName)
				.setManagingOffice(officeName).setInputManagedObjectName("MO");

		// Provide the executive
		this.getOfficeFloorBuilder().setExecutive(MockExecutionSource.class);

		// Open the OfficeFloor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Reset
		MockExecutionSource.executionThread = null;
		MockExecutionSource.markThread.set(null);
		ThreadDecorateManagedObjectSource.isInvokeProcess = false;

		// Invoke the process
		ThreadDecorateManagedObjectSource.invokeProcess();

		// Ensure registered
		assertNotNull("Should be registered", MockExecutionSource.executionThread);
		assertSame("Incorrect inbound thread", Thread.currentThread(), MockExecutionSource.executionThread);

		// Ensure not invoke (as intercepted)
		assertFalse("Should not yet execute managed function", ThreadDecorateManagedObjectSource.isInvokeProcess);

		// Undertake the execution
		MockExecutionSource.markThread.get().execute();

		// Should now be invoked
		assertTrue("Should now have executed", ThreadDecorateManagedObjectSource.isInvokeProcess);
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
		public <T extends Throwable> ProcessManager manageExecution(Execution<T> execution) throws T {

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

		private static ManagedObjectExecuteContext<InvokeProcess> context;

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
			ThreadDecorateManagedObjectSource.context = context;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not be invoked");
			return null;
		}
	}

}