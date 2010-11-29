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
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.building.process.officefloor.OfficeFloorManagerMBean;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorCommand} to list information about the
 * {@link OfficeBuilding} and its contained {@link OfficeFloor} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingListOfficeFloorCommand implements
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

	/*
	 * ======================= OfficeFloorCommandFactory ===================
	 */

	@Override
	public String getCommandName() {
		return "list";
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return new OfficeBuildingListOfficeFloorCommand();
	}

	/*
	 * =========================== OfficeFloorCommand =======================
	 */

	@Override
	public String getDescription() {
		return "Lists details of the OfficeBuilding/OfficeFloor";
	}

	@Override
	public OfficeFloorCommandParameter[] getParameters() {
		return new OfficeFloorCommandParameter[] { this.officeBuildingHost,
				this.officeBuildingPort, this.processName };
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context)
			throws Exception {
		// Nothing to initialise
	}

	@Override
	public ManagedProcess createManagedProcess(
			OfficeFloorCommandEnvironment environment) throws Exception {

		// Obtain details
		String officeBuildingHost = this.officeBuildingHost
				.getOfficeBuildingHost();
		int officeBuildingPort = this.officeBuildingPort
				.getOfficeBuildingPort();
		String processName = this.processName.getProcessName();

		// Return the list managed process
		return new ListManagedProcess(officeBuildingHost, officeBuildingPort,
				processName);
	}

	/**
	 * {@link ManagedProcess} to list information about the
	 * {@link OfficeBuilding} or {@link OfficeFloor}.
	 */
	public static class ListManagedProcess implements ManagedProcess {

		/**
		 * {@link OfficeBuilding} host.
		 */
		private final String officeBuildingHost;

		/**
		 * {@link OfficeBuilding} port.
		 */
		private final int officeBuildingPort;

		/**
		 * {@link Process} namespace.
		 */
		private final String processNamespace;

		/**
		 * Initiate.
		 * 
		 * @param officeBuildingHost
		 *            {@link OfficeBuilding} host.
		 * @param officeBuildingPort
		 *            {@link OfficeBuilding} port.
		 * @param processNamespace
		 *            {@link Process} namespace.
		 */
		public ListManagedProcess(String officeBuildingHost,
				int officeBuildingPort, String processNamespace) {
			this.officeBuildingHost = officeBuildingHost;
			this.officeBuildingPort = officeBuildingPort;
			this.processNamespace = processNamespace;
		}

		/*
		 * ===================== ManagedProcess ============================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Nothing to initialise
		}

		@Override
		public void main() throws Throwable {

			String listing;
			if (this.processNamespace == null) {
				// List the processes
				OfficeBuildingManagerMBean officeBuildingManager = OfficeBuildingManager
						.getOfficeBuildingManager(this.officeBuildingHost,
								this.officeBuildingPort);
				listing = officeBuildingManager.listProcessNamespaces();
			} else {
				// List the tasks of the process name space
				OfficeFloorManagerMBean officeFloorManager = OfficeBuildingManager
						.getOfficeFloorManager(this.officeBuildingHost,
								this.officeBuildingPort, this.processNamespace);
				listing = officeFloorManager.listTasks();
			}

			// Output the listing
			System.out.println(listing);
		}
	}

}