/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.api.construct;

import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.execute.WorkContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Tests the {@link net.officefloor.frame.internal.structure.ProcessState} is
 * appropriately passed between {@link net.officefloor.frame.api.execute.Work}
 * instances of the Office.
 * 
 * @author Daniel
 */
public class OfficeProcessStateTest extends AbstractOfficeConstructTestCase {

	/**
	 * Validate {@link net.officefloor.frame.internal.structure.ProcessState} is
	 * passed between {@link net.officefloor.frame.api.execute.Work} instances.
	 */
	@SuppressWarnings("unchecked")
	public void testProcessState() throws Exception {

		// Parameter to be passed between work instances
		final Object parameter = new Object();

		// Add the team
		this.constructTeam("TEAM", new OnePersonTeam(10));

		// Add the Managed Object
		this.constructManagedObject(new ManagedObjectOne(), "MANAGED_OBJECT",
				"TEST");

		// Add the first work
		WorkOne workOne = new WorkOne(parameter);
		WorkBuilder<WorkOne> workOneBuilder = this.constructWork("WORK_ONE",
				workOne, "SENDER");
		workOneBuilder.addWorkManagedObject("mo-one", "MANAGED_OBJECT");
		TaskBuilder<Object, WorkOne, WorkOneManagedObjectsEnum, WorkOneDelegatesEnum> taskOneBuilder = this
				.constructTask("SENDER", Object.class, workOne, "TEAM", null);
		taskOneBuilder.linkFlow(WorkOneDelegatesEnum.WORK_TWO.ordinal(),
				"WORK_TWO", "RECEIVER", FlowInstigationStrategyEnum.SEQUENTIAL);
		taskOneBuilder.linkManagedObject(
				WorkOneManagedObjectsEnum.MANAGED_OBJECT_ONE.ordinal(),
				"mo-one");

		// Add the second work
		WorkTwo workTwo = new WorkTwo();
		WorkBuilder<WorkTwo> workTwoBuilder = this.constructWork("WORK_TWO",
				workTwo, "RECEIVER");
		workTwoBuilder.addWorkManagedObject("mo-two", "MANAGED_OBJECT");
		TaskBuilder<Object, WorkTwo, WorkTwoManagedObjectsEnum, NoDelegatesEnum> taskTwoBuilder = this
				.constructTask("RECEIVER", Object.class, workTwo, "TEAM", null);
		taskTwoBuilder.linkManagedObject(
				WorkTwoManagedObjectsEnum.MANAGED_OBJECT_ONE.ordinal(),
				"mo-two");

		// Register and open the office floor
		OfficeFloor officeFloor = this.constructOfficeFloor("TEST");
		officeFloor.openOfficeFloor();

		// Invoke WorkOne
		WorkManager workManager = officeFloor.getOffice("TEST").getWorkManager(
				"WORK_ONE");
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
	 * First {@link net.officefloor.frame.api.execute.Work} type for testing.
	 */
	private class WorkOne
			implements
			Work,
			Task<Object, WorkOne, WorkOneManagedObjectsEnum, WorkOneDelegatesEnum> {

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
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.execute.Work#setWorkContext(net.officefloor.frame.api.execute.WorkContext)
		 */
		public void setWorkContext(WorkContext context) {
			// Do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
		 */
		public Object doTask(
				TaskContext<Object, WorkOne, WorkOneManagedObjectsEnum, WorkOneDelegatesEnum> context)
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

			// No futher parameter
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
	 * Second {@link net.officefloor.frame.api.execute.Work} type for testing.
	 */
	private class WorkTwo implements Work,
			Task<Object, WorkTwo, WorkTwoManagedObjectsEnum, NoDelegatesEnum> {

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
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.execute.Work#setWorkContext(net.officefloor.frame.api.execute.WorkContext)
		 */
		public void setWorkContext(WorkContext context) {
			// Do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
		 */
		public Object doTask(
				TaskContext<Object, WorkTwo, WorkTwoManagedObjectsEnum, NoDelegatesEnum> context)
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
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
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
