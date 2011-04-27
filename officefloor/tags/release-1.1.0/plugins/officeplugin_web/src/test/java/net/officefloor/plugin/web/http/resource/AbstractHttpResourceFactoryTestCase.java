/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Deque;
import java.util.LinkedList;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.resource.HttpDirectory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpFileDescriber;
import net.officefloor.plugin.web.http.resource.HttpFileDescription;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
import net.officefloor.plugin.web.http.resource.InvalidHttpRequestUriException;

/**
 * Abstract testing of the {@link HttpResourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpResourceFactoryTestCase extends
		OfficeFrameTestCase {

	/**
	 * {@link Charset} for description.
	 */
	private static final Charset CHARSET = Charset.defaultCharset();

	/**
	 * <code>Content-Encoding</code> for description.
	 */
	private static final String CONTENT_ENCODING = "test-encoding";

	/**
	 * <code>Content-Type</code> for description.
	 */
	private final static String CONTENT_TYPE = "test-type";

	/**
	 * Creates the {@link HttpResourceFactory} to test.
	 * 
	 * @param namespace
	 *            Name space for {@link HttpResourceFactory}. Typically this
	 *            will be the class path prefix.
	 * @return {@link HttpResourceFactory} to test.
	 */
	protected abstract HttpResourceFactory createHttpResourceFactory(
			String namespace);

	/**
	 * {@link HttpResourceFactory} being tested.
	 */
	private HttpResourceFactory factory;

	/**
	 * Expected {@link RecordedFileDescription} instances.
	 */
	private final Deque<RecordedFileDescription> expectedDescriptions = new LinkedList<RecordedFileDescription>();

	@Override
	protected void setUp() throws Exception {

		// Create the factory to create the resources
		this.factory = this.createHttpResourceFactory(this.getClass()
				.getPackage().getName());

		// Add HTTP file describer for testing
		this.factory.addHttpFileDescriber(new HttpFileDescriber() {
			@Override
			public void describe(HttpFileDescription description) {

				// Obtain the next expected description
				assertTrue(
						"Unexpexted file description",
						AbstractHttpResourceFactoryTestCase.this.expectedDescriptions
								.size() > 0);
				RecordedFileDescription expectedDescription = AbstractHttpResourceFactoryTestCase.this.expectedDescriptions
						.removeFirst();

				// Ensure resource details are as expected
				HttpResource resource = description.getResource();
				assertNotNull("Must provide resource for description", resource);
				assertEquals("Incorrect path for resource description",
						expectedDescription.expectedResourcePath,
						resource.getPath());
				assertTrue("File always exists", resource.isExist());

				// Ensure content is as expected
				AbstractHttpResourceFactoryTestCase.this.assertBufferContents(
						expectedDescription.fileNameForExpectedContent,
						description.getContents());

				// Provide the description
				description.setContentType(CONTENT_TYPE, CHARSET);
				description.setContentEncoding(CONTENT_ENCODING);
			}
		});
	}

	/**
	 * Ensure can locate a {@link HttpFile} by exact path.
	 */
	public void testExactPath() {
		this.doFileTest("/index.html", "/index.html", "index.html");
	}

	/**
	 * Ensure obtain {@link HttpResource} for not existing path.
	 */
	public void testNotExistingResource() {
		HttpResource resource = this.createHttpResource("/not_exist",
				"/not_exist", false, null);
		assertFalse("Non-existing resource should not be HttpFile",
				resource instanceof HttpFile);
		assertFalse("Non-existing resource should not be HttpDirectory",
				resource instanceof HttpDirectory);
	}

	/**
	 * Ensure can locate via non-canonical path for {@link HttpFile}.
	 */
	public void testNotCanonicalFilePath() {
		this.doFileTest("/path/.././index.html", "/index.html", "index.html");
	}

	/**
	 * Ensure can locate via non-canonical path for {@link HttpDirectory}.
	 */
	public void testNotCanonicalDirectoryPath() {
		this.recordFileDescription("/directory/sub_directory/index.html",
				"directory/sub_directory/index.html");
		HttpResource resource = this.createHttpResource(
				"/path/../directory/./sub_directory/../sub_directory",
				"/directory/sub_directory/", true, null);
		this.assertHttpDirectory(resource, "/directory/sub_directory/",
				"/directory/sub_directory/index.html");
	}

	/**
	 * Ensure not existing {@link HttpResource} still provide canonical path.
	 * This allows for better caching of non-existing resources.
	 */
	public void testNotExistNotCanonicalPath() {
		HttpResource resource = this.createHttpResource(
				"/path/.././not_exist.html", "/not_exist.html", false, null);
		assertFalse("Non-existing resource should not be HttpFile",
				resource instanceof HttpFile);
		assertFalse("Non-existing resource should not be HttpDirectory",
				resource instanceof HttpDirectory);
	}

	/**
	 * Ensure exception thrown if request URI path is invalid.
	 */
	public void testFailOnInvalidPath() throws IOException {

		// Obtain the HTTP resource factory
		HttpResourceFactory factory = this.createHttpResourceFactory("");

		// Ensure issue if try to go above root
		try {
			factory.createHttpResource("/..");
			fail("Should not succeed");
		} catch (InvalidHttpRequestUriException ex) {
			assertEquals("Incorrect http status", HttpStatus.SC_BAD_REQUEST,
					ex.getHttpStatus());
		}
	}

	/**
	 * Ensure ignores repeated separators in path for {@link HttpFile}.
	 */
	public void testIgnoreRepeatedSeparatorsForFile() {
		this.doFileTest("//directory///index.html", "/directory/index.html",
				"directory/index.html");
	}

	/**
	 * Ensure ignores repeated separators in path for {@link HttpDirectory}.
	 */
	public void testIgnoreRepeatedSeparatorsForDirectory() {
		this.recordFileDescription("/directory/sub_directory/index.html",
				"directory/sub_directory/index.html");
		HttpResource resource = this.createHttpResource(
				"///directory///sub_directory///", "/directory/sub_directory/",
				true, null);
		this.assertHttpDirectory(resource, "/directory/sub_directory/",
				"/directory/sub_directory/index.html");
	}

	/**
	 * Ensure can locate default file for directory.
	 */
	public void testDefaultFile() {
		// Record for listing (in validating directory)
		this.recordFileDescription("/directory/index.html",
				"directory/index.html");

		// Record for default file
		this.recordFileDescription("/directory/index.html",
				"directory/index.html");

		// Test
		HttpResource resource = this.createHttpResource("/directory",
				"/directory/", true, null);
		HttpDirectory directory = this.assertHttpDirectory(resource,
				"/directory/", "/directory/index.html",
				"/directory/sub_directory/");
		HttpFile defaultFile = directory.getDefaultFile();
		this.assertHttpFile(defaultFile, "/directory/index.html",
				"directory/index.html");
	}

	/**
	 * Ensure no file if no default file for directory.
	 */
	public void testNoDefaultFile() {
		this.recordFileDescription("/empty/empty.txt", "empty/empty.txt");
		HttpResource resource = this.createHttpResource("/empty", "/empty/",
				true, null);
		HttpDirectory directory = this.assertHttpDirectory(resource, "/empty/",
				"/empty/empty.txt");
		HttpFile defaultFile = directory.getDefaultFile();
		assertNull("Should not be default file if one not in directory",
				defaultFile);
	}

	/**
	 * Ensure can locate file within sub directory.
	 */
	public void testSubDirectoryExactPath() {
		this.doFileTest("/directory/index.html", "/directory/index.html",
				"directory/index.html");
	}

	/**
	 * Ensure can locate default file within sub directory.
	 */
	public void testSubDirectoryDefaultFile() {
		// Record listing (validating directory)
		this.recordFileDescription("/directory/sub_directory/index.html",
				"directory/sub_directory/index.html");

		// Record default file
		this.recordFileDescription("/directory/sub_directory/index.html",
				"directory/sub_directory/index.html");

		// Test
		HttpResource resource = this.createHttpResource(
				"/directory/sub_directory", "/directory/sub_directory/", true,
				null);
		HttpDirectory directory = this.assertHttpDirectory(resource,
				"/directory/sub_directory/",
				"/directory/sub_directory/index.html");
		HttpFile defaultFile = directory.getDefaultFile();
		this.assertHttpFile(defaultFile, "/directory/sub_directory/index.html",
				"directory/sub_directory/index.html");
	}

	/**
	 * Ensure able to list directory contents.
	 */
	public void testDirectoryContents() {
		// Record listing (validate directory)
		this.recordFileDescription("/directory/index.html",
				"directory/index.html");

		// Record create the listing
		this.recordFileDescription("/directory/index.html",
				"directory/index.html");

		// Record listing (validate sub_directory)
		this.recordFileDescription("/directory/sub_directory/index.html",
				"directory/sub_directory/index.html");

		// Test
		HttpResource resource = this.createHttpResource("/directory",
				"/directory/", true, null);
		HttpDirectory directory = this.assertHttpDirectory(resource,
				"/directory/", "/directory/index.html",
				"/directory/sub_directory/");
		HttpResource[] children = directory.listResources();
		this.assertHttpFile(children[0], "/directory/index.html",
				"directory/index.html");
		this.assertHttpDirectory(children[1], "/directory/sub_directory/",
				"/directory/sub_directory/index.html");
	}

	/**
	 * Tests retrieving directory from a JAR (as most tests are using
	 * directories).
	 * 
	 * Note test expects JUnit 3.8.2 on class path.
	 */
	public void testObtainDirectoryFromJar() throws Exception {
		HttpResourceFactory factory = this.createHttpResourceFactory("");
		HttpResource directory = factory.createHttpResource("/junit");
		this.assertHttpDirectory(directory, "/junit/", "/junit/awtui/",
				"/junit/extensions/", "/junit/framework/", "/junit/runner/",
				"/junit/swingui/", "/junit/textui/");
	}

	/**
	 * Tests retrieving file from a JAR (as most tests are using directories).
	 * 
	 * Note test expects JUnit 3.8.2 on class path.
	 */
	public void testObtainFileFromJar() throws Exception {
		HttpResourceFactory factory = this.createHttpResourceFactory("");
		HttpResource file = factory
				.createHttpResource("/junit/swingui/icons/ok.gif");
		assertTrue("Expecting file", file instanceof HttpFile);
		assertEquals("Incorrect file path", "/junit/swingui/icons/ok.gif",
				file.getPath());
		assertTrue("File should exist", file.isExist());
	}

	/**
	 * Convenience method for running a {@link HttpFile} test.
	 * 
	 * @param requestUriPath
	 *            Request URI path for the {@link HttpResource}.
	 * @param expectedPath
	 *            Expected path.
	 * @param fileNameForExpectedContent
	 *            File containing the expected content. <code>null</code>
	 *            indicates no content (and resource not existing).
	 */
	private void doFileTest(String requestUriPath, String expectedPath,
			String fileNameForExpectedContent) {
		this.recordFileDescription(expectedPath, fileNameForExpectedContent);
		HttpResource resource = this.createHttpResource(requestUriPath,
				expectedPath, (fileNameForExpectedContent != null),
				fileNameForExpectedContent);
		this.assertHttpFile(resource, expectedPath, fileNameForExpectedContent);
	}

	/**
	 * Creates the {@link HttpResource}.
	 * 
	 * @param requestUriPath
	 *            Request URI path for the {@link HttpResource}.
	 * @param expectedPath
	 *            Expected path.
	 * @param isExpectedToExist
	 *            Indicates if expect {@link HttpResource} to exist.
	 * @param fileNameForExpectedContent
	 *            File containing expected content for
	 *            {@link HttpFileDescription}. <code>null</code> for no expected
	 *            content or {@link HttpDirectory}.
	 * @return {@link HttpResource}.
	 */
	private HttpResource createHttpResource(String requestUriPath,
			final String expectedPath, final boolean isExpectedToExist,
			final String fileNameForExpectedContent) {
		try {

			// Create the HTTP resource
			HttpResource resource = this.factory
					.createHttpResource(requestUriPath);

			// Always expect to return instance and have path
			assertNotNull("Always expected to return instance", resource);
			assertEquals("Incorrect path", expectedPath, resource.getPath());
			assertEquals("Incorrect existance", isExpectedToExist,
					resource.isExist());

			// Return the resource
			return resource;

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Validates the {@link HttpFile}.
	 * 
	 * @param resource
	 *            {@link HttpFile} to validate.
	 * @param expectedPath
	 *            Expected path.
	 * @param fileNameForExpectedContent
	 *            Name of file containing the expected content.
	 * @return {@link HttpFile}.
	 */
	private HttpFile assertHttpFile(HttpResource resource, String expectedPath,
			String fileNameForExpectedContent) {

		// Ensure is a file
		assertTrue("Resource is not a HttpFile", resource instanceof HttpFile);
		HttpFile httpFile = (HttpFile) resource;

		// Ensure correct path and file always exists
		assertEquals("Incorrect path for file", expectedPath,
				httpFile.getPath());
		assertTrue("File should always exist", httpFile.isExist());

		// Ensure correct description of file
		assertEquals("Incorrect Content-Encoding", CONTENT_ENCODING,
				httpFile.getContentEncoding());
		assertEquals("Incorrect Content-Type", CONTENT_TYPE,
				httpFile.getContentType());
		assertEquals("Incorrect charset", CHARSET, httpFile.getCharset());

		// Validate the contents of the file
		assertBufferContents(fileNameForExpectedContent, httpFile.getContents());

		// Return the file
		return httpFile;
	}

	/**
	 * Validates the {@link HttpDirectory}.
	 * 
	 * @param resource
	 *            {@link HttpDirectory} to validate.
	 * @param expectedPath
	 *            Expected path.
	 * @param childResourcePaths
	 *            Paths of the child {@link HttpResource} instances of the
	 *            {@link HttpDirectory}.
	 * @return {@link HttpDirectory}.
	 */
	private HttpDirectory assertHttpDirectory(HttpResource resource,
			String expectedPath, String... childResourcePaths) {

		// Ensure is a file
		assertTrue("Resource is not a HttpDirectory",
				resource instanceof HttpDirectory);
		HttpDirectory httpDirectory = (HttpDirectory) resource;

		// Ensure correct path
		assertEquals("Incorrect path for file", expectedPath,
				httpDirectory.getPath());

		// Ensure the correct children
		HttpResource[] children = httpDirectory.listResources();
		assertEquals("Incorrect number of children for directory",
				childResourcePaths.length, children.length);
		for (int i = 0; i < childResourcePaths.length; i++) {
			String childResourcePath = childResourcePaths[i];
			HttpResource child = children[i];
			assertEquals("Incorrect path for child " + i, childResourcePath,
					child.getPath());
			assertTrue("Child " + childResourcePath + " is expected to exist",
					child.isExist());
		}

		// Return the directory
		return httpDirectory;
	}

	/**
	 * Asserts the contents of the {@link ByteBuffer}.
	 * 
	 * @param fileNameForExpectedContent
	 *            File name for the expected content.
	 * @param buffer
	 *            {@link ByteBuffer} to validate.
	 */
	private void assertBufferContents(String fileNameForExpectedContent,
			ByteBuffer buffer) {
		try {

			// Read in the expected file content
			byte[] expectedContents;
			if (fileNameForExpectedContent == null) {
				// No expected content
				expectedContents = new byte[0];

			} else {
				// Obtain the expected content
				File expectedFile = this.findFile(this.getClass(),
						fileNameForExpectedContent);
				InputStream inputStream = new FileInputStream(expectedFile);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				for (int value = inputStream.read(); value != -1; value = inputStream
						.read()) {
					outputStream.write(value);
				}
				expectedContents = outputStream.toByteArray();
			}

			// Ensure buffer contains expected content
			assertEquals("Incorrect content length", expectedContents.length,
					buffer.remaining());
			for (int i = 0; i < expectedContents.length; i++) {
				byte expectedByte = expectedContents[i];
				byte actualByte = buffer.get(i); // starts with 0 position
				assertEquals("Incorrect content byte at index " + i,
						expectedByte, actualByte);
			}

		} catch (IOException ex) {
			throw fail(ex);
		}
	}

	/**
	 * Records {@link HttpFileDescription}.
	 * 
	 * @param expectedResourcePath
	 *            Expected {@link HttpFileDescription} {@link HttpResource}
	 *            path.
	 * @param fileNameForExpectedContent
	 *            File name containing the expected content of the
	 *            {@link HttpFile}.
	 */
	private void recordFileDescription(String expectedResourcePath,
			String fileNameForExpectedContent) {
		this.expectedDescriptions.add(new RecordedFileDescription(
				expectedResourcePath, fileNameForExpectedContent));
	}

	/**
	 * Recorded {@link HttpFileDescription}.
	 */
	private static class RecordedFileDescription {

		/**
		 * Expected {@link HttpFileDescription} {@link HttpResource} path.
		 */
		public final String expectedResourcePath;

		/**
		 * File name containing the expected content of the {@link HttpFile}.
		 */
		public final String fileNameForExpectedContent;

		/**
		 * Initiate.
		 * 
		 * @param expectedResourcePath
		 *            Expected {@link HttpFileDescription} {@link HttpResource}
		 *            path.
		 * @param fileNameForExpectedContent
		 *            File name containing the expected content of the
		 *            {@link HttpFile}.
		 */
		public RecordedFileDescription(String expectedResourcePath,
				String fileNameForExpectedContent) {
			this.expectedResourcePath = expectedResourcePath;
			this.fileNameForExpectedContent = fileNameForExpectedContent;
		}
	}

}