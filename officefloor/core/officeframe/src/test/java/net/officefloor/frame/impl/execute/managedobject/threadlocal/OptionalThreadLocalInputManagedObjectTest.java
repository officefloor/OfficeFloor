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

package net.officefloor.frame.impl.execute.managedobject.threadlocal;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.thread.OptionalThreadLocal;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
import net.officefloor.frame.impl.spi.team.WorkerPerJobTeamSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ThreadSafeClosure;

/**
 * Tests accessing the {@link OptionalThreadLocal} via an input
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class OptionalThreadLocalInputManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure the {@link OptionalThreadLocal} not provide object if
	 * {@link ManagedObject} not instantiated.
	 */
	public void testNotAvailable() throws Exception {
		this.doInputManagedObjectTest(false, false);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value.
	 */
	public void testAvailable() throws Exception {
		this.doInputManagedObjectTest(true, false);
	}

	/**
	 * Ensure the {@link OptionalThreadLocal} no provide object if
	 * {@link ManagedObject} not instantiated via {@link Team} on another
	 * {@link Thread}.
	 */
	public void testNotAvailableWithTeam() throws Exception {
		this.doInputManagedObjectTest(false, true);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value run by {@link Team} on another {@link Thread}.
	 */
	public void testAvailableWithTeam() throws Exception {
		this.doInputManagedObjectTest(true, true);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param isExpectAvailable If object should be available.
	 * @param isTeam            If use {@link Team} for {@link ManagedFunction}.
	 */
	private void doInputManagedObjectTest(boolean isExpectAvailable, boolean isTeam) throws Exception {

		// Create the managed object
		String value = "Should be available";
		this.constructManagedObject(value, "MOS", this.getOfficeName());
		ThreadDependencyMappingBuilder mo = this.getOfficeBuilder().addProcessManagedObject("MO", "MOS");

		// Obtain the expected thread local value
		String expectedValue = isExpectAvailable ? value : null;

		// Construct the input managed object
		TestInputManagedObjectSource input = new TestInputManagedObjectSource(expectedValue);
		ManagedObjectBuilder<?> inputBuilder = this.constructManagedObject("INPUT", input, null);
		ManagingOfficeBuilder<?> inputOffice = inputBuilder.setManagingOffice(this.getOfficeName());
		inputOffice.linkFlow(0, "service");
		ThreadDependencyMappingBuilder inputDependencies = inputOffice.setInputManagedObjectName("INPUT");
		if (isExpectAvailable) {
			inputDependencies.mapDependency(0, "MO");
		}

		// Ensure the same optional thread local on multiple calls
		OptionalThreadLocal<String> threadLocal = mo.getOptionalThreadLocal();
		assertNotNull("Should have thread local", threadLocal);
		assertSame("Should be same thread local on multiple calls", threadLocal, mo.getOptionalThreadLocal());
		input.threadLocal = threadLocal;

		// Construct the function to handle input
		Work work = new Work(threadLocal);
		ReflectiveFunctionBuilder function = this.constructFunction(work, "service");
		function.buildObject("INPUT");

		// Determine if team
		if (isTeam) {
			this.constructTeam("TEAM", WorkerPerJobTeamSource.class);
			function.getBuilder().setResponsibleTeam("TEAM");
		}

		// Compile the OfficeFloor
		this.constructOfficeFloor().openOfficeFloor();

		// Execute the input managed object (and ensure correct dependencies)
		ThreadSafeClosure<Boolean> complete = new ThreadSafeClosure<>(Boolean.FALSE);
		input.serviceContext.invokeProcess(FlowKeys.INPUT, null, input, 0, (failure) -> {
			if (failure != null) {
				failure.printStackTrace();
			}
			complete.set(Boolean.TRUE);
		});
		this.waitForTrue(() -> complete.get());
		assertSame("Should inject object", input, work.inputObject);
		if (isExpectAvailable) {
			assertSame("Thread local object should be available", value, work.threadLocalObject);
		} else {
			assertNull("Should not be available", work.threadLocalObject);
		}
	}

	public static class Work {

		private final OptionalThreadLocal<String> threadLocal;

		private volatile TestInputManagedObjectSource inputObject;

		private volatile String threadLocalObject;

		public Work(OptionalThreadLocal<String> threadLocal) {
			this.threadLocal = threadLocal;
		}

		public void service(TestInputManagedObjectSource input) {
			this.inputObject = input;
			this.threadLocalObject = this.threadLocal.get();
		}
	}

	public static enum FlowKeys {
		INPUT
	}

	@TestSource
	public static class TestInputManagedObjectSource extends AbstractManagedObjectSource<Indexed, FlowKeys>
			implements CoordinatingManagedObject<Indexed> {

		private final String expectedObject;

		private volatile OptionalThreadLocal<String> threadLocal;

		private ManagedObjectServiceContext<FlowKeys> serviceContext;

		public TestInputManagedObjectSource(String expectedObject) {
			this.expectedObject = expectedObject;
		}

		/*
		 * ================= ManagedObjectSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<Indexed, FlowKeys> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.addFlow(FlowKeys.INPUT, null);
			if (this.expectedObject != null) {
				context.addDependency(String.class);
			}
		}

		@Override
		public void start(ManagedObjectExecuteContext<FlowKeys> context) throws Exception {
			this.serviceContext = new SafeManagedObjectService<>(context);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not be input only");
			return null;
		}

		/*
		 * =============== CoordinatingManagedObject =====================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {
			assertSame("Available in loadObjects", this.expectedObject, this.threadLocal.get());
		}

		@Override
		public Object getObject() throws Throwable {
			assertSame("Available in getObject", this.expectedObject, this.threadLocal.get());
			return this;
		}
	}

}
