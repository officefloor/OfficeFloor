/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building.process.officefloor;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link ManagedProcess} for an {@link OfficeFloor} {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManager implements ManagedProcess,
		OfficeFloorManagerMBean {

	/**
	 * {@link ObjectName} for the {@link OfficeFloorManager}.
	 */
	static ObjectName OFFICE_FLOOR_MANAGER_OBJECT_NAME;

	static {
		try {
			OFFICE_FLOOR_MANAGER_OBJECT_NAME = new ObjectName("officefloor",
					"type", "OfficeFloor");
		} catch (MalformedObjectNameException ex) {
			// This should never be the case
		}
	}

	/**
	 * Obtains the {@link OfficeFloorManagerMBean} {@link ObjectName}.
	 * 
	 * @return {@link OfficeFloorManagerMBean} {@link ObjectName}.
	 */
	public static ObjectName getOfficeFloorManagerObjectName() {
		return OFFICE_FLOOR_MANAGER_OBJECT_NAME;
	}

	/**
	 * Location of the {@link OfficeFloor} configuration.
	 */
	private final String officeFloorLocation;

	/**
	 * Listing of the {@link WorkState}.
	 */
	private final List<WorkState> workStates = new LinkedList<WorkState>();

	/**
	 * {@link ManagedProcessContext}.
	 */
	private transient ManagedProcessContext context = null;

	/**
	 * {@link OfficeFloor} being managed within the {@link Process}.
	 */
	private transient OfficeFloor officeFloor = null;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor} configuration.
	 */
	public OfficeFloorManager(String officeFloorLocation) {
		this.officeFloorLocation = officeFloorLocation;
	}

	/*
	 * ============= OfficeFloorManagedProcessMBean ===================
	 */

	@Override
	public String getOfficeFloorLocation() {
		return this.officeFloorLocation;
	}

	@Override
	public synchronized void invokeWork(String officeName, String workName,
			String parameter) throws Exception {

		// Determine if already opened the OfficeFloor
		if (this.officeFloor != null) {
			// OfficeFloor running so invoke immediately
			new WorkState(officeName, workName, parameter)
					.invoke(this.officeFloor);

		} else {
			// Register the work to be invoked inside the main
			this.workStates.add(new WorkState(officeName, workName, parameter));
		}
	}

	/*
	 * =================== ManagedProcess ============================
	 */

	@Override
	public synchronized void init(ManagedProcessContext context)
			throws Throwable {
		this.context = context;

		// Create the OfficeFloor compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler();
		compiler.addEnvProperties();
		compiler.addSystemProperties();
		compiler.addSourceAliases();

		// Compile the OfficeFloor
		this.officeFloor = compiler.compile(this.officeFloorLocation);

		// Register this MBean
		this.context.registerMBean(this, OFFICE_FLOOR_MANAGER_OBJECT_NAME);
	}

	@Override
	public void main() throws Throwable {

		// Open the OfficeFloor
		this.officeFloor.openOfficeFloor();

		// Ensure close OfficeFloor
		try {

			// Determine if initial work to run
			if (this.workStates.size() == 0) {
				// No work, so wait until triggered to stop
				for (;;) {

					// Determine if flagged to stop
					if (!this.context.continueProcessing()) {
						return; // triggered to stop
					}

					// Wait some time for trigger to stop
					Thread.sleep(100);
				}

			} else {
				// Invoke the work and stop once the work is complete
				for (WorkState workState : this.workStates) {
					workState.invoke(this.officeFloor);
				}

				// Wait for all work to complete
				for (;;) {

					// Check if all work complete
					boolean isAllWorkComplete = true;
					for (WorkState workState : this.workStates) {
						if (!workState.isComplete()) {
							isAllWorkComplete = false;
						}
					}

					// Exit processing if work complete or flagged to stop
					if (isAllWorkComplete
							|| (!this.context.continueProcessing())) {
						return; // All work complete
					}

					// Wait some time for work to complete
					Thread.sleep(100);
				}
			}

		} catch (Throwable ex) {
			// Indicate failure
			ex.printStackTrace();

		} finally {
			// Close the OfficeFloor
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Maintains state of {@link Work} that is invoked on the
	 * {@link OfficeFloor}.
	 */
	private static class WorkState implements Serializable {

		/**
		 * Name of the {@link Office} containing the {@link Work}.
		 */
		private final String officeName;

		/**
		 * Name of the {@link Work}.
		 */
		private final String workName;

		/**
		 * Parameter for the initial {@link Task} of the {@link Work}.
		 */
		private final Object parameter;

		/**
		 * {@link FlowFuture} for invoking the {@link Work}.
		 */
		private FlowFuture invokedFlowFuture = null;

		/**
		 * Initiate.
		 * 
		 * @param officeName
		 *            Name of the {@link Office} containing the {@link Work}.
		 * @param workName
		 *            Name of the {@link Work}.
		 * @param parameter
		 *            Parameter for the initial {@link Task}.
		 */
		public WorkState(String officeName, String workName, Object parameter) {
			this.officeName = officeName;
			this.workName = workName;
			this.parameter = parameter;
		}

		/**
		 * Invokes the {@link Work}.
		 * 
		 * @param officeFloor
		 *            {@link OfficeFloor} to invoke the {@link Work} on.
		 * @throws Exception
		 *             If fails to invoke the {@link Work}.
		 */
		public void invoke(OfficeFloor officeFloor) throws Exception {
			// Invoke the work keeping track of its flow future
			this.invokedFlowFuture = officeFloor.getOffice(this.officeName)
					.getWorkManager(this.workName).invokeWork(this.parameter);
		}

		/**
		 * Indicates if the invoked {@link Work} is complete.
		 * 
		 * @return <code>true</code> if complete.
		 */
		public boolean isComplete() {
			return this.invokedFlowFuture.isComplete();
		}
	}

}