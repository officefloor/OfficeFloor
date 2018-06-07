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
package net.officefloor.building.command.officefloor;

import java.io.Serializable;

import javax.management.remote.JMXServiceURL;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingHostOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.console.OfficeBuilding;

/**
 * {@link OfficeFloorCommand} to output the URL to the {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingUrlCommand implements OfficeFloorCommandFactory,
		OfficeFloorCommand {

	/**
	 * {@link OfficeBuilding} host.
	 */
	private final OfficeBuildingHostOfficeFloorCommandParameter officeBuildingHost = new OfficeBuildingHostOfficeFloorCommandParameter();

	/**
	 * {@link OfficeBuilding} port.
	 */
	private final OfficeBuildingPortOfficeFloorCommandParameter officeBuildingPort = new OfficeBuildingPortOfficeFloorCommandParameter();

	/*
	 * ===================== OfficeFloorCommandFactory ===================
	 */

	@Override
	public String getCommandName() {
		return "url";
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return new OfficeBuildingUrlCommand();
	}

	/*
	 * =========================== OfficeFloorCommand ======================
	 */

	@Override
	public String getDescription() {
		return "Obtains the URL for the OfficeBuilding";
	}

	@Override
	public OfficeFloorCommandParameter[] getParameters() {
		return new OfficeFloorCommandParameter[] { this.officeBuildingHost,
				this.officeBuildingPort };
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context)
			throws Exception {
		// Nothing to initialise
	}

	@Override
	public ManagedProcess createManagedProcess(
			OfficeFloorCommandEnvironment environment) throws Exception {

		// Obtain the details
		String officeBuildingHost = this.officeBuildingHost
				.getOfficeBuildingHost();
		int officeBuildingPort = this.officeBuildingPort
				.getOfficeBuildingPort();

		// Create and return the process to output the URL
		return new OfficeBuildingUrlManagedProcess(officeBuildingHost,
				officeBuildingPort);
	}

	/**
	 * {@link ManagedProcess} to output the {@link OfficeBuilding} URL.
	 */
	public static class OfficeBuildingUrlManagedProcess implements
			ManagedProcess {

		/**
		 * {@link Serializable} version.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link OfficeBuilding} host.
		 */
		private final String officeBuildingHost;

		/**
		 * {@link OfficeBuilding} port.
		 */
		private final int officeBuildingPort;

		/**
		 * Initiate.
		 * 
		 * @param officeBuildingHost
		 *            {@link OfficeBuilding} host.
		 * @param officeBuildingPort
		 *            {@link OfficeBuilding} port.
		 */
		public OfficeBuildingUrlManagedProcess(String officeBuildingHost,
				int officeBuildingPort) {
			this.officeBuildingHost = officeBuildingHost;
			this.officeBuildingPort = officeBuildingPort;
		}

		/*
		 * ================== ManagedProcess ======================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Nothing to initialise
		}

		@Override
		public void main() throws Throwable {
			// Obtain the URL and output
			JMXServiceURL serviceUrl = OfficeBuildingManager
					.getOfficeBuildingJmxServiceUrl(this.officeBuildingHost,
							this.officeBuildingPort);
			System.out.println(serviceUrl.toString());
		}
	}

}