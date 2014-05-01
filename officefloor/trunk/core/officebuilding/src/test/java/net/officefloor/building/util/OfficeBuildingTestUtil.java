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
package net.officefloor.building.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import javax.management.MBeanServer;

import junit.framework.TestCase;
import net.officefloor.building.classpath.ClassPathFactoryImpl;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.ModelReader;
import org.codehaus.plexus.DefaultPlexusContainer;

/**
 * Utility methods for testing the {@link OfficeBuilding} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingTestUtil {

	/**
	 * Index to obtain a unique local repository for testing.
	 */
	private static int localRepositoryTestIndex = 1;

	/**
	 * Ensures the {@link OfficeBuildingManagerMBean} on the port is stopped.
	 * 
	 * @param port
	 *            Port.
	 */
	public static void ensureOfficeBuildingStopped(int port) {

		OfficeBuildingManagerMBean manager = null;
		try {
			// Attempt to connect to Office Building
			manager = OfficeBuildingManager.getOfficeBuildingManager(null,
					port, getTrustStore(), getTrustStorePassword(),
					getLoginUsername(), getLoginPassword());

		} catch (Exception ex) {
			// Assume not running
		}

		// Connected, so stop the Office Building
		if (manager != null) {
			try {
				manager.stopOfficeBuilding(1000);
			} catch (Exception ex) {
				throw OfficeFrameTestCase.fail(ex);
			}
		}
	}

	/**
	 * Convenience method to start the {@link OfficeBuildingManagerMBean} using
	 * details of this utility class.
	 * 
	 * @param port
	 *            Port.
	 * @return {@link OfficeBuildingManagerMBean}.
	 * @throws Exception
	 *             If fails to start the {@link OfficeBuildingManager}.
	 */
	public static OfficeBuildingManagerMBean startOfficeBuilding(int port)
			throws Exception {

		// Obtain the details for the Office Building
		File keyStore = getKeyStore();
		String keyStorePassword = getKeyStorePassword();
		String username = getLoginUsername();
		String password = getLoginPassword();
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		String[] remoteRepositoryUrls = new String[] { "file://"
				+ OfficeBuildingTestUtil.getUserLocalRepository()
						.getAbsolutePath() };

		// Start the office building
		return OfficeBuildingManager.startOfficeBuilding(null, port, keyStore,
				keyStorePassword, username, password, null, false,
				new Properties(), mbeanServer, new String[0], false,
				remoteRepositoryUrls);
	}

	/**
	 * Obtains the login username.
	 */
	public static String getLoginUsername() {
		return "admin";
	}

	/**
	 * Obtains the login password.
	 */
	public static String getLoginPassword() {
		return "password";
	}

	/**
	 * Key store {@link File}.
	 */
	public static File getKeyStore() throws FileNotFoundException {
		return new OfficeFrameTestCase() {
		}.findFile("src/main/resources/config/keystore.jks");
	}

	/**
	 * Password to the key store {@link File}.
	 */
	public static String getKeyStorePassword() {
		return "changeit";
	}

	/**
	 * Trust store {@link File}.
	 */
	public static File getTrustStore() throws FileNotFoundException {
		return new OfficeFrameTestCase() {
		}.findFile("src/main/resources/config/keystore.jks");
	}

	/**
	 * Password to the trust store {@link File}.
	 */
	public static String getTrustStorePassword() {
		return "changeit";
	}

	/**
	 * access.properties file as per JMX specification.
	 */
	public static File getAccessPropertiesFile() throws FileNotFoundException {
		return new OfficeFrameTestCase() {
		}.findFile("src/main/resources/config/access.properties");
	}

	/**
	 * password.properties file as per JMX specification.
	 */
	public static File getPasswordPropertiesFile() throws FileNotFoundException {
		return new OfficeFrameTestCase() {
		}.findFile("src/main/resources/config/password.properties");
	}

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
			localRepository = new File(tempDir, "test-local-repository-"
					+ localRepositoryTestIndex);
			localRepositoryTestIndex++;
		} while (localRepository.exists());

		// Ensure the directory exists
		TestCase.assertTrue("Failed to create local repository",
				localRepository.mkdirs());

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
				String version = name.substring(ARTIFACT_ID.length()
						+ "-".length());
				version = version.substring(0,
						version.length() - ".jar".length());

				// Return the version
				return version;
			}
		}

		// Not found, likely running in Eclipse with project import
		ModelReader reader = new DefaultPlexusContainer()
				.lookup(ModelReader.class);
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
		TestCase.fail("Unable to extract version for artifact '" + ARTIFACT_ID
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
	public static File getOfficeCompilerArtifactJar() throws Exception {

		// Obtain version for artifact
		String version = getOfficeCompilerArtifactVersion();

		// Create the path to the Jar
		File jarFile = getArtifactFile(getUserLocalRepository(),
				"net.officefloor.core", "officecompiler", version, "jar");

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
		reader.close();

		// Ensure content in file
		TestCase.assertEquals("Content should be in file", expectedContent,
				content.toString());
	}

	/**
	 * Waits until the {@link Process} is complete (or times out).
	 * 
	 * @param manager
	 *            {@link ProcessManagerMBean} of {@link Process} to wait until
	 *            complete.
	 * @param details
	 *            Provides further details should {@link Process} time out. May
	 *            be <code>null</code>.
	 */
	public static void waitUntilProcessComplete(ProcessManagerMBean manager,
			FurtherDetails details) throws Exception {

		// Maximum run time (allow reasonable time to close)
		final int MAX_RUN_TIME = 20000;

		// Wait until process completes (or times out)
		long maxFinishTime = System.currentTimeMillis() + MAX_RUN_TIME;
		while (!manager.isProcessComplete()) {
			// Determine if taken too long
			if (System.currentTimeMillis() > maxFinishTime) {
				manager.destroyProcess();
				TestCase.fail("Processing took too long"
						+ (details == null ? "" : ": " + details.getMessage()));
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