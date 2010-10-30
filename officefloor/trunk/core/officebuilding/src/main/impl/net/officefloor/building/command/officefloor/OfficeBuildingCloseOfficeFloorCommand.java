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
package net.officefloor.building.command.officefloor;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingHostOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ProcessNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.StopMaxWaitTimeOfficeFloorCommandParameter;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorCommand} to close the {@link OfficeFloor} within the
 * {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingCloseOfficeFloorCommand implements
		OfficeFloorCommandFactory, OfficeFloorCommand {

	/**
	 * {@link OfficeBuilding} host.
	 */
	private final OfficeBuildingHostOfficeFloorCommandParameter officeBuildingHost = new OfficeBuildingHostOfficeFloorCommandParameter();

	/**
	 * {@link OfficeBuilding} port.
	 */
	private final OfficeBuildingPortOfficeFloorCommandParameter officeBuildingPort = new OfficeBuildingPortOfficeFloorCommandParameter();

	/**
	 * {@link Process} name.
	 */
	private final ProcessNameOfficeFloorCommandParameter processName = new ProcessNameOfficeFloorCommandParameter();

	/**
	 * Stop maximum wait time.
	 */
	private final StopMaxWaitTimeOfficeFloorCommandParameter stopMaxWaitTime = new StopMaxWaitTimeOfficeFloorCommandParameter();

	/*
	 * ================= OfficeFloorCommandFactory =================
	 */

	@Override
	public String getCommandName() {
		return "close";
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return new OfficeBuildingCloseOfficeFloorCommand();
	}

	/*
	 * ==================== OfficeFloorCommand ======================
	 */

	@Override
	public String getDescription() {
		return "Closes an OfficeFloor within the OfficeBuilding";
	}

	@Override
	public OfficeFloorCommandParameter[] getParameters() {
		return new OfficeFloorCommandParameter[] { this.officeBuildingHost,
				this.officeBuildingPort, this.processName, this.stopMaxWaitTime };
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context)
			throws Exception {
		// Nothing to initialise
	}

	@Override
	public ManagedProcess createManagedProcess(
			OfficeFloorCommandEnvironment environment) throws Exception {

		// Obtain the close details
		String officeBuildingHost = this.officeBuildingHost
				.getOfficeBuildingHost();
		int officeBuildingPort = this.officeBuildingPort
				.getOfficeBuildingPort();
		String processNamespace = this.processName.getProcessName();
		long stopMaxWaitTime = this.stopMaxWaitTime.getStopMaxWaitTime();

		// Create and return the managed process
		return new CloseManagedProcess(officeBuildingHost, officeBuildingPort,
				processNamespace, stopMaxWaitTime);
	}

	/**
	 * {@link ManagedProcess} to close the {@link OfficeFloor}.
	 */
	public static class CloseManagedProcess implements ManagedProcess {

		/**
		 * {@link OfficeBuilding} host.
		 */
		private final String officeBuildingHost;

		/**
		 * {@link OfficeBuilding} port.
		 */
		private final int officeBuildingPort;

		/**
		 * {@link OfficeFloor} process name space.
		 */
		private final String processNamespace;

		/**
		 * Stop maximum wait time.
		 */
		private final long stopMaxWaitTime;

		/**
		 * Initiate.
		 * 
		 * @param officeBuildingHost
		 *            {@link OfficeBuilding} host.
		 * @param officeBuildingPort
		 *            {@link OfficeBuilding} port.
		 * @param processNamespace
		 *            {@link OfficeFloor} process name space.
		 * @param stopMaxWaitTime
		 *            Stop maximum wait time.
		 */
		public CloseManagedProcess(String officeBuildingHost,
				int officeBuildingPort, String processNamespace,
				long stopMaxWaitTime) {
			this.officeBuildingHost = officeBuildingHost;
			this.officeBuildingPort = officeBuildingPort;
			this.processNamespace = processNamespace;
			this.stopMaxWaitTime = stopMaxWaitTime;
		}

		/*
		 * =================== ManagedProcess =========================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Nothing to initialise
		}

		@Override
		public void main() throws Throwable {

			// Obtain the OfficeBuilding manager
			OfficeBuildingManagerMBean officeBuildingManager = OfficeBuildingManager
					.getOfficeBuildingManager(this.officeBuildingHost,
							this.officeBuildingPort);

			// Close the OfficeFloor
			String result = officeBuildingManager.closeOfficeFloor(
					this.processNamespace, this.stopMaxWaitTime);

			// Output result of closing
			System.out.println(result);
		}
	}

}