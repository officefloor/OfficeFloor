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

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Tests the {@link ProcessState} is appropriately passed between
 * {@link ManagedFunction} instances of the Office.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionProcessStateTest extends AbstractOfficeConstructTestCase {

	/**
	 * Validate {@link ProcessState} is passed between {@link ManagedFunction}
	 * instances.
	 */
	public void testProcessState() throws Exception {

		final String officeName = this.getOfficeName();

		// Parameter to be passed between work instances
		final Object parameter = new Object();

		// Add the Managed Object
		this.constructManagedObject(new ManagedObjectOne(), "MANAGED_OBJECT", officeName);

		// Add the first work
		WorkOne workOne = new WorkOne(parameter);
		ManagedFunctionBuilder<WorkOneManagedObjectsEnum, WorkOneDelegatesEnum> taskOneBuilder = this
				.constructFunction("SENDER", workOne);
		taskOneBuilder.addManagedObject("mo-one", "MANAGED_OBJECT");
		taskOneBuilder.linkFlow(WorkOneDelegatesEnum.WORK_TWO.ordinal(), "RECEIVER", Object.class, false);
		taskOneBuilder.linkManagedObject(WorkOneManagedObjectsEnum.MANAGED_OBJECT_ONE.ordinal(), "mo-one",
				ManagedObjectOne.class);

		// Add the second work
		WorkTwo workTwo = new WorkTwo();
		ManagedFunctionBuilder<WorkTwoManagedObjectsEnum, NoDelegatesEnum> taskTwoBuilder = this
				.constructFunction("RECEIVER", workTwo);
		taskTwoBuilder.addManagedObject("mo-two", "MANAGED_OBJECT");
		taskTwoBuilder.linkManagedObject(WorkTwoManagedObjectsEnum.MANAGED_OBJECT_ONE.ordinal(), "mo-two",
				ManagedObjectOne.class);

		// Invoke WorkOne
		this.invokeFunction("SENDER", new Object());

		// Validate the parameter was passed
		assertEquals("Incorrect parameter", parameter, workTwo.getParameter());
	}

	/**
	 * Object retrieved.
	 */
	private static Object retrievedObject = null;

	/**
	 * First {@link ManagedFunction} type for testing.
	 */
	private class WorkOne implements ManagedFunction<WorkOneManagedObjectsEnum, WorkOneDelegatesEnum> {

		/**
		 * Parameter to invoke delegate work with.
		 */
		protected final Object parameter;

		/**
		 * Initiate.
		 * 
		 * @param parameter Parameter to invoke delegate work with.
		 */
		public WorkOne(Object parameter) {
			this.parameter = parameter;
		}

		/*
		 * ==================== Task ==========================================
		 */

		@Override
		public void execute(ManagedFunctionContext<WorkOneManagedObjectsEnum, WorkOneDelegatesEnum> context)
				throws Exception {

			// Obtain the Managed Object
			ManagedObjectOne managedObjectOne = (ManagedObjectOne) context
					.getObject(WorkOneManagedObjectsEnum.MANAGED_OBJECT_ONE);

			// Specify the retrieved object
			retrievedObject = managedObjectOne;

			// Specify the parameter
			managedObjectOne.setParameter(parameter);

			// Obtain the Work Supervisor to initiate the second work
			context.doFlow(WorkOneDelegatesEnum.WORK_TWO, null, null);
		}
	}

	private enum WorkOneDelegatesEnum {
		WORK_TWO
	}

	private enum WorkOneManagedObjectsEnum {
		MANAGED_OBJECT_ONE
	}

	/**
	 * Second {@link ManagedFunction} type for testing.
	 */
	private class WorkTwo implements ManagedFunction<WorkTwoManagedObjectsEnum, NoDelegatesEnum> {

		/**
		 * Parameter received when invoked.
		 */
		protected volatile Object parameter;

		/**
		 * Obtains the received parameter;
		 * 
		 * @return Received parameter;
		 */
		public Object getParameter() {
			return this.parameter;
		}

		/*
		 * ==================== Task ==========================================
		 */

		@Override
		public void execute(ManagedFunctionContext<WorkTwoManagedObjectsEnum, NoDelegatesEnum> context)
				throws Exception {

			// Obtain the Managed Object
			ManagedObjectOne managedObjectOne = (ManagedObjectOne) context
					.getObject(WorkTwoManagedObjectsEnum.MANAGED_OBJECT_ONE);

			// Ensure the correct retrieved object
			assertSame("Incorrect retrieved object", retrievedObject, managedObjectOne);

			// Obtain the parameter
			this.parameter = managedObjectOne.getParameter();
		}

	}

	private enum NoDelegatesEnum {
	}

	private enum WorkTwoManagedObjectsEnum {
		MANAGED_OBJECT_ONE
	}

	/**
	 * {@link ManagedObject}.
	 */
	private class ManagedObjectOne {

		/**
		 * Parameter.
		 */
		protected volatile Object parameter;

		/**
		 * Specifies the parameter.
		 * 
		 * @param parameter Parameter.
		 */
		public void setParameter(Object parameter) {
			this.parameter = parameter;
		}

		/**
		 * Obtains the parameter.
		 * 
		 * @return Parameter.
		 */
		public Object getParameter() {
			return this.parameter;
		}
	}

}
