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
import java.io.IOException;

import junit.framework.TestCase;
import net.officefloor.building.OfficeBuilding;
import net.officefloor.building.classpath.ClassPathBuilder;
import net.officefloor.building.classpath.ClassPathBuilderFactory;
import net.officefloor.building.process.ProcessManager;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

/**
 * Utility methods for testing the {@link OfficeBuilding} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingTestUtil {

	/**
	 * {@link ClassPathBuilderFactory}.
	 */
	private static ClassPathBuilderFactory classPathBuilderFactory;

	/**
	 * Local repository directory.
	 */
	private static File localRepositoryDirectory;

	/**
	 * Version of {@link OfficeFloor}.
	 */
	private static String officeFloorVersion;

	/**
	 * Obtains the test {@link ClassPathBuilderFactory}.
	 * 
	 * @return Test {@link ClassPathBuilderFactory}.
	 */
	public static ClassPathBuilderFactory getClassPathBuilderFactory()
			throws Exception {
		init();
		return classPathBuilderFactory;
	}

	/**
	 * Obtains the local repository directory.
	 * 
	 * @return Local repository directory.
	 */
	public static File getLocalRepositoryDirectory() throws Exception {
		init();
		return localRepositoryDirectory;
	}

	/**
	 * Obtains the {@link OfficeFloor} version.
	 * 
	 * @return {@link OfficeFloor} version.
	 */
	public static String getOfficeFloorVersion() throws Exception {
		init();
		return officeFloorVersion;
	}

	/**
	 * Obtains the class path for the {@link OfficeFloor} artifacts.
	 * 
	 * @param artifactIds
	 *            Listing of {@link OfficeFloor} artifacts.
	 * @return Class path with capitalised tags for replacement.
	 */
	public static String getOfficeFloorClassPath(String... artifactIds)
			throws Exception {

		// Initialise
		init();

		// Build the class path
		StringBuilder path = new StringBuilder();
		for (String artifactId : artifactIds) {
			path.append("MAVEN_REPO/net/officefloor/core/" + artifactId
					+ "/VERSION/" + artifactId + "-VERSION.jar:");
		}
		String classPath = path.toString();
		classPath = classPath.substring(0, classPath.length() - ":".length());

		// Replace tags in the class path
		classPath = classPath.replace("MAVEN_REPO", localRepositoryDirectory
				.getCanonicalPath());
		classPath = classPath.replace("VERSION", officeFloorVersion);
		classPath = classPath.replace(":", File.pathSeparator);

		// Return the class path
		return classPath;
	}

	/**
	 * Retrieves the {@link OfficeFloor} Jar.
	 * 
	 * @param artifactId
	 *            Artifact Id.
	 * @return Location of the retrieved Jar.
	 */
	public static String retrieveOfficeFloorJar(String artifactId)
			throws Exception {
		ClassPathBuilder builder = getClassPathBuilderFactory()
				.createClassPathBuilder();
		builder.includeArtifact("net.officefloor.core", artifactId,
				getOfficeFloorVersion());
		String classPath = builder.getBuiltClassPath();
		builder.close();
		String jarPath = classPath.split(File.pathSeparator)[0];
		return jarPath;
	}

	/**
	 * Initialises the {@link ClassPathBuilderFactory} and associated
	 * configuration.
	 */
	private static void init() throws Exception {

		// Lazy initialise
		if (classPathBuilderFactory != null) {
			return; // already initialised
		}

		// Obtain the directory for the local repository
		localRepositoryDirectory = new File(".", "target/localRepository");
		if (!localRepositoryDirectory.exists()) {
			localRepositoryDirectory.mkdir();
		}

		// Obtain the current OfficeBuilding version
		Model project = new MavenXpp3Reader().read(new FileReader(new File(".",
				"pom.xml")));
		officeFloorVersion = project.getParent().getVersion();
		TestCase.assertNotNull("Must have version", officeFloorVersion);

		// Obtain the Maven local repository as the remote repository.
		// (Stops download of artifacts and has latest OfficeFloor)
		MavenEmbedder maven = new MavenEmbedder();
		maven.setClassLoader(OfficeBuildingTestUtil.class.getClassLoader());
		maven.start();
		ArtifactRepository mavenRepository = maven.getLocalRepository();
		String mavenRepositoryUrl = mavenRepository.getUrl();
		maven.stop();
		TestCase.assertNotNull("Must have maven repository URL",
				mavenRepositoryUrl);

		// Create the class path builder factory
		classPathBuilderFactory = new ClassPathBuilderFactory(
				localRepositoryDirectory.getAbsolutePath(), mavenRepositoryUrl);
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