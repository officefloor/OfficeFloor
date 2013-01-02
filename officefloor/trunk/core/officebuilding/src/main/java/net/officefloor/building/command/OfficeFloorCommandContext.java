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
package net.officefloor.building.command;

import net.officefloor.building.process.ManagedProcess;

/**
 * Context for the {@link OfficeFloorCommand} to create a {@link ManagedProcess}
 * to undertake the command.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCommandContext {

	/**
	 * <p>
	 * Provides support for including a class path entry.
	 * <p>
	 * The class path entry is included as specified without interrogation for
	 * dependencies.
	 * 
	 * @param classPathEntry
	 *            Class path entry.
	 */
	void includeClassPathEntry(String classPathEntry);

	/**
	 * Provides support for including an Artifact and its dependencies (via
	 * <code>META-INF/maven/.../pom.xml</code>) on the class path.
	 * 
	 * @param artifactLocation
	 *            Artifact location.
	 */
	void includeClassPathArtifact(String artifactLocation);

	/**
	 * Provides support for including an Artifact and its dependencies on the
	 * class path.
	 * 
	 * @param groupId
	 *            Group Id.
	 * @param artifactId
	 *            Artifact Id.
	 * @param version
	 *            Version.
	 * @param type
	 *            Type. <code>null</code> will default to <code>jar</code>.
	 * @param classifier
	 *            Classifier, may be <code>null</code>.
	 */
	void includeClassPathArtifact(String groupId, String artifactId,
			String version, String type, String classifier);

}