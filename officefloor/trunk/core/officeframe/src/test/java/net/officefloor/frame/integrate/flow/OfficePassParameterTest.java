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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;

/**
 * Validates passing a parameter between two {@link Work} instances of a office.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficePassParameterTest extends AbstractOfficeConstructTestCase {

	/**
	 * Validates that able to pass parameters between {@link Work} instances.
	 */
	@SuppressWarnings("unchecked")
	public void testPassParameterBetweenWork() throws Exception {

		// Parameter to be passed between work instances
		final Object parameter = new Object();

		// Add the team
		this.constructTeam("TEAM",
				new OnePersonTeam("TEAM",
						MockTeamSource.createTeamIdentifier(), 10));

		// Add the first work
		WorkOne workOne = new WorkOne(parameter);
		this.constructWork("WORK_ONE", workOne, "SENDER");
		TaskBuilder<WorkOne, None, WorkOneDelegatesEnum> taskOneBuilder = this
				.constructTask("SENDER", workOne, "TEAM", null, null);
		taskOneBuilder.linkFlow(WorkOneDelegatesEnum.WORK_TWO.ordinal(),
				"WORK_TWO", "RECEIVER", FlowInstigationStrategyEnum.SEQUENTIAL,
				Object.class);

		// Add the second work
		WorkTwo workTwo = new WorkTwo();
		this.constructWork("WORK_TWO", workTwo, "RECEIVER");
		TaskBuilder<WorkTwo, WorkTwoDependenciesEnum, None> taskTwoBuilder = this
				.constructTask("RECEIVER", workTwo, "TEAM", null, null);
		taskTwoBuilder.linkParameter(WorkTwoDependenciesEnum.PARAMETER,
				Object.class);

		// Register and open the office floor
		String officeName = this.getOfficeName();
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Invoke WorkOne
		WorkManager workManager = officeFloor.getOffice(officeName)
				.getWorkManager("WORK_ONE");
		workManager.invokeWork(null);

		// Allow some time for processing
		this.sleep(1);

		// Close the office floor
		officeFloor.closeOfficeFloor();

		// Validate the parameter was passed
		assertEquals("Incorrect parameter", parameter, workTwo.getParameter());
	}

	/**
	 * First {@link Work} type for testing.
	 */
	private class WorkOne implements Work,
			Task<WorkOne, None, WorkOneDelegatesEnum> {

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
		public Object doTask(
				TaskContext<WorkOne, None, WorkOneDelegatesEnum> context)
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
	 * Second {@link Work} type for testing.
	 */
	private class WorkTwo implements Work,
			Task<WorkTwo, WorkTwoDependenciesEnum, None> {

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
		public Object doTask(
				TaskContext<WorkTwo, WorkTwoDependenciesEnum, None> context)
				throws Exception {

			// Store the parameter
			this.parameter = context
					.getObject(WorkTwoDependenciesEnum.PARAMETER);

			// No parameter
			return null;
		}
	}

	private enum WorkTwoDependenciesEnum {
		PARAMETER
	}

}