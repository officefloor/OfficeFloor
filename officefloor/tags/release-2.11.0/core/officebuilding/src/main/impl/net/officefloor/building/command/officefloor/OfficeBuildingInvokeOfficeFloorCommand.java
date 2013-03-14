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
import net.officefloor.building.command.parameters.OfficeNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ParameterOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ProcessNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.TaskNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.UsernameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.WorkNameOfficeFloorCommandParameter;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.building.process.officefloor.OfficeFloorManagerMBean;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorCommandFactory} to invoke a {@link Task} within an
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingInvokeOfficeFloorCommand implements
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
	 * {@link Work} name.
	 */
	private final WorkNameOfficeFloorCommandParameter workName = new WorkNameOfficeFloorCommandParameter();

	/**
	 * {@link Task} name.
	 */
	private final TaskNameOfficeFloorCommandParameter taskName = new TaskNameOfficeFloorCommandParameter();

	/**
	 * Parameter value for {@link Task}.
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
		return new OfficeFloorCommandParameter[] { this.officeBuildingHost,
				this.officeBuildingPort, this.processName, this.trustStore,
				this.trustStorePassword, this.userName, this.password,
				this.officeName, this.workName, this.taskName, this.parameter };
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context)
			throws Exception {
		// Nothing to initialise
	}

	@Override
	public ManagedProcess createManagedProcess(
			OfficeFloorCommandEnvironment environment) throws Exception {

		// Obtain the task invocation details
		String officeBuildingHost = this.officeBuildingHost
				.getOfficeBuildingHost();
		int officeBuildingPort = this.officeBuildingPort
				.getOfficeBuildingPort();
		String processNamespace = this.processName.getProcessName();
		File trustStore = this.trustStore.getKeyStore();
		String trustStorePassword = this.trustStorePassword
				.getKeyStorePassword();
		String userName = this.userName.getUserName();
		String password = this.password.getPassword();
		String officeName = this.officeName.getOfficeName();
		String workName = this.workName.getWorkName();
		String taskName = this.taskName.getTaskName();
		String parameter = this.parameter.getParameterValue();

		// Create and return process to invoke the task
		return new InvokeManagedProcess(officeBuildingHost, officeBuildingPort,
				processNamespace, trustStore, trustStorePassword, userName,
				password, officeName, workName, taskName, parameter);
	}

	/**
	 * {@link ManagedProcess} to invoke a {@link Task}.
	 */
	public static class InvokeManagedProcess implements ManagedProcess {

		/**
		 * {@link OfficeBuilding} host.
		 */
		private final String officeBuildingHost;

		/**
		 * {@link OfficeBuilding} port.
		 */
		private final int officeBuildingPort;

		/**
		 * {@link OfficeFloor} {@link Process} name space.
		 */
		private final String processNamespace;

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
		 * {@link Work} name.
		 */
		private final String workName;

		/**
		 * {@link Task} name.
		 */
		private final String taskName;

		/**
		 * Parameter value for the {@link Task}.
		 */
		private final String parameter;

		/**
		 * Initiate.
		 * 
		 * @param officeBuildingHost
		 *            {@link OfficeBuilding} host.
		 * @param officeBuildingPort
		 *            {@link OfficeBuilding} port.
		 * @param processNamespace
		 *            {@link OfficeFloor} {@link Process} name space.
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
		 * @param workName
		 *            {@link Work} name.
		 * @param taskName
		 *            {@link Task} name.
		 * @param parameter
		 *            Parameter value for the {@link Task}.
		 */
		public InvokeManagedProcess(String officeBuildingHost,
				int officeBuildingPort, String processNamespace,
				File trustStore, String trustStorePassword, String userName,
				String password, String officeName, String workName,
				String taskName, String parameter) {
			this.officeBuildingHost = officeBuildingHost;
			this.officeBuildingPort = officeBuildingPort;
			this.processNamespace = processNamespace;
			this.trustStoreLocation = trustStore.getAbsolutePath();
			this.trustStorePassword = trustStorePassword;
			this.userName = userName;
			this.password = password;
			this.officeName = officeName;
			this.workName = workName;
			this.taskName = taskName;
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
			OfficeFloorManagerMBean officeFloorManager = OfficeBuildingManager
					.getOfficeFloorManager(this.officeBuildingHost,
							this.officeBuildingPort, this.processNamespace,
							new File(this.trustStoreLocation),
							this.trustStorePassword, this.userName,
							this.password);

			// Invoke the Task within the OfficeFloor
			officeFloorManager.invokeTask(this.officeName, this.workName,
					this.taskName, this.parameter);

			// Indicate started
			System.out.println("Invoked work "
					+ this.workName
					+ (this.taskName != null ? " (task " + this.taskName + ")"
							: "")
					+ " on office "
					+ this.officeName
					+ (this.parameter != null ? " with parameter "
							+ this.parameter : ""));
		}
	}

}