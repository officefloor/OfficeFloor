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

package net.officefloor.building.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.officefloor.building.command.OfficeFloorCommandContextImpl;
import net.officefloor.building.process.ProcessManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.main.OfficeBuildingMain;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

/**
 * Utility methods for testing the {@link OfficeBuildingMain} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingTestUtil {

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
	 * Artifact Id for ClassPathTestArtifact.
	 */
	@Deprecated
	public static final String CLASS_PATH_TEST_ARTIFACT_ID = "ClassPathTestArtifact";

	/**
	 * Obtains the local repository directory.
	 * 
	 * @return Local repository directory.
	 */
	public static File getLocalRepositoryDirectory() throws Exception {
		return OfficeFloorCommandContextImpl.getLocalRepositoryDirectory(null);
	}

	/**
	 * Obtains the Remote repository directory.
	 * 
	 * @return Remote repository directory.
	 */
	public static File getRemoteRepositoryDirectory() {
		File directory = new File(".", "target/test-classes/remoteRepository");
		TestCase.assertTrue("Remote repository directory not available: "
				+ directory.getAbsolutePath(), directory.exists());
		return directory;
	}

	/**
	 * Obtains the remote repository URLs.
	 * 
	 * @return Remote repository URLs.
	 */
	public static String[] getRemoteRepositoryUrls() throws Exception {
		return new String[] { "file://"
				+ getRemoteRepositoryDirectory().getCanonicalPath() };
	}

	/**
	 * Obtains the version for the {@link OfficeFloor} artifact.
	 * 
	 * @param artifactId
	 *            Id of the {@link OfficeFloor} artifact.
	 * @return Version of the {@link OfficeFloor} artifact.
	 */
	public static String getOfficeFloorArtifactVersion(String artifactId)
			throws Exception {

		// Determine if class path test artifact
		if (CLASS_PATH_TEST_ARTIFACT_ID.equals(artifactId)) {
			return TEST_ARTIFACT_VERSION;
		}

		// Extract the artifact version from the class path
		String classPath = System.getProperty("java.class.path");
		String[] classPathEntries = classPath.split(File.pathSeparator);
		for (String entry : classPathEntries) {

			// Determine if entry
			String name = new File(entry).getName();
			if ((name.endsWith("jar")) && (name.startsWith(artifactId))) {
				// Found the artifact, so extract version
				String version = name.substring(artifactId.length()
						+ "-".length());
				version = version.substring(0, version.length()
						- ".jar".length());

				// Return the version
				return version;
			}
		}

		// Not found, likely running in Eclipse with directory
		MavenXpp3Reader reader = new MavenXpp3Reader();
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
			Model pom = reader.read(new FileReader(pomFile));

			// Ignore if not correct artifact
			String pomArtifactId = pom.getArtifactId();
			if (!pomArtifactId.equals(artifactId)) {
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
		TestCase.fail("Unable to extract version for artifact '" + artifactId
				+ "' from class path: " + classPath);
		return null; // fail to throw exception
	}

	/**
	 * Obtains the {@link File} to the {@link OfficeFloor} artifact Jar.
	 * 
	 * @param artifactId
	 *            Id of the {@link OfficeFloor} artifact.
	 * @return {@link File} to the the {@link OfficeFloor} artifact Jar.
	 */
	public static File getOfficeFloorArtifactJar(String artifactId)
			throws Exception {

		// Obtain version for artifact
		String version = getOfficeFloorArtifactVersion(artifactId);

		// Create the path to the Jar
		File jarFile = getArtifactFile(getLocalRepositoryDirectory(),
				"net.officefloor.core", artifactId, version, "jar");

		// Return Jar file
		return jarFile;
	}

	/**
	 * Obtains the {@link Artifact} {@link File}.
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
	 * @return {@link File} to the {@link Artifact}.
	 */
	public static File getArtifactFile(File repository, String groupId,
			String artifactId, String version, String type) {

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

	/**
	 * Remove the ClassPathTestArtifact from local repository.
	 */
	public static void cleanupClassPathTestArtifactInLocalRepository()
			throws Exception {
		// Obtain location of POM file
		File classPathTestArtifactPom = getArtifactFile(
				getLocalRepositoryDirectory(), TEST_GROUP_ID,
				TEST_JAR_ARTIFACT_ID, TEST_ARTIFACT_VERSION, "pom");
		File classPathTestArtifactVersionDir = classPathTestArtifactPom
				.getParentFile();
		File classPathTestArtifactDir = classPathTestArtifactVersionDir
				.getParentFile();
		File classPathTestGroupDir = classPathTestArtifactDir.getParentFile();
		deleteDirectory(classPathTestGroupDir);
	}

	/**
	 * Setup for ClassPathTestArtifact for download.
	 */
	public static void setupClassPathTestArtifactForDownload() throws Exception {

		// Clean up
		cleanupClassPathTestArtifactInLocalRepository();

		// Obtain location of POM file
		File pom = getArtifactFile(getRemoteRepositoryDirectory(),
				TEST_GROUP_ID, CLASS_PATH_TEST_ARTIFACT_ID,
				TEST_ARTIFACT_VERSION, "pom");
		TestCase.assertTrue(CLASS_PATH_TEST_ARTIFACT_ID + "-"
				+ TEST_ARTIFACT_VERSION
				+ ".pom not available in remote repository: "
				+ getRemoteRepositoryDirectory(), pom.exists());

		// Read in POM contents
		StringWriter writer = new StringWriter();
		FileReader reader = new FileReader(pom);
		for (int value = reader.read(); value != -1; value = reader.read()) {
			writer.write(value);
		}
		reader.close();

		// Replace officecompiler version in POM file
		String officeCompilerVersion = getOfficeFloorArtifactVersion("officecompiler");
		String contents = writer.toString();
		contents = contents.replace("${officecompiler.version}",
				officeCompilerVersion);
		FileWriter pomWriter = new FileWriter(pom, false);
		pomWriter.write(contents);
		pomWriter.close();
	}

	/**
	 * Deletes the directory.
	 * 
	 * @param directory
	 *            Directory to be deleted.
	 */
	private static void deleteDirectory(File directory) {

		// Ensure exists
		if (!directory.exists()) {
			return; // not exists so do not delete
		}

		// Delete the child files
		for (File child : directory.listFiles()) {
			if (child.isDirectory()) {
				// Recursively delete the directory
				deleteDirectory(child);
			} else {
				// Delete the file
				TestCase.assertTrue("Failed to clear file: " + child, child
						.delete());
			}
		}

		// Child files deleted, so now delete the directory
		TestCase.assertTrue("Failed to clear directory: " + directory,
				directory.delete());
	}

	/**
	 * Creates a temporary file.
	 * 
	 * @param testCase
	 *            {@link TestCase} requiring the temporary file.
	 * @return Temporary file.
	 * @throws IOException
	 *             If fails to create temporary file.
	 */
	public static File createTempFile(TestCase testCase) throws IOException {

		// Obtain the file
		File file = File.createTempFile(testCase.getClass().getSimpleName(),
				testCase.getName());

		// Return the file
		return file;
	}

	/**
	 * Validates the contents of the file.
	 * 
	 * @param message
	 *            Message if contents are invalid.
	 * @param expectedContent
	 *            Expected content of the file.
	 * @param file
	 *            File to validate its content.
	 * @throws IOException
	 *             If fails to validate content.
	 */
	public static void validateFileContent(String message,
			String expectedContent, File file) throws IOException {

		// Obtain the content from file
		StringBuilder content = new StringBuilder();
		FileReader reader = new FileReader(file);
		for (int value = reader.read(); value != -1; value = reader.read()) {
			content.append((char) value);
		}

		// Ensure content in file
		TestCase.assertEquals("Content should be in file", expectedContent,
				content.toString());
	}

	/**
	 * Waits until the {@link Process} is complete (or times out).
	 */
	public static void waitUntilProcessComplete(ProcessManager manager)
			throws Exception {

		// Obtain the maximum run time
		final int MAX_RUN_TIME = 5000;

		// Wait until process completes (or times out)
		long maxFinishTime = System.currentTimeMillis() + MAX_RUN_TIME;
		while (!manager.isProcessComplete()) {
			// Determine if taken too long
			if (System.currentTimeMillis() > maxFinishTime) {
				manager.destroyProcess();
				TestCase.fail("Processing took too long");
			}

			// Wait some time for further processing
			Thread.sleep(100);
		}
	}

	/**
	 * All access via static methods.
	 */
	private OfficeBuildingTestUtil() {
	}

}