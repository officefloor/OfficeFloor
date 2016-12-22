/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.integrate.flow;

import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;

/**
 * Tests the {@link ProcessState} is appropriately passed between {@link Work}
 * instances of the Office.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeProcessStateTest extends AbstractOfficeConstructTestCase {

	/**
	 * Validate {@link ProcessState} is passed between {@link Work} instances.
	 */
	@SuppressWarnings("unchecked")
	public void testProcessState() throws Exception {

		final String officeName = this.getOfficeName();

		// Parameter to be passed between work instances
		final Object parameter = new Object();

		// Add the team
		this.constructTeam("TEAM", new OnePersonTeam("TEAM",
				MockTeamSource.createTeamIdentifier(), 10));

		// Add the Managed Object
		this.constructManagedObject(new ManagedObjectOne(), "MANAGED_OBJECT",
				officeName);

		// Add the first work
		WorkOne workOne = new WorkOne(parameter);
		WorkBuilder<WorkOne> workOneBuilder = this.constructWork("WORK_ONE",
				workOne, "SENDER");
		workOneBuilder.addWorkManagedObject("mo-one", "MANAGED_OBJECT");
		ManagedFunctionBuilder<WorkOne, WorkOneManagedObjectsEnum, WorkOneDelegatesEnum> taskOneBuilder = this
				.constructTask("SENDER", workOne, "TEAM", null, null);
		taskOneBuilder.linkFlow(WorkOneDelegatesEnum.WORK_TWO.ordinal(),
				"WORK_TWO", "RECEIVER", FlowInstigationStrategyEnum.SEQUENTIAL,
				Object.class);
		taskOneBuilder.linkManagedObject(
				WorkOneManagedObjectsEnum.MANAGED_OBJECT_ONE.ordinal(),
				"mo-one", ManagedObjectOne.class);

		// Add the second work
		WorkTwo workTwo = new WorkTwo();
		WorkBuilder<WorkTwo> workTwoBuilder = this.constructWork("WORK_TWO",
				workTwo, "RECEIVER");
		workTwoBuilder.addWorkManagedObject("mo-two", "MANAGED_OBJECT");
		ManagedFunctionBuilder<WorkTwo, WorkTwoManagedObjectsEnum, NoDelegatesEnum> taskTwoBuilder = this
				.constructTask("RECEIVER", workTwo, "TEAM", null, null);
		taskTwoBuilder.linkManagedObject(
				WorkTwoManagedObjectsEnum.MANAGED_OBJECT_ONE.ordinal(),
				"mo-two", ManagedObjectOne.class);

		// Register and open the office floor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Invoke WorkOne
		WorkManager workManager = officeFloor.getOffice(officeName)
				.getWorkManager("WORK_ONE");
		workManager.invokeWork(new Object());

		// Allow some time for processing
		this.sleep(1);

		// Close the office floor
		officeFloor.closeOfficeFloor();

		// Validate the parameter was passed
		assertEquals("Incorrect parameter", parameter, workTwo.getParameter());
	}

	/**
	 * Object retrieved.
	 */
	private static Object retrievedObject = null;

	/**
	 * First {@link Work} type for testing.
	 */
	private class WorkOne implements Work,
			ManagedFunction<WorkOne, WorkOneManagedObjectsEnum, WorkOneDelegatesEnum> {

		/**
		 * Parameter to invoke delegate work with.
		 */
		protected final Object parameter;

		/**
		 * Initiate.
		 * 
		 * @param parameter
		 *            Parameter to invoke delegate work with.
		 */
		public WorkOne(Object parameter) {
			this.parameter = parameter;
		}

		/*
		 * ==================== Task ==========================================
		 */

		@Override
		public Object execute(
				ManagedFunctionContext<WorkOne, WorkOneManagedObjectsEnum, WorkOneDelegatesEnum> context)
				throws Exception {

			// Obtain the Managed Object
			ManagedObjectOne managedObjectOne = (ManagedObjectOne) context
					.getObject(WorkOneManagedObjectsEnum.MANAGED_OBJECT_ONE);

			// Specify the retrieved object
			retrievedObject = managedObjectOne;

			// Specify the parameter
			managedObjectOne.setParameter(parameter);

			// Obtain the Work Supervisor to initiate the second work
			context.doFlow(WorkOneDelegatesEnum.WORK_TWO, null);

			// No further parameter
			return null;
		}
	}

	private enum WorkOneDelegatesEnum {
		WORK_TWO
	}

	private enum WorkOneManagedObjectsEnum {
		MANAGED_OBJECT_ONE
	}

	/**
	 * Second {@link Work} type for testing.
	 */
	private class WorkTwo implements Work,
			ManagedFunction<WorkTwo, WorkTwoManagedObjectsEnum, NoDelegatesEnum> {

		/**
		 * Parameter received when {@link Work} invoked.
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
		public Object execute(
				ManagedFunctionContext<WorkTwo, WorkTwoManagedObjectsEnum, NoDelegatesEnum> context)
				throws Exception {

			// Obtain the Managed Object
			ManagedObjectOne managedObjectOne = (ManagedObjectOne) context
					.getObject(WorkTwoManagedObjectsEnum.MANAGED_OBJECT_ONE);

			// Ensure the correct retrieved object
			assertSame("Incorrect retrieved object", retrievedObject,
					managedObjectOne);

			// Obtain the parameter
			this.parameter = managedObjectOne.getParameter();

			// No parameter
			return null;
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
		 * @param parameter
		 *            Parameter.
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