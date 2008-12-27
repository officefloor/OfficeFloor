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
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Validates passing a parameter between two
 * {@link net.officefloor.frame.api.execute.Work} instances of a office.
 * 
 * @author Daniel
 */
public class OfficePassParameterTest extends AbstractOfficeConstructTestCase {

	/**
	 * Validates that able to pass parameters between
	 * {@link net.officefloor.frame.api.execute.Work} instances.
	 */
	@SuppressWarnings("unchecked")
	public void testPassParameterBetweenWork() throws Exception {

		// Parameter to be passed between work instances
		final Object parameter = new Object();

		// Add the team
		this.constructTeam("TEAM", new OnePersonTeam(10));

		// Add the first work
		WorkOne workOne = new WorkOne(parameter);
		this.constructWork("WORK_ONE", workOne, "SENDER");
		TaskBuilder<Object, WorkOne, NoManagedObjectsEnum, WorkOneDelegatesEnum> taskBuilder = this
				.constructTask("SENDER", Object.class, workOne, "TEAM", null);
		taskBuilder.linkFlow(WorkOneDelegatesEnum.WORK_TWO.ordinal(),
				"WORK_TWO", "RECEIVER", FlowInstigationStrategyEnum.SEQUENTIAL);

		// Add the second work
		WorkTwo workTwo = new WorkTwo();
		this.constructWork("WORK_TWO", workTwo, "RECEIVER");
		this.constructTask("RECEIVER", Object.class, workTwo, "TEAM", null);

		// Register and open the office floor
		OfficeFloor officeFloor = this.constructOfficeFloor("TEST");
		officeFloor.openOfficeFloor();

		// Invoke WorkOne
		WorkManager workManager = officeFloor.getOffice("TEST").getWorkManager(
				"WORK_ONE");
		workManager.invokeWork(null);

		// Allow some time for processing
		this.sleep(1);

		// Close the office floor
		officeFloor.closeOfficeFloor();

		// Validate the parameter was passed
		assertEquals("Incorrect parameter", parameter, workTwo.getParameter());
	}

	/**
	 * First {@link net.officefloor.frame.api.execute.Work} type for testing.
	 */
	private class WorkOne implements Work,
			Task<Object, WorkOne, NoManagedObjectsEnum, WorkOneDelegatesEnum> {

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
		 * @see
		 * net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame
		 * .api.execute.TaskContext)
		 */
		public Object doTask(
				TaskContext<Object, WorkOne, NoManagedObjectsEnum, WorkOneDelegatesEnum> context)
				throws Exception {

			// Delegate to the work
			context.doFlow(WorkOneDelegatesEnum.WORK_TWO, this.parameter);

			// No parameter
			return null;
		}

	}

	private enum WorkOneDelegatesEnum {
		WORK_TWO
	}

	/**
	 * Second {@link net.officefloor.frame.api.execute.Work} type for testing.
	 */
	private class WorkTwo implements Work,
			Task<Object, WorkTwo, NoManagedObjectsEnum, NoDelegatesEnum> {

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
		 * @see
		 * net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame
		 * .api.execute.TaskContext)
		 */
		public Object doTask(
				TaskContext<Object, WorkTwo, NoManagedObjectsEnum, NoDelegatesEnum> context)
				throws Exception {

			// Store the parameter
			this.parameter = context.getParameter();

			// No parameter
			return null;
		}

	}

	private enum NoDelegatesEnum {
	}

	private enum NoManagedObjectsEnum {
	}

}
