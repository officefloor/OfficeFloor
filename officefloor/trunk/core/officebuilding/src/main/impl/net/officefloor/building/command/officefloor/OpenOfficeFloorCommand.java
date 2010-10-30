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

import net.officefloor.building.command.CommandLineBuilder;
import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ArtifactArgument;
import net.officefloor.building.command.parameters.ClassPathOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.JarOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.MultipleArtifactsOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeFloorLocationOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ParameterOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ProcessNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.TaskNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.WorkNameOfficeFloorCommandParameter;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.officefloor.OfficeFloorManager;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorCommand} to open an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenOfficeFloorCommand implements OfficeFloorCommandFactory,
		OfficeFloorCommand {

	/**
	 * Convenience method to create arguments for running {@link OfficeFloor}
	 * from an archive.
	 * 
	 * @param archiveLocation
	 *            Location of the archive.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @return Arguments.
	 */
	public static String[] createArguments(String archiveLocation,
			String officeFloorLocation) {
		CommandLineBuilder arguments = new CommandLineBuilder();
		arguments.addArchive(archiveLocation);
		arguments.addOfficeFloor(officeFloorLocation);
		return arguments.getCommandLine();
	}

	/**
	 * Convenience method to create arguments for running {@link OfficeFloor}
	 * from an artifact.
	 * 
	 * @param groupId
	 *            Group Id of the artifact.
	 * @param artifactId
	 *            Artifact Id.
	 * @param version
	 *            Version of the artifact.
	 * @param type
	 *            Type of artifact.
	 * @param classifier
	 *            Classifier for the artifact.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @return Arguments.
	 */
	public static String[] createArguments(String groupId, String artifactId,
			String version, String type, String classifier,
			String officeFloorLocation) {
		CommandLineBuilder arguments = new CommandLineBuilder();
		arguments
				.addOption(
						MultipleArtifactsOfficeFloorCommandParameter.PARAMETER_ARTIFACT,
						MultipleArtifactsOfficeFloorCommandParameter
								.getArtifactArgumentValue(groupId, artifactId,
										version, type, classifier));
		arguments.addOfficeFloor(officeFloorLocation);
		return arguments.getCommandLine();
	}

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final OfficeFloorLocationOfficeFloorCommandParameter officeFloorLocation = new OfficeFloorLocationOfficeFloorCommandParameter();

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
	 * {@link Process} name.
	 */
	private final ProcessNameOfficeFloorCommandParameter processName = new ProcessNameOfficeFloorCommandParameter();

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
	 * Indicates if {@link OfficeFloor} is to be opened within a spawned
	 * {@link Process}.
	 */
	private final boolean isSpawn;

	/**
	 * Initiate.
	 * 
	 * @param isSpawn
	 *            <code>true</code> if {@link OfficeFloor} is to be opened
	 *            within a spawned {@link Process}.
	 */
	public OpenOfficeFloorCommand(boolean isSpawn) {
		this.isSpawn = isSpawn;
	}

	/*
	 * ================ OfficeFloorCommandFactory =====================
	 */

	@Override
	public String getCommandName() {
		return "open";
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return new OpenOfficeFloorCommand(this.isSpawn);
	}

	/*
	 * =================== OfficeFloorCommand =========================
	 */

	@Override
	public String getDescription() {
		return "Opens an OfficeFloor";
	}

	@Override
	public OfficeFloorCommandParameter[] getParameters() {
		return new OfficeFloorCommandParameter[] { this.officeFloorLocation,
				this.archives, this.artifacts, this.classpath,
				this.processName, this.officeName, this.workName,
				this.taskName, this.parameter };
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

		// Indicate if OfficeFloor in spawned process
		environment.setSpawnProcess(this.isSpawn);

		// Specify the process name
		environment.setProcessName(this.processName.getProcessName());

		// Create the managed process to open the office floor
		String officeFloorLocation = this.officeFloorLocation
				.getOfficeFloorLocation();
		OfficeFloorManager managedProcess = new OfficeFloorManager(
				officeFloorLocation);

		// Obtain details of the possible task to open
		String officeName = this.officeName.getOfficeName();
		String workName = this.workName.getWorkName();
		String taskName = this.taskName.getTaskName();
		String parameter = this.parameter.getParameterValue();

		// Determine if invoking task (by checking work name provided)
		if (workName != null) {
			// Invoke the task
			managedProcess
					.invokeTask(officeName, workName, taskName, parameter);
		}

		// Return the managed process
		return managedProcess;
	}

}