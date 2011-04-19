/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.maven;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.building.command.LocalRepositoryOfficeFloorCommandParameter;
import net.officefloor.building.command.RemoteRepositoryUrlsOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.RemoteRepositoryUrlsOfficeFloorCommandParameterImpl;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.console.OfficeBuilding;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Maven goal to start the {@link OfficeBuilding}.
 * 
 * @goal start
 * 
 * @author Daniel Sagenschneider
 */
public class StartOfficeBuildingGoal extends AbstractGoal {

	/**
	 * Default {@link OfficeBuilding} port.
	 */
	public static final Integer DEFAULT_OFFICE_BUILDING_PORT = Integer
			.valueOf(OfficeBuildingPortOfficeFloorCommandParameter.DEFAULT_OFFICE_BUILDING_PORT);

	/**
	 * Ensures the {@link OfficeBuilding} is running on the
	 * {@link #DEFAULT_OFFICE_BUILDING_PORT}.
	 * 
	 * @param project
	 *            {@link MavenProject}.
	 * @param localRepository
	 *            Local repository.
	 * @param log
	 *            {@link Log}.
	 * @throws MojoExecutionException
	 *             As per {@link Mojo} API.
	 * @throws MojoFailureException
	 *             As per {@link Mojo} API.
	 */
	public static void ensureDefaultOfficeBuildingAvailable(
			MavenProject project, ArtifactRepository localRepository, Log log)
			throws MojoExecutionException, MojoFailureException {

		// Ensure the OfficeBuilding is available
		if (!OfficeBuildingManager.isOfficeBuildingAvailable(null,
				DEFAULT_OFFICE_BUILDING_PORT.intValue())) {

			// OfficeBuilding not available, so start it
			StartOfficeBuildingGoal.createStartOfficeBuildingGoal(project,
					localRepository, DEFAULT_OFFICE_BUILDING_PORT.intValue(),
					log).execute();
		}
	}

	/**
	 * Creates the {@link StartOfficeBuildingGoal} with the required parameters.
	 * 
	 * @param project
	 *            {@link MavenProject}.
	 * @param localRepository
	 *            Local repository.
	 * @param port
	 *            Port to run {@link OfficeBuilding} on.
	 * @param log
	 *            {@link Log}.
	 * @return {@link StartOfficeBuildingGoal}.
	 */
	public static StartOfficeBuildingGoal createStartOfficeBuildingGoal(
			MavenProject project, ArtifactRepository localRepository,
			Integer port, Log log) {
		StartOfficeBuildingGoal goal = new StartOfficeBuildingGoal();
		goal.project = project;
		goal.localRepository = localRepository;
		goal.port = port;
		goal.setLog(log);
		return goal;
	}

	/**
	 * {@link MavenProject}.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;

	/**
	 * Local repository.
	 * 
	 * @parameter expression="${localRepository}"
	 * @required
	 */
	private ArtifactRepository localRepository;

	/**
	 * Port to run the {@link OfficeBuilding} on.
	 * 
	 * @parameter
	 */
	private Integer port = DEFAULT_OFFICE_BUILDING_PORT;

	/*
	 * ======================== Mojo ==========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Ensure have configured values
		assertNotNull("Must have project", this.project);
		assertNotNull("Must have local repository", this.localRepository);
		assertNotNull(
				"Port not configured for the "
						+ OfficeBuilding.class.getSimpleName(), this.port);

		// Obtain the remote repository URLs
		String[] remoteRepositoryURLs;
		try {
			List<String> urls = new LinkedList<String>();
			for (Object object : this.project.getRemoteArtifactRepositories()) {
				ArtifactRepository repository = (ArtifactRepository) object;
				urls.add(repository.getUrl());
			}
			remoteRepositoryURLs = urls.toArray(new String[0]);
		} catch (Throwable ex) {
			throw this.newMojoExecutionException(
					"Failed obtaining Remote Repository URLs", ex);
		}

		// Create the environment properties
		Properties environment = new Properties();
		environment.putAll(this.project.getProperties());
		environment
				.put(LocalRepositoryOfficeFloorCommandParameter.PARAMETER_LOCAL_REPOSITORY,
						this.localRepository.getBasedir());
		environment
				.put(RemoteRepositoryUrlsOfficeFloorCommandParameter.PARAMETER_REMOTE_REPOSITORY_URLS,
						RemoteRepositoryUrlsOfficeFloorCommandParameterImpl
								.transformForParameterValue(remoteRepositoryURLs));

		// Start the OfficeBuilding
		try {
			OfficeBuildingManager.startOfficeBuilding(this.port.intValue(),
					environment, null);
		} catch (Throwable ex) {
			// Provide details of the failure
			final String MESSAGE = "Failed starting the "
					+ OfficeBuilding.class.getSimpleName();
			this.getLog().error(
					MESSAGE + ": " + ex.getMessage() + " ["
							+ ex.getClass().getSimpleName() + "]");
			this.getLog().error(
					"DIAGNOSIS INFORMATION: classpath='"
							+ System.getProperty("java.class.path") + "'");

			// Propagate the failure
			throw this.newMojoExecutionException(MESSAGE, ex);
		}

		// Log started OfficeBuilding
		this.getLog().info(
				"Started " + OfficeBuilding.class.getSimpleName() + " on port "
						+ this.port.intValue());
	}

}