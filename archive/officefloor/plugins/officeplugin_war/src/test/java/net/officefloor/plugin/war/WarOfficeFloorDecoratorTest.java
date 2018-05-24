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
package net.officefloor.plugin.war;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import net.officefloor.building.decorate.OfficeFloorDecoratorContext;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.application.WebArchitect;

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
	 * <p>
	 * Ensure the same class path directory is used for public files as
	 * {@link WebArchitect}.
	 * <p>
	 * This test ensures they stay the same and allows this project to not have
	 * to reference the {@link WebArchitect} at run time (pulling in
	 * their dependencies for the {@link OfficeBuilding}).
	 */
	public void testMatchingPublicClassPathDirectory() {
		assertEquals("Incorrect public class path directory",
				WebArchitect.WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX
						+ "/", WarOfficeFloorDecorator.WEB_PUBLIC);
	}

	/**
	 * Ensure can restructure WAR.
	 */
	public void testWebArchive() throws Exception {

		// Obtain the web archive
		File webArchive = this.findFile(this.getClass(), "WebArchive.war");

		// Map war name
		this.archiveNameMappings.put("war", "WebArchive_jar");

		// Test decoration
		this.doTest(webArchive.getAbsolutePath(), "ExpectedWarDecoration");
	}

	/**
	 * Ensure not decorates non-war archive.
	 */
	public void testNonWarArchive() throws Exception {

		// Obtain the jar archive
		File jarArchive = this.findFile(this.getClass(),
				"ExtractedDirectory/WEB-INF/lib/test.jar");

		// Test no decoration
		this.doTest(jarArchive.getAbsolutePath(), null);
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
		this.doTest(notExistRawClassPathEntry, null);
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
		this.archiveNameMappings.put("war", "ExtractedDirectory_jar");

		// Test decoration
		this.doTest(extractedDirectory.getAbsolutePath(),
				"ExpectedWarDecoration");
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param rawClassPathEntry
	 *            Raw class path entry for decoration.
	 * @param expectedDirectoryName
	 *            Expected directory name. <code>null</code> indicates no
	 *            decoration.
	 */
	private void doTest(String rawClassPathEntry, String expectedDirectoryName)
			throws Exception {

		// Run decoration
		new WarOfficeFloorDecorator()
				.decorate(new MockOfficeFloorDecoratorContext(rawClassPathEntry));

		// Determine if expecting decoration
		if (expectedDirectoryName == null) {
			// No decoration expected
			assertEquals("Should not be decorating", 0,
					this.resolvedArchives.size());

		} else {
			// Validate archive decoration
			this.assertResolvedArchives(expectedDirectoryName);
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
	}

	/**
	 * Asserts that the JAR content is as per the expected directory.
	 * 
	 * @param expectedDirectory
	 *            Root of expected content for JAR file.
	 * @param jarFile
	 *            JAR file to validate.
	 */
	public static void assertJar(File expectedDirectory, File jarFile)
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