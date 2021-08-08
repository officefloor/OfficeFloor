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

package net.officefloor.frame.impl.execute.managedexecution;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.frame.internal.structure.ManagedExecution;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Tests that external {@link Thread} instances are wrapped with
 * {@link ManagedExecution} for the {@link OfficeFloor}.
 */
public class ManagedExecutionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link FunctionManager} executes within {@link ManagedExecution}
	 * context.
	 */
	public void testFunctionManager() throws Exception {

		// Construct the function
		MockFunction function = new MockFunction();
		this.constructFunction(function, "function");

		// Ensure not in managed execution
		assertFalse("Should not be managed execution", ManagedExecutionFactoryImpl.isCurrentThreadManaged());

		// Ensure construction does not alter thread execution state
		String officeName = this.getOfficeName();
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Execute the function (to ensure managed execution)
		FunctionManager functionManager = officeFloor.getOffice(officeName).getFunctionManager("function");
		assertFalse("Should still not be managed execution", ManagedExecutionFactoryImpl.isCurrentThreadManaged());
		boolean[] isCallbackManaged = new boolean[] { false };
		functionManager.invokeProcess(null, (escalation) -> {
			assertNull("Should be escalation", escalation);
			isCallbackManaged[0] = ManagedExecutionFactoryImpl.isCurrentThreadManaged();
		});
		assertTrue("Function should be execution managed", function.isManaged);
		assertTrue("Callback should be execution managed", isCallbackManaged[0]);

		// Should no longer be managed
		assertFalse("Should stop managing execution", ManagedExecutionFactoryImpl.isCurrentThreadManaged());
	}

	public static class MockFunction {

		private boolean isManaged = false;

		public void function() {
			this.isManaged = ManagedExecutionFactoryImpl.isCurrentThreadManaged();
		}
	}

	/**
	 * Ensures {@link ManagedObjectExecuteContext} executes within
	 * {@link ManagedExecution} context.
	 */
	public void testManagedObjectExecution() throws Exception {

		String officeName = this.getOfficeName();
		MockThreadCompletionListener completion = new MockThreadCompletionListener();

		// Construct the functions
		MockObjectFunctions functions = new MockObjectFunctions(completion);
		this.constructFunction(functions, "function");

		// Construct the managed object
		MockManagedObjectSource mo = new MockManagedObjectSource();
		ManagedObjectBuilder<Indexed> moBuilder = this.constructManagedObject("MO", mo, null);
		ManagingOfficeBuilder<Indexed> managingOffice = moBuilder.setManagingOffice(officeName);
		managingOffice.linkFlow(0, "function");
		managingOffice.setInputManagedObjectName("MO");

		// Construct thread completion
		moBuilder.setManagedObjectPool((context) -> new MockManagedObjectPool(context))
				.addThreadCompletionListener((pool) -> completion);

		// Open the OfficeFloor
		this.constructOfficeFloor().openOfficeFloor();

		// Trigger function to trigger managed object flow
		assertFalse("Thread should not initially be managed", ManagedExecutionFactoryImpl.isCurrentThreadManaged());
		boolean[] isCallbackManaged = new boolean[] { false };
		mo.context.invokeProcess(0, null, mo, 0, (escalation) -> {
			assertNull("Should not be escalation", escalation);
			assertFalse("Thread should not be complete", completion.isThreadComplete);
			isCallbackManaged[0] = ManagedExecutionFactoryImpl.isCurrentThreadManaged();
		});
		assertTrue("Function should be managed", functions.isFunctionManaged);
		assertTrue("Callback should be managed", isCallbackManaged[0]);
		assertFalse("Thread should no longer be managed", ManagedExecutionFactoryImpl.isCurrentThreadManaged());
		assertTrue("Thread managing should be completed", completion.isThreadComplete);
	}

	/**
	 * Ensures only the single {@link ManagedExecution} context, when triggering
	 * internally.
	 */
	public void testReentrantManagedObjectExecution() throws Exception {

		String officeName = this.getOfficeName();
		MockThreadCompletionListener completion = new MockThreadCompletionListener();

		// Construct the functions
		MockObjectFunctions functions = new MockObjectFunctions(completion);
		this.constructFunction(functions, "trigger").buildObject("MO", ManagedObjectScope.PROCESS);
		this.constructFunction(functions, "function");

		// Construct the managed object
		MockManagedObjectSource mo = new MockManagedObjectSource();
		ManagedObjectBuilder<Indexed> moBuilder = this.constructManagedObject("MO", mo, null);
		ManagingOfficeBuilder<Indexed> managingOffice = moBuilder.setManagingOffice(officeName);
		managingOffice.linkFlow(0, "function");
		managingOffice.setInputManagedObjectName("MO");

		// Construct thread completion
		moBuilder.setManagedObjectPool((context) -> new MockManagedObjectPool(context))
				.addThreadCompletionListener((pool) -> completion);

		// Open the OfficeFloor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Obtain the function manager to trigger internally
		FunctionManager functionManager = officeFloor.getOffice(officeName).getFunctionManager("trigger");

		// Trigger managed object flow
		assertFalse("Thread should not initially be managed", ManagedExecutionFactoryImpl.isCurrentThreadManaged());
		functionManager.invokeProcess(0, null);
		assertTrue("Function should be managed", functions.isFunctionManaged);
		assertTrue("Should have successfully invoked trigger callback", functions.isTriggerCallbackInvoked);
		assertFalse("Thread should no longer be managed", ManagedExecutionFactoryImpl.isCurrentThreadManaged());
		assertTrue("Thread managing should be completed", completion.isThreadComplete);
	}

	public static class MockThreadCompletionListener implements ThreadCompletionListener {

		private boolean isThreadComplete = false;

		@Override
		public void threadComplete() {
			this.isThreadComplete = true;
		}
	}

	public static class MockObjectFunctions {

		private final MockThreadCompletionListener completionListener;

		private MockObjectFunctions(MockThreadCompletionListener completionListener) {
			this.completionListener = completionListener;
		}

		private boolean isFunctionManaged = false;

		public void function() {
			assertFalse("Thread should not be complete", this.completionListener.isThreadComplete);
			this.isFunctionManaged = ManagedExecutionFactoryImpl.isCurrentThreadManaged();
		}

		private boolean isTriggerCallbackInvoked = false;

		public void trigger(MockManagedObjectSource mo) {
			mo.context.invokeProcess(0, null, mo, 0, (escalation) -> {
				assertNull("Should not be escalation", escalation);
				assertFalse("Should not complete thread yet", this.completionListener.isThreadComplete);
				assertTrue("Thread should still be managed", ManagedExecutionFactoryImpl.isCurrentThreadManaged());
				this.isTriggerCallbackInvoked = true;
			});
		}
	}

	public static class MockManagedObjectPool implements ManagedObjectPool {

		private final ManagedObjectPoolContext context;

		public MockManagedObjectPool(ManagedObjectPoolContext context) {
			this.context = context;
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			assertTrue("sourcing managed object should be managed",
					ManagedExecutionFactoryImpl.isCurrentThreadManaged());
			this.context.getManagedObjectSource().sourceManagedObject(user);
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			assertTrue("returning mangaed object should be managed",
					ManagedExecutionFactoryImpl.isCurrentThreadManaged());
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
			assertTrue("load managed object should be managed", ManagedExecutionFactoryImpl.isCurrentThreadManaged());
		}

		@Override
		public void empty() {
			// Nothing to empty
		}
	}

	@TestSource
	public static class MockManagedObjectSource extends AbstractManagedObjectSource<None, Indexed>
			implements ManagedObject {

		private ManagedObjectServiceContext<Indexed> context;

		/*
		 * ==================== ManagedObjectSource ==================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {
			context.addFlow(null);
			context.setObjectClass(this.getClass());
		}

		@Override
		public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {
			this.context = new SafeManagedObjectService<>(context);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ==================== ManagedObject =========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}
