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
import java.io.Serializable;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.FunctionNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.KeyStoreOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.KeyStorePasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingHostOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ParameterOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeFloorNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.UsernameOfficeFloorCommandParameter;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.compile.mbean.OfficeFloorMBean;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorCommandFactory} to invoke a {@link ManagedFunction} within
 * an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingInvokeOfficeFloorCommand implements OfficeFloorCommandFactory, OfficeFloorCommand {

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
	private final OfficeFloorNameOfficeFloorCommandParameter officeFloorName = new OfficeFloorNameOfficeFloorCommandParameter();

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
	 * {@link Office} name.
	 */
	private final OfficeNameOfficeFloorCommandParameter officeName = new OfficeNameOfficeFloorCommandParameter();

	/**
	 * {@link ManagedFunction} name.
	 */
	private final FunctionNameOfficeFloorCommandParameter functionName = new FunctionNameOfficeFloorCommandParameter();

	/**
	 * Parameter value for {@link ManagedFunction}.
	 */
	private final ParameterOfficeFloorCommandParameter parameter = new ParameterOfficeFloorCommandParameter();

	/*
	 * ================= OfficeFloorCommandFactory ==================
	 */

	@Override
	public String getCommandName() {
		return "invoke";
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return new OfficeBuildingInvokeOfficeFloorCommand();
	}

	/*
	 * ======================= OfficeFloorCommand ===================
	 */

	@Override
	public String getDescription() {
		return "Invokes a Task within a running OfficeFloor";
	}

	@Override
	public OfficeFloorCommandParameter[] getParameters() {
		return new OfficeFloorCommandParameter[] { this.officeBuildingHost, this.officeBuildingPort,
				this.officeFloorName, this.trustStore, this.trustStorePassword, this.userName, this.password,
				this.officeName, this.functionName, this.parameter };
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context) throws Exception {
		// Nothing to initialise
	}

	@Override
	public ManagedProcess createManagedProcess(OfficeFloorCommandEnvironment environment) throws Exception {

		// Obtain the task invocation details
		String officeBuildingHost = this.officeBuildingHost.getOfficeBuildingHost();
		int officeBuildingPort = this.officeBuildingPort.getOfficeBuildingPort();
		String officeFloorName = this.officeFloorName.getOfficeFloorName();
		File trustStore = this.trustStore.getKeyStore();
		String trustStorePassword = this.trustStorePassword.getKeyStorePassword();
		String userName = this.userName.getUserName();
		String password = this.password.getPassword();
		String officeName = this.officeName.getOfficeName();
		String functionName = this.functionName.getFunctionName();
		String parameter = this.parameter.getParameterValue();

		// Create and return process to invoke the function
		return new InvokeManagedProcess(officeBuildingHost, officeBuildingPort, officeFloorName, trustStore,
				trustStorePassword, userName, password, officeName, functionName, parameter);
	}

	/**
	 * {@link ManagedProcess} to invoke a {@link ManagedFunction}.
	 */
	public static class InvokeManagedProcess implements ManagedProcess {

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
		 * Name of the {@link OfficeFloor}.
		 */
		private final String officeFloorName;

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
		 * {@link Office} name.
		 */
		private final String officeName;

		/**
		 * {@link ManagedFunction} name.
		 */
		private final String functionName;

		/**
		 * Parameter value for the {@link ManagedFunction}.
		 */
		private final String parameter;

		/**
		 * Initiate.
		 * 
		 * @param officeBuildingHost
		 *            {@link OfficeBuilding} host.
		 * @param officeBuildingPort
		 *            {@link OfficeBuilding} port.
		 * @param officeFloorName
		 *            Name of the {@link OfficeFloor}.
		 * @param trustStore
		 *            Trust store {@link File}.
		 * @param trustStorePassword
		 *            Password to the trust store {@link File}.
		 * @param userName
		 *            User name to connect.
		 * @param password
		 *            Password to connect.
		 * @param officeName
		 *            {@link Office} name.
		 * @param functionName
		 *            {@link ManagedFunction} name.
		 * @param parameter
		 *            Parameter value for the {@link ManagedFunction}.
		 */
		public InvokeManagedProcess(String officeBuildingHost, int officeBuildingPort, String officeFloorName,
				File trustStore, String trustStorePassword, String userName, String password, String officeName,
				String functionName, String parameter) {
			this.officeBuildingHost = officeBuildingHost;
			this.officeBuildingPort = officeBuildingPort;
			this.officeFloorName = officeFloorName;
			this.trustStoreLocation = trustStore.getAbsolutePath();
			this.trustStorePassword = trustStorePassword;
			this.userName = userName;
			this.password = password;
			this.officeName = officeName;
			this.functionName = functionName;
			this.parameter = parameter;
		}

		/*
		 * ==================== ManagedProcess ==========================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Nothing to initialise
		}

		@Override
		public void main() throws Throwable {

			// Obtain the OfficeFloor manager
			OfficeFloorMBean officeFloorManager = OfficeBuildingManager.getOfficeFloorManager(this.officeBuildingHost,
					this.officeBuildingPort, this.officeFloorName, new File(this.trustStoreLocation),
					this.trustStorePassword, this.userName, this.password);

			// Invoke the function within the OfficeFloor
			officeFloorManager.invokeFunction(this.officeName, this.functionName, this.parameter);

			// Indicate started
			System.out.println("Invoked function " + this.functionName + " on office " + this.officeName
					+ (this.parameter != null ? " with parameter " + this.parameter : ""));
		}
	}

}