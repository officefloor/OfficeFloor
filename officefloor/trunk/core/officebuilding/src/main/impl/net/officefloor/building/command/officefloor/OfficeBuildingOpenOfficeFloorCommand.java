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

import java.util.Properties;

import net.officefloor.building.command.CommandLineBuilder;
import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ArtifactArgument;
import net.officefloor.building.command.parameters.ClassPathOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.JarOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.JvmOptionOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.MultipleArtifactsOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingHostOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeFloorLocationOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ParameterOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ProcessNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PropertiesOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.TaskNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.WorkNameOfficeFloorCommandParameter;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
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
	 * {@link OfficeBuilding} host.
	 */
	private final OfficeBuildingHostOfficeFloorCommandParameter officeBuildingHost = new OfficeBuildingHostOfficeFloorCommandParameter();

	/**
	 * {@link OfficeBuilding} port.
	 */
	private final OfficeBuildingPortOfficeFloorCommandParameter officeBuildingPort = new OfficeBuildingPortOfficeFloorCommandParameter();

	/**
	 * JVM options.
	 */
	private final JvmOptionOfficeFloorCommandParameter jvmOptions = new JvmOptionOfficeFloorCommandParameter();

	/**
	 * {@link Process} name.
	 */
	private final ProcessNameOfficeFloorCommandParameter processName = new ProcessNameOfficeFloorCommandParameter();

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final OfficeFloorLocationOfficeFloorCommandParameter officeFloorLocation = new OfficeFloorLocationOfficeFloorCommandParameter();

	/**
	 * {@link Properties} for the {@link OfficeFloor}.
	 */
	private final PropertiesOfficeFloorCommandParameter officeFloorProperties = new PropertiesOfficeFloorCommandParameter();

	/**
	 * Archives to include on the class path.
	 */
	private final JarOfficeFloorCommandParameter archives = new JarOfficeFloorCommandParameter();

	/**
	 * Artifacts to include on the class path.
	 */
	private final MultipleArtifactsOfficeFloorCommandParameter artifacts = new MultipleArtifactsOfficeFloorCommandParameter();

	/**
	 * Addition to the class path.
	 */
	private final ClassPathOfficeFloorCommandParameter classpath = new ClassPathOfficeFloorCommandParameter();

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

	/*
	 * ======================= OfficeFloorCommandFactory =====================
	 */

	@Override
	public String getCommandName() {
		return "open";
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return new OfficeBuildingOpenOfficeFloorCommand();
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
		return new OfficeFloorCommandParameter[] { this.officeBuildingHost,
				this.officeBuildingPort, this.jvmOptions, this.processName,
				this.officeFloorLocation, this.officeFloorProperties,
				this.archives, this.artifacts, this.classpath, this.officeName,
				this.workName, this.taskName, this.parameter };
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context)
			throws Exception {

		// Include the raw class paths
		for (String classPathEntry : this.classpath.getClassPathEntries()) {
			context.includeClassPathEntry(classPathEntry);
		}

		// Include the archives on the class path
		for (String archive : this.archives.getArchives()) {
			context.includeClassPathArtifact(archive);
		}

		// Include the artifacts on the class path
		for (ArtifactArgument artifact : this.artifacts.getArtifacts()) {
			context.includeClassPathArtifact(artifact.getGroupId(), artifact
					.getArtifactId(), artifact.getVersion(),
					artifact.getType(), artifact.getClassifier());
		}
	}

	@Override
	public ManagedProcess createManagedProcess(
			OfficeFloorCommandEnvironment environment) throws Exception {

		// Obtain details to open OfficeFloor
		String officeBuildingHost = this.officeBuildingHost
				.getOfficeBuildingHost();
		int officeBuildingPort = this.officeBuildingPort
				.getOfficeBuildingPort();

		// Construct the arguments
		CommandLineBuilder arguments = new CommandLineBuilder();

		// Add the JVM options
		for (String jvmOption : this.jvmOptions.getJvmOptions()) {
			arguments.addJvmOption(jvmOption);
		}

		// Specify process name
		String processName = this.processName.getProcessName();
		environment.setProcessName(processName);
		arguments.addProcessName(processName);

		// Add the OfficeFloor
		arguments.addOfficeFloor(this.officeFloorLocation
				.getOfficeFloorLocation());

		// Add the properties for the OfficeFloor
		arguments.addOfficeFloorProperties(this.officeFloorProperties
				.getProperties());

		// Add archives
		for (String archive : this.archives.getArchives()) {
			arguments.addArchive(archive);
		}

		// Add the artifacts
		for (ArtifactArgument artifact : this.artifacts.getArtifacts()) {
			arguments.addArtifact(artifact.getId());
		}

		// Add the class path entries
		for (String classPathEntry : this.classpath.getClassPathEntries()) {
			arguments.addClassPathEntry(classPathEntry);
		}

		// Obtain details to invoke a task
		String officeName = this.officeName.getOfficeName();
		String workName = this.workName.getWorkName();
		String taskName = this.taskName.getTaskName();
		String parameterValue = this.parameter.getParameterValue();

		// Add the invoke task arguments (arguments only added if available)
		arguments.addInvokeTask(officeName, workName, taskName, parameterValue);

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
				arguments.getCommandLine(), outputSuffix.toString());
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
		 * Arguments to open the {@link OfficeFloor}.
		 */
		private final String[] arguments;

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
		 * @param arguments
		 *            Arguments to open the {@link OfficeFloor}.
		 * @param outputSuffix
		 *            Suffix of output indicating the opening of the
		 *            {@link OfficeFloor}.
		 */
		public OpenManagedProcess(String officeBuildingHost,
				int officeBuildingPort, String[] arguments, String outputSuffix) {
			this.officeBuildingHost = officeBuildingHost;
			this.officeBuildingPort = officeBuildingPort;
			this.arguments = arguments;
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
							this.officeBuildingPort);

			// Open the OfficeFloor
			String processNamespace = manager.openOfficeFloor(this.arguments);

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