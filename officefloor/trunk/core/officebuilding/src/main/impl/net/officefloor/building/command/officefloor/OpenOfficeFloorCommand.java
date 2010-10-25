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
import net.officefloor.building.command.parameters.MultipleArtifactsOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.MultiplePathsOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.SingleValueOfficeFloorCommandParameter;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.officefloor.OfficeFloorManager;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorCommand} to open an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenOfficeFloorCommand implements OfficeFloorCommandFactory,
		OfficeFloorCommand {

	/**
	 * Name of {@link OfficeFloorCommandParameter} for the {@link OfficeFloor}
	 * location.
	 */
	public static final String PARAMETER_OFFICE_FLOOR_LOCATION = "officefloor";

	/**
	 * Name of {@link OfficeFloorCommandParameter} for the possible archive
	 * locations.
	 */
	public static final String PARAMETER_ARCHIVE_LOCATION = "jar";

	/**
	 * Name of {@link OfficeFloorCommandParameter} for possible artifacts.
	 */
	public static final String PARAMETER_ARTIFACT = "artifact";

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
		arguments.addOption(PARAMETER_ARTIFACT,
				MultipleArtifactsOfficeFloorCommandParameter
						.getArtifactArgumentValue(groupId, artifactId, version,
								type, classifier));
		arguments.addOfficeFloor(officeFloorLocation);
		return arguments.getCommandLine();
	}

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final SingleValueOfficeFloorCommandParameter officeFloorLocation = new SingleValueOfficeFloorCommandParameter(
			PARAMETER_OFFICE_FLOOR_LOCATION, "o");

	/**
	 * Archives to include on the class path.
	 */
	private final MultiplePathsOfficeFloorCommandParameter archives = new MultiplePathsOfficeFloorCommandParameter(
			PARAMETER_ARCHIVE_LOCATION, "j");

	/**
	 * Artifacts to include on the class path.
	 */
	private final MultipleArtifactsOfficeFloorCommandParameter artifacts = new MultipleArtifactsOfficeFloorCommandParameter(
			"artifact", "a");

	/*
	 * ================ OfficeFloorCommandFactory =====================
	 */

	@Override
	public String getCommandName() {
		return "open";
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return new OpenOfficeFloorCommand();
	}

	/*
	 * =================== OfficeFloorCommand =========================
	 */

	@Override
	public OfficeFloorCommandParameter[] getParameters() {
		return new OfficeFloorCommandParameter[] { this.officeFloorLocation,
				this.archives, this.artifacts };
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context)
			throws Exception {

		// Include the archives on the class path
		for (String archive : this.archives.getPaths()) {
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

		// Create the managed process to open the office floor
		String officeFloorLocation = this.officeFloorLocation.getValue();
		OfficeFloorManager managedProcess = new OfficeFloorManager(
				officeFloorLocation);

		// Return the managed process
		return managedProcess;
	}

}