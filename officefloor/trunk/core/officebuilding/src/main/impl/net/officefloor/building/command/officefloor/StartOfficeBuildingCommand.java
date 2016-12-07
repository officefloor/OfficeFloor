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
import java.util.Properties;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.IsIsolateProcessesOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.JvmOptionOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.KeyStoreOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.KeyStorePasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingHostOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.UsernameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.WorkspaceOfficeFloorCommandParameter;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.console.OfficeBuilding;

/**
 * Starts the {@link OfficeBuildingManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class StartOfficeBuildingCommand implements OfficeFloorCommandFactory, OfficeFloorCommand {

	/**
	 * Host running the {@link OfficeBuilding}.
	 */
	private final OfficeBuildingHostOfficeFloorCommandParameter officeBuildingHost = new OfficeBuildingHostOfficeFloorCommandParameter();

	/**
	 * Port to run the {@link OfficeBuilding} on.
	 */
	private final OfficeBuildingPortOfficeFloorCommandParameter officeBuildingPort = new OfficeBuildingPortOfficeFloorCommandParameter();

	/**
	 * Key store {@link File}.
	 */
	private final KeyStoreOfficeFloorCommandParameter keyStore = new KeyStoreOfficeFloorCommandParameter();

	/**
	 * Password to the key store {@link File}.
	 */
	private final KeyStorePasswordOfficeFloorCommandParameter keyStorePassword = new KeyStorePasswordOfficeFloorCommandParameter();

	/**
	 * User name.
	 */
	private final UsernameOfficeFloorCommandParameter userName = new UsernameOfficeFloorCommandParameter();

	/**
	 * Password.
	 */
	private final PasswordOfficeFloorCommandParameter password = new PasswordOfficeFloorCommandParameter();

	/**
	 * Workspace.
	 */
	private final WorkspaceOfficeFloorCommandParameter workspace = new WorkspaceOfficeFloorCommandParameter();

	/**
	 * Flag indicating to isolate the {@link Process} instances.
	 */
	private final IsIsolateProcessesOfficeFloorCommandParameter isIsolateProcesses = new IsIsolateProcessesOfficeFloorCommandParameter();

	/**
	 * JVM options.
	 */
	private final JvmOptionOfficeFloorCommandParameter jvmOptions = new JvmOptionOfficeFloorCommandParameter();

	/**
	 * Environment {@link Properties}.
	 */
	private final Properties environment;

	/**
	 * Initiate.
	 * 
	 * @param environment
	 *            Environment {@link Properties}.
	 */
	public StartOfficeBuildingCommand(Properties environment) {
		this.environment = environment;
	}

	/*
	 * ===================== OfficeFloorCommandFactory =====================
	 */

	@Override
	public String getCommandName() {
		return "start";
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return new StartOfficeBuildingCommand(this.environment);
	}

	/*
	 * ======================== OfficeFloorCommand ==========================
	 */

	@Override
	public String getDescription() {
		return "Starts the OfficeBuilding";
	}

	@Override
	public OfficeFloorCommandParameter[] getParameters() {
		return new OfficeFloorCommandParameter[] { this.officeBuildingHost, this.officeBuildingPort, this.keyStore,
				this.keyStorePassword, this.userName, this.password, this.workspace, this.isIsolateProcesses,
				this.jvmOptions };
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context) throws Exception {
		// Nothing to initialise
	}

	@Override
	public ManagedProcess createManagedProcess(OfficeFloorCommandEnvironment environment) throws Exception {

		// Obtain the office building details
		String officeBuildingHost = this.officeBuildingHost.getOfficeBuildingHost();
		int officeBuildingPort = this.officeBuildingPort.getOfficeBuildingPort();
		File keyStore = this.keyStore.getKeyStore();
		String keyStorePassword = this.keyStorePassword.getKeyStorePassword();
		String userName = this.userName.getUserName();
		String password = this.password.getPassword();
		File workspace = this.workspace.getWorkspace();
		boolean isIsolateProcesses = this.isIsolateProcesses.isIsolateProcesses();
		String[] jvmOptions = this.jvmOptions.getJvmOptions();

		// Create and return managed process to start office building
		return new StartOfficeBuildingManagedProcess(officeBuildingHost, officeBuildingPort, keyStore, keyStorePassword,
				userName, password, workspace, isIsolateProcesses, this.environment, jvmOptions);
	}

	/**
	 * {@link ManagedProcess} to start the {@link OfficeBuilding}.
	 */
	private static class StartOfficeBuildingManagedProcess implements ManagedProcess {

		/**
		 * {@link Serializable} version.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Host running the {@link OfficeBuilding}.
		 */
		private final String officeBuildingHost;

		/**
		 * Port to run the {@link OfficeBuilding} on.
		 */
		private final int officeBuildingPort;

		/**
		 * Location of the key store {@link File}.
		 */
		private final String keyStoreLocation;

		/**
		 * Password to the key store {@link File}.
		 */
		private final String keyStorePassword;

		/**
		 * User name to allow connections.
		 */
		private final String userName;

		/**
		 * Password to allow connections.
		 */
		private final String password;

		/**
		 * Location of the work space. May be <code>null</code> to use default
		 * location.
		 */
		private final String workspaceLocation;

		/**
		 * Flag indicating to isolate the {@link Process} instances.
		 */
		private final boolean isIsolateProcesses;

		/**
		 * Environment {@link Properties}.
		 */
		private final Properties environment;

		/**
		 * JVM options for the {@link Process} instances.
		 */
		private final String[] jvmOptions;

		/**
		 * Initiate.
		 * 
		 * @param officeBuildingHost
		 *            Host running the {@link OfficeBuilding}.
		 * @param officeBuildingPort
		 *            Port to run the {@link OfficeBuilding} on.
		 * @param keyStore
		 *            Key store {@link File}.
		 * @param keyStorePassword
		 *            Password to the key store {@link File}.
		 * @param userName
		 *            User name to allow connections.
		 * @param password
		 *            Password to allow connections.
		 * @param workspace
		 *            Work space for the {@link OfficeBuilding}. May be
		 *            <code>null</code> to use default location.
		 * @param isIsolateProcesses
		 *            Flag indicating to isolate the {@link Process} instances.
		 * @param environment
		 *            Environment {@link Properties}.
		 * @param jvmOptions
		 *            JVM options for the {@link Process} instances.
		 */
		public StartOfficeBuildingManagedProcess(String officeBuildingHost, int officeBuildingPort, File keyStore,
				String keyStorePassword, String userName, String password, File workspace, boolean isIsolateProcesses,
				Properties environment, String[] jvmOptions) {
			this.officeBuildingHost = officeBuildingHost;
			this.officeBuildingPort = officeBuildingPort;
			this.keyStoreLocation = keyStore.getAbsolutePath();
			this.keyStorePassword = keyStorePassword;
			this.userName = userName;
			this.password = password;
			this.workspaceLocation = (workspace == null ? null : workspace.getAbsolutePath());
			this.isIsolateProcesses = isIsolateProcesses;
			this.environment = environment;
			this.jvmOptions = jvmOptions;
		}

		/*
		 * ==================== ManagedProcess ========================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Nothing to initialise
		}

		@Override
		public void main() throws Throwable {

			// Create the environment for the OfficeBuilding
			Properties env = new Properties();
			env.putAll(this.environment);

			// Obtain the work space
			File workspace = null;
			if (this.workspaceLocation != null) {
				workspace = new File(this.workspaceLocation);
			}

			// Start the OfficeBuilding
			OfficeBuildingManagerMBean manager = OfficeBuildingManager.startOfficeBuilding(this.officeBuildingHost,
					this.officeBuildingPort, new File(this.keyStoreLocation), this.keyStorePassword, this.userName,
					this.password, workspace, this.isIsolateProcesses, env, null, this.jvmOptions, false);

			// Indicate started and location
			String serviceUrl = manager.getOfficeBuildingJmxServiceUrl();
			System.out.println("OfficeBuilding started at " + serviceUrl);
		}
	}

}