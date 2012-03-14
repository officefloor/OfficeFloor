/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.war;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import net.officefloor.building.decorate.OfficeFloorDecoratorContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link WarOfficeFloorDecoratorTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class WarOfficeFloorDecoratorTest extends OfficeFrameTestCase {

	/**
	 * Resolved archives.
	 */
	private final Map<String, File> resolvedArchives = new HashMap<String, File>();

	/**
	 * Maps an expected archive directory name to resolved archive name.
	 */
	private final Map<String, String> archiveNameMappings = new HashMap<String, String>();

	/**
	 * Environment {@link Properties}.
	 */
	private final Properties environment = new Properties();

	/**
	 * Command options.
	 */
	private final Map<String, List<String>> commandOptions = new HashMap<String, List<String>>();

	@Override
	protected void tearDown() throws Exception {
		// Clean up test
		System.clearProperty(WarOfficeFloorDecorator.SYSTEM_PROPERTY_PASSWORD_FILE_LOCATION);
		System.clearProperty(WarOfficeFloorDecorator.SYSTEM_PROPERTY_HTTP_PORT);
	}

	/**
	 * Ensure can restructure WAR.
	 */
	public void testWebArchive() throws Exception {

		// Obtain the web archive
		File webArchive = this.findFile(this.getClass(), "WebArchive.war");

		// Map war name
		this.archiveNameMappings.put("war", "WebArchive_war");

		// Test decoration
		this.doTest(webArchive.getAbsolutePath(), "ExpectedWarDecoration",
				null, null);
	}

	/**
	 * Ensure not decorates non-war archive.
	 */
	public void testNonWarArchive() throws Exception {

		// Obtain the jar archive
		File jarArchive = this.findFile(this.getClass(),
				"ExtractedDirectory/WEB-INF/lib/test.jar");

		// Test no decoration
		this.doTest(jarArchive.getAbsolutePath(), null, null, null);
	}

	/**
	 * Ensure no decoration if raw class path entry not exists.
	 */
	public void testNotExists() throws Exception {

		// Obtain path not existing
		String notExistRawClassPathEntry = "not-exists";
		assertFalse("Ensure test valid by raw class path entry not existing",
				new File(notExistRawClassPathEntry).exists());

		// Test no decoration
		this.doTest(notExistRawClassPathEntry, null, null, null);
	}

	/**
	 * Ensure can restructure extracted directory.
	 */
	public void testExtractedDirectory() throws Exception {

		// Obtain the extracted directory
		File extractedDirectory = this
				.findFile(this.getClass(), "ExtractedDirectory/WEB-INF/web.xml")
				.getParentFile().getParentFile();

		// Map war name
		this.archiveNameMappings.put("war", "ExtractedDirectory_war");

		// Test decoration
		this.doTest(extractedDirectory.getAbsolutePath(),
				"ExpectedWarDecoration", null, null);
	}

	/**
	 * Ensure can configure by system properties.
	 */
	public void testConfigurationBySystemProperties() throws Exception {

		// Obtain the extracted directory
		File extractedDirectory = this
				.findFile(this.getClass(), "ExtractedDirectory/WEB-INF/web.xml")
				.getParentFile().getParentFile();

		// Obtain the password file
		File passwordFile = this.findFile(this.getClass(), "password.txt");

		// Map war name
		this.archiveNameMappings.put("war", "ExtractedDirectory_war");

		// Test decoration
		this.doTest(extractedDirectory.getAbsolutePath(),
				"ExpectedWarDecoration", "80", passwordFile.getAbsolutePath());
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param rawClassPathEntry
	 *            Raw class path entry for decoration.
	 * @param expectedDirectoryName
	 *            Expected directory name. <code>null</code> indicates no
	 *            decoration.
	 * @param expectedHttpPort
	 *            Expected HTTP port.
	 * @param expectedPasswordFileLocation
	 *            Expected password file location. <code>null</code> indicates
	 *            temporary password file created.
	 */
	private void doTest(String rawClassPathEntry, String expectedDirectoryName,
			String expectedHttpPort, String expectedPasswordFileLocation)
			throws Exception {

		// Specify the system properties for decoration
		if (expectedHttpPort == null) {
			// Ensure HTTP port not specified
			System.clearProperty(WarOfficeFloorDecorator.SYSTEM_PROPERTY_HTTP_PORT);

			// Default HTTP port
			expectedHttpPort = "8080";

		} else {
			// Ensure HTTP port specified
			System.setProperty(
					WarOfficeFloorDecorator.SYSTEM_PROPERTY_HTTP_PORT,
					expectedHttpPort);
		}
		if (expectedPasswordFileLocation == null) {
			// Ensure password file not specified
			System.clearProperty(WarOfficeFloorDecorator.SYSTEM_PROPERTY_PASSWORD_FILE_LOCATION);
		} else {
			// Ensure password file specified
			System.setProperty(
					WarOfficeFloorDecorator.SYSTEM_PROPERTY_PASSWORD_FILE_LOCATION,
					expectedPasswordFileLocation);
		}

		// Run decoration
		new WarOfficeFloorDecorator()
				.decorate(new MockOfficeFloorDecoratorContext(rawClassPathEntry));

		// Determine if expecting decoration
		if (expectedDirectoryName == null) {
			// No decoration expected
			assertEquals("Should not be decorating", 0,
					this.resolvedArchives.size());
			assertEquals("Should not decorate environment", 0,
					this.environment.size());
			assertEquals("Should not decorate command options", 0,
					this.commandOptions.size());

		} else {
			// Validate archive decoration
			this.assertResolvedArchives(expectedDirectoryName);

			// Should provide appropriate properties
			assertEquals("Incorrect number command option types", 2,
					this.commandOptions.size());

			// Validate the officefloor command options
			List<String> officefloor = this.commandOptions.get("officefloor");
			assertNotNull("Should have officefloor command option", officefloor);
			assertEquals("Expecting only one officefloor command option", 1,
					officefloor.size());
			assertEquals("Incorrect officefloor",
					"net/officefloor/plugin/war/WarOfficeFloor.officefloor",
					officefloor.get(0));

			// Validate the property command options
			List<String> properties = this.commandOptions.get("property");
			assertNotNull("Should have property command options", properties);
			assertEquals("Incorrect number of property command options", 2,
					properties.size());
			assertEquals("Incorrect http.port",
					"http.port=" + expectedHttpPort, properties.get(0));

			// Determine if temporary directory
			String passwordProperty = properties.get(1);
			if (expectedPasswordFileLocation == null) {
				// Validate temporary password file created
				final String tmpDir = System.getProperty("java.io.tmpdir");
				String passwordFileLocation = passwordProperty
						.substring("password.file.location=".length());
				assertTrue("Should be temporary password file",
						passwordFileLocation.startsWith(tmpDir));
				assertEquals("Incorrect temporary password file content",
						"algorithm=-",
						this.getFileContents(new File(passwordFileLocation)));

			} else {
				// Validate specified location
				assertEquals("Incorrect password file location",
						"password.file.location="
								+ expectedPasswordFileLocation,
						passwordProperty);
			}
		}
	}

	/**
	 * Asserts the resolved archives were included.
	 * 
	 * @param expectedDirectory
	 *            Directory name containing expected content. Children of
	 *            directory are directories with their names corresponding to
	 *            the expected archives (with transformed name - remove digits
	 *            and replace . with _).
	 */
	private void assertResolvedArchives(String expectedDirectoryName)
			throws IOException {

		// Obtain the expected directory
		File expectedDirectory = this.findFile(this.getClass(),
				expectedDirectoryName);

		// Ensure archives are correct
		int expectedArchiveCount = 0;
		for (File expectedArchiveDirectory : expectedDirectory.listFiles()) {

			// Ignore SVN files
			if (".svn".equalsIgnoreCase(expectedArchiveDirectory.getName())) {
				continue; // ignore SVN files
			}

			// Increment the number of expected archives
			expectedArchiveCount++;

			// Obtain the resolved archive name
			String resolvedArchiveName = expectedArchiveDirectory.getName();
			if (this.archiveNameMappings.containsKey(resolvedArchiveName)) {
				resolvedArchiveName = this.archiveNameMappings
						.get(resolvedArchiveName);
			}

			// Obtain the archive
			File archiveFile = this.resolvedArchives.get(resolvedArchiveName);
			assertNotNull("Missing resolved archive " + resolvedArchiveName,
					archiveFile);

			// Ensure archive is correct
			assertJar(expectedArchiveDirectory, archiveFile);
		}

		// Ensure correct number of resolved archives
		assertEquals("Incorrect number of resolved archives",
				expectedArchiveCount, this.resolvedArchives.size());
	}

	/**
	 * Mock {@link OfficeFloorDecoratorContext} for testing.
	 */
	private class MockOfficeFloorDecoratorContext implements
			OfficeFloorDecoratorContext {

		/**
		 * Raw class path entry.
		 */
		private final String rawClassPathEntry;

		/**
		 * Initiate.
		 * 
		 * @param rawClassPathEntry
		 *            Raw class path entry.
		 */
		public MockOfficeFloorDecoratorContext(String rawClassPathEntry) {
			this.rawClassPathEntry = rawClassPathEntry;
		}

		/*
		 * ================== OfficeFloorDecoratorContext ==================
		 */

		@Override
		public String getRawClassPathEntry() {
			return this.rawClassPathEntry;
		}

		@Override
		public File createWorkspaceFile(String identifier, String extension) {
			try {
				return File.createTempFile(identifier, "." + extension);
			} catch (IOException ex) {
				// Not testing failure of creating file in this test
				throw fail(ex);
			}
		}

		@Override
		public void includeResolvedClassPathEntry(String classpathEntry) {

			// Ignore SVN entries
			if (classpathEntry.toLowerCase().endsWith(".svn")) {
				return; // ignore SVN entries
			}

			// Ensure the class path jar entry exists
			File resolvedJarFile = new File(classpathEntry);
			assertTrue("Ensure JAR file exists: " + classpathEntry,
					resolvedJarFile.isFile());

			// Obtain the archive name for comparison
			String archiveName = resolvedJarFile.getName();
			archiveName = archiveName.replaceAll("\\d", "");
			archiveName = archiveName.replace('.', '_');

			// Register the resolved archive
			WarOfficeFloorDecoratorTest.this.resolvedArchives.put(archiveName,
					resolvedJarFile);
		}

		@Override
		public void setEnvironmentProperty(String name, String value) {
			// Register the properties
			WarOfficeFloorDecoratorTest.this.environment.put(name, value);
		}

		@Override
		public void addCommandOption(String parameterName, String value) {
			List<String> options = WarOfficeFloorDecoratorTest.this.commandOptions
					.get(parameterName);
			if (options == null) {
				options = new LinkedList<String>();
				WarOfficeFloorDecoratorTest.this.commandOptions.put(
						parameterName, options);
			}
			options.add(value);
		}
	}

	/**
	 * Asserts that the JAR content is as per the expected directory.
	 * 
	 * @param expectedDirectory
	 *            Root of expected content for JAR file.
	 * @param jarFile
	 *            JAR file to validate.
	 */
	private static void assertJar(File expectedDirectory, File jarFile)
			throws IOException {

		// Obtain the expected entries
		Map<String, byte[]> expected = new HashMap<String, byte[]>();
		loadDirectoryEntries(null, expectedDirectory, expected);

		// Obtain the actual entries
		Map<String, byte[]> actual = new HashMap<String, byte[]>();
		loadJarEntries(jarFile, actual);

		// Ensure all actual entries were expected
		for (String name : actual.keySet()) {
			byte[] actualData = actual.get(name);
			byte[] expectedData = expected.get(name);

			// Ensure entry exists and matches
			assertNotNull("No expected entry " + name, expectedData);

			// Ensure data is correct for entry
			assertEquals("Incorrect content for entry " + name,
					createByteText(expectedData), createByteText(actualData));

			// Remove expected entry so later not considered missing
			expected.remove(name);
		}

		// Ensure no missing entries
		for (String expectedName : expected.keySet()) {
			fail("Missing entry " + expectedName);
		}
	}

	/**
	 * Obtains the byte as text.
	 * 
	 * @param data
	 *            Data to be returned as text.
	 * @return Text of the byte data.
	 */
	private static String createByteText(byte[] data) {

		// Load text for data
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < data.length; i++) {

			// Record the value (with suffix padding)
			String value = String.valueOf(data[i]);
			text.append(value);
			for (int j = value.length(); j < 3; j++) {
				text.append(" ");
			}
			text.append(" ");

			// Provide on new line after every 8 values
			if ((i % 8) == 0) {
				text.append("\n");
			}
		}
		return text.toString();
	}

	/**
	 * Loads the directory entries.
	 * 
	 * @param parentPath
	 *            Parent path. <code>null</code> for root entry.
	 * @param directory
	 *            Root directory to load the entries.
	 * @param entries
	 *            {@link Map} to receive the loaded entries.
	 */
	private static void loadDirectoryEntries(String parentPath, File directory,
			Map<String, byte[]> entries) throws IOException {

		// Load the children of the directory
		for (File child : directory.listFiles()) {

			// Obtain the child name
			String childName = child.getName();

			// Ignore SVN files
			if (".svn".equalsIgnoreCase(childName)) {
				continue; // ignore SVN files
			}

			// Create the child path
			String childPath = (parentPath == null ? "" : parentPath + "/")
					+ childName;

			// Load based on type (directory/file)
			if (child.isDirectory()) {
				// Add entry for the directory (including trailing '/')
				entries.put(childPath + "/", new byte[0]);

				// Add the directory's children
				loadDirectoryEntries(childPath, child, entries);

			} else {
				// Obtain data for the file
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				InputStream input = new FileInputStream(child);
				for (int value = input.read(); value != -1; value = input
						.read()) {
					buffer.write(value);
				}
				input.close();
				byte[] data = buffer.toByteArray();

				// Add entry for file
				entries.put(childPath, data);
			}
		}
	}

	/**
	 * Loads the JAR entries.
	 * 
	 * @param jarFile
	 *            JAR file.
	 * @param entries
	 *            {@link Map} to receive the loaded entries.
	 */
	private static void loadJarEntries(File jarFile, Map<String, byte[]> entries)
			throws IOException {

		// Iterate over contents of JAR loading entries
		JarInputStream input = new JarInputStream(new FileInputStream(jarFile));
		for (JarEntry entry = input.getNextJarEntry(); entry != null; entry = input
				.getNextJarEntry()) {

			// Obtain the entry name
			String entryName = entry.getName();

			// Ignore SVN entries
			if (entryName.toLowerCase().contains(".svn")) {
				continue; // ignore SVN files
			}

			// Obtain the data for the entry
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			for (int value = input.read(); value != -1; value = input.read()) {
				buffer.write(value);
			}
			byte[] data = buffer.toByteArray();

			// Load the entry
			entries.put(entryName, data);
		}
		input.close();
	}

}