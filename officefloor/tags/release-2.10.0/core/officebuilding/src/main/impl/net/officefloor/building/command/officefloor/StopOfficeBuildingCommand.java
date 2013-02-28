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

import java.io.File;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.KeyStoreOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.KeyStorePasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingHostOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.StopMaxWaitTimeOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.UsernameOfficeFloorCommandParameter;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.console.OfficeBuilding;

/**
 * {@link OfficeFloorCommand} to stop the {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class StopOfficeBuildingCommand implements OfficeFloorCommandFactory,
		OfficeFloorCommand {

	/**
	 * {@link OfficeBuilding} host.
	 */
	private final OfficeBuildingHostOfficeFloorCommandParameter officeBuildingHost = new OfficeBuildingHostOfficeFloorCommandParameter();

	/**
	 * {@link OfficeBuilding} port.
	 */
	private final OfficeBuildingPortOfficeFloorCommandParameter officeBuildingPort = new OfficeBuildingPortOfficeFloorCommandParameter();

	/**
	 * Trust store {@link File}.
	 */
	private final KeyStoreOfficeFloorCommandParameter trustStore = new KeyStoreOfficeFloorCommandParameter();

	/**
	 * Password to the trust store {@link File}.
	 */
	private final KeyStorePasswordOfficeFloorCommandParameter trustStorePassword = new KeyStorePasswordOfficeFloorCommandParameter();

	/**
	 * User name.
	 */
	private final UsernameOfficeFloorCommandParameter userName = new UsernameOfficeFloorCommandParameter();

	/**
	 * Password.
	 */
	private final PasswordOfficeFloorCommandParameter password = new PasswordOfficeFloorCommandParameter();

	/**
	 * Stop max wait time.
	 */
	private final StopMaxWaitTimeOfficeFloorCommandParameter stopMaxWaitTime = new StopMaxWaitTimeOfficeFloorCommandParameter();

	/*
	 * ==================== OfficeFloorCommandFactory =====================
	 */

	@Override
	public String getCommandName() {
		return "stop";
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return new StopOfficeBuildingCommand();
	}

	/*
	 * ==================== OfficeFloorCommandFactory =====================
	 */

	@Override
	public String getDescription() {
		return "Stops the OfficeBuilding";
	}

	@Override
	public OfficeFloorCommandParameter[] getParameters() {
		return new OfficeFloorCommandParameter[] { this.officeBuildingHost,
				this.officeBuildingPort, this.trustStore,
				this.trustStorePassword, this.userName, this.password,
				this.stopMaxWaitTime };
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context)
			throws Exception {
		// Nothing to initialise
	}

	@Override
	public ManagedProcess createManagedProcess(
			OfficeFloorCommandEnvironment environment) throws Exception {

		// Obtain the stop details
		String officeBuildingHost = this.officeBuildingHost
				.getOfficeBuildingHost();
		int officeBuildingPort = this.officeBuildingPort
				.getOfficeBuildingPort();
		File trustStore = this.trustStore.getKeyStore();
		String trustStorePassword = this.trustStorePassword
				.getKeyStorePassword();
		String userName = this.userName.getUserName();
		String password = this.password.getPassword();
		long stopMaxWaitTime = this.stopMaxWaitTime.getStopMaxWaitTime();

		// Create and return process to stop the OfficeBuilding
		return new StopOfficeBuildingManagedProcess(officeBuildingHost,
				officeBuildingPort, trustStore, trustStorePassword, userName,
				password, stopMaxWaitTime);
	}

	/**
	 * {@link ManagedProcess} to stop the {@link OfficeBuilding}.
	 */
	public static class StopOfficeBuildingManagedProcess implements
			ManagedProcess {

		/**
		 * {@link OfficeBuilding} host.
		 */
		private final String officeBuildingHost;

		/**
		 * {@link OfficeBuilding} port.
		 */
		private final int officeBuildingPort;

		/**
		 * Location of the trust store {@link File}.
		 */
		private final String trustStoreLocation;

		/**
		 * Password to the trust store {@link File}.
		 */
		private final String trustStorePassword;

		/**
		 * User name to connect.
		 */
		private final String userName;

		/**
		 * Password to connect.
		 */
		private final String password;

		/**
		 * Stop max wait time.
		 */
		private final long stopMaxWaitTime;

		/**
		 * Initiate.
		 * 
		 * @param officeBuildingHost
		 *            {@link OfficeBuilding} host.
		 * @param officeBuildingPort
		 *            {@link OfficeBuilding} port.
		 * @param trustStore
		 *            Trust store {@link File}.
		 * @param trustStorePassword
		 *            Password to the trust store {@link File}.
		 * @param userName
		 *            User name to connect.
		 * @param password
		 *            Password to connect.
		 * @param stopMaxWaitTime
		 *            Stop max wait time.
		 */
		public StopOfficeBuildingManagedProcess(String officeBuildingHost,
				int officeBuildingPort, File trustStore,
				String trustStorePassword, String userName, String password,
				long stopMaxWaitTime) {
			this.officeBuildingHost = officeBuildingHost;
			this.officeBuildingPort = officeBuildingPort;
			this.trustStoreLocation = trustStore.getAbsolutePath();
			this.trustStorePassword = trustStorePassword;
			this.userName = userName;
			this.password = password;
			this.stopMaxWaitTime = stopMaxWaitTime;
		}

		/*
		 * ===================== ManagedProcess ======================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Nothing to initialise
		}

		@Override
		public void main() throws Throwable {

			// Stop the OfficeBuilding
			OfficeBuildingManagerMBean manager = OfficeBuildingManager
					.getOfficeBuildingManager(this.officeBuildingHost,
							this.officeBuildingPort, new File(
									this.trustStoreLocation),
							this.trustStorePassword, this.userName,
							this.password);
			String stopDetails = manager
					.stopOfficeBuilding(this.stopMaxWaitTime);

			// Provide details of stopping the OfficeBuilding
			System.out.println(stopDetails);
		}
	}

}