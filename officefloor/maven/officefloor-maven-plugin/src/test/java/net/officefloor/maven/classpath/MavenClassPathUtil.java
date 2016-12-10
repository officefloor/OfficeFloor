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
package net.officefloor.maven.classpath;

import java.io.File;
import java.io.FileReader;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.ModelReader;

import junit.framework.TestCase;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Maven class path utility functions.
 *
 * @author Daniel Sagenschneider
 */
public class MavenClassPathUtil {

	/**
	 * Group Id for test artifacts.
	 */
	public static final String TEST_GROUP_ID = "test.officefloor.test";

	/**
	 * Version for the test artifacts.
	 */
	public static final String TEST_ARTIFACT_VERSION = "1.0.0";

	/**
	 * Artifact Id of jar with no dependencies.
	 */
	public static final String TEST_JAR_ARTIFACT_ID = "JarArtifact";

	/**
	 * Artifact Id of jar with dependencies.
	 */
	public static final String TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID = "JarWithDependenciesArtifact";

	/**
	 * Index to obtain a unique local repository for testing.
	 */
	private static int localRepositoryTestIndex = 1;

	/**
	 * Obtains the test local repository for testing.
	 * 
	 * @return Test local repository.
	 */
	public static File getTestLocalRepository() {

		// Obtain new directory for the test local repository
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File localRepository;
		do {
			localRepository = new File(tempDir, "test-local-repository-" + localRepositoryTestIndex);
			localRepositoryTestIndex++;
		} while (localRepository.exists());

		// Ensure the directory exists
		TestCase.assertTrue("Failed to create local repository", localRepository.mkdirs());

		// Return the local repository
		return localRepository;
	}

	/**
	 * Obtains the local repository of user executing the tests.
	 * 
	 * @return Local repository.
	 */
	public static File getUserLocalRepository() {
		try {
			return ClassPathFactoryImpl.getUserLocalRepository();
		} catch (Exception ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * <p>
	 * Obtains the version for the {@link OfficeFloor} compiler artifact.
	 * <p>
	 * OfficeCompiler should be built before OfficeBuilding so should be
	 * available within the repository.
	 * 
	 * @return Version of the {@link OfficeFloor} artifact.
	 */
	public static String getOfficeCompilerArtifactVersion() throws Exception {

		final String ARTIFACT_ID = "officecompiler";

		// Extract the artifact version from the class path
		String classPath = System.getProperty("java.class.path");
		String[] classPathEntries = classPath.split(File.pathSeparator);
		for (String entry : classPathEntries) {

			// Determine if entry
			String name = new File(entry).getName();
			if ((name.endsWith("jar")) && (name.startsWith(ARTIFACT_ID))) {
				// Found the artifact, so extract version
				String version = name.substring(ARTIFACT_ID.length() + "-".length());
				version = version.substring(0, version.length() - ".jar".length());

				// Return the version
				return version;
			}
		}

		// Not found, likely running in Eclipse with project import
		ModelReader reader = ClassPathFactoryImpl.createPlexusContainer().lookup(ModelReader.class);
		for (String entry : classPathEntries) {

			// Ignore non-directories
			File directory = new File(entry);
			if (!directory.isDirectory()) {
				continue;
			}

			// Search upwards for the pom.xml file
			File pomFile = new File(directory, "pom.xml");
			while ((!pomFile.exists()) && (directory != null)) {
				// pom.xml not exists, so try parent directory
				directory = directory.getParentFile();
				pomFile = new File(directory, "pom.xml");
			}

			// Ignore if no pom.xml file found
			if (!pomFile.exists()) {
				continue;
			}

			// Read in the pom.xml file for the version
			Model pom = reader.read(new FileReader(pomFile), null);

			// Ignore if not correct artifact
			String pomArtifactId = pom.getArtifactId();
			if (!pomArtifactId.equals(ARTIFACT_ID)) {
				continue;
			}

			// Obtain the version
			String version = pom.getVersion();
			if (version == null) {
				version = pom.getParent().getVersion();
			}

			// Return the version
			return version;
		}

		// Not able to obtain the version
		TestCase.fail("Unable to extract version for artifact '" + ARTIFACT_ID + "' from class path: " + classPath);
		return null; // fail to throw exception
	}

	/**
	 * Obtains the {@link File} to the {@link OfficeFloor} artifact Jar.
	 * 
	 * @param artifactId
	 *            Id of the {@link OfficeFloor} artifact.
	 * @return {@link File} to the the {@link OfficeFloor} artifact Jar.
	 */
	public static File getOfficeCompilerArtifactJar() throws Exception {

		// Obtain version for artifact
		String version = getOfficeCompilerArtifactVersion();

		// Create the path to the Jar
		File jarFile = getArtifactFile(getUserLocalRepository(), "net.officefloor.core", "officecompiler", version,
				"jar");

		// Return Jar file
		return jarFile;
	}

	/**
	 * Obtains the Artifact {@link File}.
	 * 
	 * @param repository
	 *            {@link Repository} root directory.
	 * @param groupIdPath
	 *            Group Id.
	 * @param artifactId
	 *            Artifact Id.
	 * @param version
	 *            Artifact version.
	 * @param type
	 *            Artifact type.
	 * @return {@link File} to the Artifact.
	 */
	public static File getArtifactFile(File repository, String groupId, String artifactId, String version,
			String type) {

		// Obtain the group path
		String groupPath = groupId.replace('.', File.separatorChar);

		// Create the path to the artifact
		File artifact = new File(repository, groupPath);
		artifact = new File(artifact, artifactId);
		artifact = new File(artifact, version);
		artifact = new File(artifact, artifactId + "-" + version + "." + type);

		// Return the artifact file
		return artifact;
	}

}