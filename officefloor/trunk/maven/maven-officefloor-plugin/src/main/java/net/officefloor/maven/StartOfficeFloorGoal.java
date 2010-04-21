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

import java.io.File;

import net.officefloor.building.OfficeBuilding;
import net.officefloor.building.classpath.ClassPathBuilderFactory;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Maven goal to start the {@link OfficeFloor}.
 * 
 * @goal start
 * 
 * @author Daniel Sagenschneider
 */
public class StartOfficeFloorGoal extends AbstractMojo {

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
		assertNotNull("Port not configured for the "
				+ OfficeBuilding.class.getSimpleName(), this.port);

		// Create the Class Path Builder
		ClassPathBuilderFactory classPathBuilderFactory;
		try {
			classPathBuilderFactory = new ClassPathBuilderFactory(null);
		} catch (Throwable ex) {
			throw new MojoExecutionException("Failed resolving the class path",
					ex);
		}

		// Start the OfficeBuilding
		OfficeBuildingManagerMBean officeBuilding;
		try {
			officeBuilding = OfficeBuildingManager.startOfficeBuilding(
					this.port.intValue(), classPathBuilderFactory);
		} catch (Throwable ex) {
			throw new MojoExecutionException("Failed starting the "
					+ OfficeBuilding.class.getSimpleName(), ex);
		}

		// Open the OfficeFloor
		// String name = officeBuilding.openOfficeFloor("MavenTestOfficeFloor",
		// jarName,
		// officeFloorLocation, jvmOptions);

		// Log the started OfficeFloor by process name
		this.getLog().info(
				"Started " + OfficeFloor.class.getSimpleName() + " '");

		// TODO remove - implement by starting an OfficeFloor
		this.getLog().error("TODO: implement");
		try {
			this.getLog().error(
					" creating file in directory"
							+ new File(".").getAbsolutePath());
			new File(".", "target").mkdir();
			new File(".", "target/OfficeFloorRun.txt").createNewFile();
			this.getLog().error("TODO: remove - file created");
		} catch (Throwable ex) {
			this.getLog().error("TODO: remove - failed create file", ex);
		}
	}

	/**
	 * Ensure the value is not <code>null</code>.
	 * 
	 * @param message
	 *            Message to report if value is <code>null</code>.
	 * @param value
	 *            Value to check.
	 * @throws MojoFailureException
	 *             If value is <code>null</code>.
	 */
	private static void assertNotNull(String message, Object value)
			throws MojoFailureException {
		if (value == null) {
			throw new MojoFailureException(message);
		}
	}

}