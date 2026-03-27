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

package net.officefloor.frame.stress.object;

import junit.framework.TestSuite;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests the {@link ManagedObjectExecuteContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class InputObjectStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(InputObjectStressTest.class);
	}

	@Override
	protected int getIterationCount() {
		return 100000;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct the managed object
		ManagedObjectBuilder<Indexed> mo = this.constructManagedObject("MO", StressInputManagedObject.class, null);
		mo.setTimeout(1000);
		ManagingOfficeBuilder<Indexed> managingOffice = mo.setManagingOffice(this.getOfficeName());
		managingOffice.setInputManagedObjectName("MO");
		managingOffice.linkFlow(0, "flow");

		// Construct the functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		context.loadOtherTeam(task.getBuilder());
		task.buildObject("MO", ManagedObjectScope.PROCESS);
		task.buildFlow("task", null, false);
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "flow");
		context.loadResponsibleTeam(flow.getBuilder());
		flow.buildParameter();
		flow.buildObject("MO");

		// Test
		context.setInitialFunction("task", null);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final StressContext context;

		private volatile TestState state = TestState.INIT;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void task(StressInputManagedObject object, ReflectiveFlow repeat) {
			assertEquals(TestState.INIT, this.state);
			this.state = TestState.RUN;

			// Ensure not input managed object
			assertFalse("Should not be input managed object", object.isInput);

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Must be in asynchronous operation
			object.asynchronous.start(() -> {
				assertEquals(TestState.RUN, this.state);
				this.state = TestState.ASYNC;

				// Trigger the flow (to stop waiting)
				object.service.invokeProcess(0, object, new StressInputManagedObject(true), 0, (escalation) -> {
					assertNull("Should be no escalation", escalation);
				});
			});

			// Repeat
			repeat.doFlow(null, null);
		}

		public void flow(StressInputManagedObject parameter, StressInputManagedObject managedObject) {
			assertEquals(TestState.ASYNC, this.state);
			this.state = TestState.INIT;

			// Ensure correct parameter
			assertFalse("Should not be input", parameter.isInput);

			// Ensure have input managed object
			assertTrue("Should be input managed object", managedObject.isInput);

			// Allow invoking task to continue
			parameter.asynchronous.complete(null);
		}
	}

	/**
	 * Test states.
	 */
	private static enum TestState {
		INIT, RUN, ASYNC
	}

	/**
	 * Stress input {@link ManagedObjectSource}.
	 */
	public static class StressInputManagedObject extends AbstractManagedObjectSource<Indexed, Indexed>
			implements AsynchronousManagedObject {

		private final boolean isInput;

		private ManagedObjectServiceContext<Indexed> service;

		private AsynchronousContext asynchronous;

		public StressInputManagedObject() {
			this(false);
		}

		public StressInputManagedObject(boolean isInput) {
			this.isInput = isInput;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<Indexed, Indexed> context) throws Exception {
			context.setManagedObjectClass(this.getClass());
			context.setObjectClass(this.getClass());
			context.addFlow(StressInputManagedObject.class);
		}

		@Override
		public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {
			this.service = new SafeManagedObjectService<>(context);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public void setAsynchronousContext(AsynchronousContext context) {
			this.asynchronous = context;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}
