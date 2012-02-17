/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.util.Properties;

import net.officefloor.building.command.LocalRepositoryOfficeFloorCommandParameter;
import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.RemoteRepositoryUrlsOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.KeyStoreOfficeFloorCommandParameterImpl;
import net.officefloor.building.command.parameters.KeyStorePasswordOfficeFloorCommandParameterImpl;
import net.officefloor.building.command.parameters.LocalRepositoryOfficeFloorCommandParameterImpl;
import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PasswordOfficeFloorCommandParameterImpl;
import net.officefloor.building.command.parameters.RemoteRepositoryUrlsOfficeFloorCommandParameterImpl;
import net.officefloor.building.command.parameters.UsernameOfficeFloorCommandParameterImpl;
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
public class StartOfficeBuildingCommand implements OfficeFloorCommandFactory,
		OfficeFloorCommand {

	/**
	 * Port to run the {@link OfficeBuilding} on.
	 */
	private final OfficeBuildingPortOfficeFloorCommandParameter officeBuildingPort = new OfficeBuildingPortOfficeFloorCommandParameter();

	/**
	 * Key store {@link File}.
	 */
	private final KeyStoreOfficeFloorCommandParameterImpl keyStore = new KeyStoreOfficeFloorCommandParameterImpl();

	/**
	 * Password to the key store {@link File}.
	 */
	private final KeyStorePasswordOfficeFloorCommandParameterImpl keyStorePassword = new KeyStorePasswordOfficeFloorCommandParameterImpl();

	/**
	 * User name.
	 */
	private final UsernameOfficeFloorCommandParameterImpl userName = new UsernameOfficeFloorCommandParameterImpl();

	/**
	 * Password.
	 */
	private final PasswordOfficeFloorCommandParameterImpl password = new PasswordOfficeFloorCommandParameterImpl();

	/**
	 * Location of the local repository.
	 */
	private final LocalRepositoryOfficeFloorCommandParameterImpl localRepository = new LocalRepositoryOfficeFloorCommandParameterImpl();

	/**
	 * Remote repository URLs.
	 */
	private final RemoteRepositoryUrlsOfficeFloorCommandParameterImpl remoteRepositoryUrls = new RemoteRepositoryUrlsOfficeFloorCommandParameterImpl();

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
		return new OfficeFloorCommandParameter[] { this.officeBuildingPort,
				this.keyStore, this.keyStorePassword, this.userName,
				this.password, this.localRepository, this.remoteRepositoryUrls };
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context)
			throws Exception {
		// Nothing to initialise
	}

	@Override
	public ManagedProcess createManagedProcess(
			OfficeFloorCommandEnvironment environment) throws Exception {

		// Obtain the office building details
		int officeBuildingPort = this.officeBuildingPort
				.getOfficeBuildingPort();
		File keyStore = this.keyStore.getKeyStore();
		String keyStorePassword = this.keyStorePassword.getKeyStorePassword();
		String userName = this.userName.getUserName();
		String password = this.password.getPassword();
		File localRepository = this.localRepository.getLocalRepository();
		String[] remoteRepositoryUrls = this.remoteRepositoryUrls
				.getRemoteRepositoryUrls();

		// Obtain the local repository location
		String localRepositoryLocation = (localRepository == null ? null
				: localRepository.getAbsolutePath());

		// Create and return managed process to start office building
		return new StartOfficeBuildingManagedProcess(officeBuildingPort,
				keyStore, keyStorePassword, userName, password,
				localRepositoryLocation, remoteRepositoryUrls, this.environment);
	}

	/**
	 * {@link ManagedProcess} to start the {@link OfficeBuilding}.
	 */
	private static class StartOfficeBuildingManagedProcess implements
			ManagedProcess {

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
		 * Location of the local repository.
		 */
		private final String localRepository;

		/**
		 * Remote repository URLs.
		 */
		private final String[] remoteRepositoryUrls;

		/**
		 * Environment {@link Properties}.
		 */
		private final Properties environment;

		/**
		 * Initiate.
		 * 
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
		 * @param localRepository
		 *            Location of the local repository.
		 * @param remoteRepositoryUrls
		 *            Remote repository URLs.
		 * @param environment
		 *            Environment {@link Properties}.
		 */
		public StartOfficeBuildingManagedProcess(int officeBuildingPort,
				File keyStore, String keyStorePassword, String userName,
				String password, String localRepository,
				String[] remoteRepositoryUrls, Properties environment) {
			this.officeBuildingPort = officeBuildingPort;
			this.keyStoreLocation = keyStore.getAbsolutePath();
			this.keyStorePassword = keyStorePassword;
			this.userName = userName;
			this.password = password;
			this.localRepository = localRepository;
			this.remoteRepositoryUrls = remoteRepositoryUrls;
			this.environment = environment;
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

			// Override local repository if provided by parameter
			if (this.localRepository != null) {
				env.put(LocalRepositoryOfficeFloorCommandParameter.PARAMETER_LOCAL_REPOSITORY,
						this.localRepository);
			}

			// Override remote repository URLs if provided by parameter
			if (this.remoteRepositoryUrls.length > 0) {
				env.put(RemoteRepositoryUrlsOfficeFloorCommandParameter.PARAMETER_REMOTE_REPOSITORY_URLS,
						RemoteRepositoryUrlsOfficeFloorCommandParameterImpl
								.transformForParameterValue(this.remoteRepositoryUrls));
			}

			// Start the OfficeBuilding
			OfficeBuildingManagerMBean manager = OfficeBuildingManager
					.startOfficeBuilding(this.officeBuildingPort, new File(
							this.keyStoreLocation), this.keyStorePassword,
							this.userName, this.password, env, null);

			// Indicate started and location
			String serviceUrl = manager.getOfficeBuildingJmxServiceUrl();
			System.out.println("OfficeBuilding started at " + serviceUrl);
		}
	}

}