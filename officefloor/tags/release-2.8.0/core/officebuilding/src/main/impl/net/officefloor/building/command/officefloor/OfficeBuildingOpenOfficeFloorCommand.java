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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ArtifactReferencesOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.JvmOptionOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.KeyStoreOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.KeyStorePasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingHostOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeFloorLocationOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeFloorSourceOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ParameterOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ProcessNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PropertiesOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.RemoteRepositoryUrlsOfficeFloorCommandParameterImpl;
import net.officefloor.building.command.parameters.TaskNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.UploadArtifactsOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.UsernameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.WorkNameOfficeFloorCommandParameter;
import net.officefloor.building.manager.ArtifactReference;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.manager.OpenOfficeFloorConfiguration;
import net.officefloor.building.manager.UploadArtifact;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorCommand} to open an {@link OfficeFloor} via an
 * {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingOpenOfficeFloorCommand implements
		OfficeFloorCommandFactory, OfficeFloorCommand {

	/**
	 * Flag indicating if remote invocation of opening the {@link OfficeFloor}.
	 */
	private final boolean isRemote;

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
	 * {@link Process} name.
	 */
	private final ProcessNameOfficeFloorCommandParameter processName = new ProcessNameOfficeFloorCommandParameter();

	/**
	 * {@link OfficeFloorSource} class name.
	 */
	private final OfficeFloorSourceOfficeFloorCommandParameter officeFloorSource = new OfficeFloorSourceOfficeFloorCommandParameter();

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final OfficeFloorLocationOfficeFloorCommandParameter officeFloorLocation = new OfficeFloorLocationOfficeFloorCommandParameter();

	/**
	 * {@link Properties} for the {@link OfficeFloor}.
	 */
	private final PropertiesOfficeFloorCommandParameter officeFloorProperties = new PropertiesOfficeFloorCommandParameter();

	/**
	 * Artifacts to upload to be included on the class path.
	 */
	private final UploadArtifactsOfficeFloorCommandParameter uploadArtifacts = new UploadArtifactsOfficeFloorCommandParameter();

	/**
	 * References to artifacts to be included on the class path.
	 */
	private final ArtifactReferencesOfficeFloorCommandParameter artifactReferences = new ArtifactReferencesOfficeFloorCommandParameter();

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
	 * Parameter for {@link Task}.
	 */
	private final ParameterOfficeFloorCommandParameter parameter = new ParameterOfficeFloorCommandParameter();

	/**
	 * JVM options.
	 */
	private final JvmOptionOfficeFloorCommandParameter jvmOptions = new JvmOptionOfficeFloorCommandParameter();

	/**
	 * Remote repository URLs.
	 */
	private final RemoteRepositoryUrlsOfficeFloorCommandParameterImpl remoteRepositoryUrls = new RemoteRepositoryUrlsOfficeFloorCommandParameterImpl();

	/**
	 * Initiate.
	 * 
	 * @param isRemote
	 *            <true> to remotely invoke opening the {@link OfficeFloor}.
	 */
	public OfficeBuildingOpenOfficeFloorCommand(boolean isRemote) {
		this.isRemote = isRemote;
	}

	/**
	 * Obtains the {@link OpenOfficeFloorConfiguration}.
	 * 
	 * @return {@link OpenOfficeFloorConfiguration}.
	 * @throws Exception
	 *             If fails to obtain the {@link OpenOfficeFloorConfiguration}.
	 */
	public OpenOfficeFloorConfiguration getOpenOfficeFloorConfiguration()
			throws Exception {

		// Obtain the OfficeFloor details
		String processName = this.processName.getProcessName();
		String officeFloorSourceClassName = this.officeFloorSource
				.getOfficeFloorSourceClassName();
		String officeFloorLocation = this.officeFloorLocation
				.getOfficeFloorLocation();
		Properties officeFloorProperties = this.officeFloorProperties
				.getProperties();
		UploadArtifact[] uploadArtifacts = this.uploadArtifacts
				.getUploadArtifacts();
		ArtifactReference[] artifactReferences = this.artifactReferences
				.getArtifactReferences();
		String[] jvmOptions = this.jvmOptions.getJvmOptions();
		String[] remoteRepositoryUrls = this.remoteRepositoryUrls
				.getRemoteRepositoryUrls();
		String officeName = this.officeName.getOfficeName();
		String workName = this.workName.getWorkName();
		String taskName = this.taskName.getTaskName();
		String parameterValue = this.parameter.getParameterValue();

		// Create the open OfficeFloor configuration
		OpenOfficeFloorConfiguration configuration = new OpenOfficeFloorConfiguration(
				officeFloorLocation);
		configuration.setProcessName(processName);
		configuration.setOfficeFloorSourceClassName(officeFloorSourceClassName);
		for (String propertyName : officeFloorProperties.stringPropertyNames()) {
			String propertyValue = officeFloorProperties
					.getProperty(propertyName);
			configuration.addOfficeFloorProperty(propertyName, propertyValue);
		}
		for (UploadArtifact uploadArtifact : uploadArtifacts) {
			configuration.addUploadArtifact(uploadArtifact);
		}
		for (ArtifactReference artifactReference : artifactReferences) {
			configuration.addArtifactReference(artifactReference);
		}
		for (String jvmOption : jvmOptions) {
			configuration.addJvmOption(jvmOption);
		}
		for (String remoteRepositoryUrl : remoteRepositoryUrls) {
			configuration.addRemoteRepositoryUrl(remoteRepositoryUrl);
		}
		configuration.setOpenTask(officeName, workName, taskName,
				parameterValue);

		// Return the configuration
		return configuration;
	}

	/*
	 * ======================= OfficeFloorCommandFactory =====================
	 */

	@Override
	public String getCommandName() {
		return "open";
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return new OfficeBuildingOpenOfficeFloorCommand(this.isRemote);
	}

	/*
	 * ========================== OfficeFloorCommand =========================
	 */

	@Override
	public String getDescription() {
		return "Opens an OfficeFloor within the OfficeBuilding";
	}

	@Override
	public OfficeFloorCommandParameter[] getParameters() {

		// Create the listing of command parameters
		List<OfficeFloorCommandParameter> parameters = new ArrayList<OfficeFloorCommandParameter>(
				18);
		if (this.isRemote) {
			parameters.add(this.officeBuildingHost);
			parameters.add(this.officeBuildingPort);
			parameters.add(this.trustStore);
			parameters.add(this.trustStorePassword);
			parameters.add(this.userName);
			parameters.add(this.password);
			parameters.add(this.uploadArtifacts);
		}
		parameters.add(this.processName);
		parameters.add(this.officeFloorSource);
		parameters.add(this.officeFloorLocation);
		parameters.add(this.officeFloorProperties);
		parameters.add(this.artifactReferences);
		parameters.add(this.officeName);
		parameters.add(this.workName);
		parameters.add(this.taskName);
		parameters.add(this.parameter);
		parameters.add(this.jvmOptions);
		parameters.add(this.remoteRepositoryUrls);

		// Return the command parameters
		return parameters.toArray(new OfficeFloorCommandParameter[parameters
				.size()]);
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context)
			throws Exception {
		// Environment initialised by the OfficeBuilding
	}

	@Override
	public ManagedProcess createManagedProcess(
			OfficeFloorCommandEnvironment environment) throws Exception {

		// Obtain details to open OfficeFloor
		String officeBuildingHost = this.officeBuildingHost
				.getOfficeBuildingHost();
		int officeBuildingPort = this.officeBuildingPort
				.getOfficeBuildingPort();
		File trustStore = this.trustStore.getKeyStore();
		String trustStorePassword = this.trustStorePassword
				.getKeyStorePassword();
		String userName = this.userName.getUserName();
		String password = this.password.getPassword();

		// Obtain the open OfficeFloor configuration
		OpenOfficeFloorConfiguration openOfficeFloorConfiguration = this
				.getOpenOfficeFloorConfiguration();

		// Obtain details to invoke a task
		String officeName = this.officeName.getOfficeName();
		String workName = this.workName.getWorkName();
		String taskName = this.taskName.getTaskName();
		String parameterValue = this.parameter.getParameterValue();

		// Generate the output suffix
		StringBuilder outputSuffix = new StringBuilder();
		if (workName != null) {
			outputSuffix.append(" for work (office=");
			outputSuffix.append(officeName);
			outputSuffix.append(", work=");
			outputSuffix.append(workName);
			if (taskName != null) {
				outputSuffix.append(", task=");
				outputSuffix.append(taskName);
			}
			if (parameterValue != null) {
				outputSuffix.append(", parameter=");
				outputSuffix.append(parameterValue);
			}
			outputSuffix.append(")");
		}

		// Create and return managed process to open OfficeFloor
		return new OpenManagedProcess(officeBuildingHost, officeBuildingPort,
				trustStore, trustStorePassword, userName, password,
				openOfficeFloorConfiguration, outputSuffix.toString());
	}

	/**
	 * {@link ManagedProcess} to open the {@link OfficeFloor} within a
	 * {@link OfficeBuilding}.
	 */
	public static class OpenManagedProcess implements ManagedProcess {

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
		 * {@link OpenOfficeFloorConfiguration}.
		 */
		private final OpenOfficeFloorConfiguration openOfficeFloorConfiguration;

		/**
		 * Suffix of output indicating the opening of the {@link OfficeFloor}.
		 */
		private final String outputSuffix;

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
		 * @param openOfficeFloorConfiguration
		 *            {@link OpenOfficeFloorConfiguration}.
		 * @param outputSuffix
		 *            Suffix of output indicating the opening of the
		 *            {@link OfficeFloor}.
		 */
		public OpenManagedProcess(String officeBuildingHost,
				int officeBuildingPort, File trustStore,
				String trustStorePassword, String userName, String password,
				OpenOfficeFloorConfiguration openOfficeFloorConfiguration,
				String outputSuffix) {
			this.officeBuildingHost = officeBuildingHost;
			this.officeBuildingPort = officeBuildingPort;
			this.trustStoreLocation = trustStore.getAbsolutePath();
			this.trustStorePassword = trustStorePassword;
			this.userName = userName;
			this.password = password;
			this.openOfficeFloorConfiguration = openOfficeFloorConfiguration;
			this.outputSuffix = outputSuffix;
		}

		/*
		 * ================= ManagedProcess ===========================
		 */

		@Override
		public void init(ManagedProcessContext context) throws Throwable {
			// Nothing to initialise
		}

		@Override
		public void main() throws Throwable {

			// Obtain the OfficeBuilding manager
			OfficeBuildingManagerMBean manager = OfficeBuildingManager
					.getOfficeBuildingManager(this.officeBuildingHost,
							this.officeBuildingPort, new File(
									this.trustStoreLocation),
							this.trustStorePassword, this.userName,
							this.password);

			// Open the OfficeFloor
			String processNamespace = manager
					.openOfficeFloor(this.openOfficeFloorConfiguration);

			// Construct message for OfficeFloor
			StringBuilder message = new StringBuilder();
			message.append("OfficeFloor open under process name space '");
			message.append(processNamespace);
			message.append("'");
			message.append(this.outputSuffix);

			// Output opened OfficeFloor
			System.out.println(message.toString());
		}
	}

}