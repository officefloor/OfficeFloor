/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.main.OfficeBuildingMain;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
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
	 * {@link MavenProject}.
	 * 
	 * @parameter expression="${project}"
	 */
	private MavenProject project;

	/**
	 * Port to run the {@link OfficeBuilding} on.
	 * 
	 * @parameter
	 * @required
	 */
	private Integer port;

	/*
	 * ======================== Mojo ==========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Ensure have configured values
		assertNotNull("Must have project", this.project);
		assertNotNull("Port not configured for the "
				+ OfficeBuildingMain.class.getSimpleName(), this.port);

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
			throw new MojoExecutionException(
					"Failed obtaining Remote Repository URLs", ex);
		}

		// Start the OfficeBuilding
		try {
			OfficeBuildingManager.startOfficeBuilding(this.port.intValue(),
					null, remoteRepositoryURLs, null);
		} catch (Throwable ex) {
			// Provide details of the failure
			final String MESSAGE = "Failed starting the "
					+ OfficeBuildingMain.class.getSimpleName();
			this.getLog().error(
					MESSAGE + ": " + ex.getMessage() + " ["
							+ ex.getClass().getSimpleName() + "]");
			this.getLog().error(
					"DIAGNOSIS INFORMATION: classpath='"
							+ System.getProperty("java.class.path") + "'");

			// Propagate the failure
			throw new MojoExecutionException(MESSAGE, ex);
		}

		// Log started OfficeBuilding
		this.getLog().info(
				"Started " + OfficeBuildingMain.class.getSimpleName()
						+ " on port " + this.port.intValue());
	}
}