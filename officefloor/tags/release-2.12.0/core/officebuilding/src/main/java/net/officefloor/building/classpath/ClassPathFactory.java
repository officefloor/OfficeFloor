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
package net.officefloor.building.classpath;

/**
 * Factory for the creation of artifact class path entries.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassPathFactory {

	/**
	 * Creates the class path for the Artifact and its dependencies (via
	 * <code>META-INF/maven/.../pom.xml</code>) on input location.
	 * 
	 * @param artifactLocation
	 *            Artifact location.
	 * @throws Exception
	 *             If fails to construct class path.
	 */
	String[] createArtifactClassPath(String artifactLocation) throws Exception;

	/**
	 * Creates the class path for the Artifact and its dependencies.
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
	 * @throws Exception
	 *             If fails to construct class path.
	 */
	String[] createArtifactClassPath(String groupId, String artifactId,
			String version, String type, String classifier) throws Exception;

}