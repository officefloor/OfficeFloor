/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;

/**
 * Abstract testing of the {@link HttpFileFactory}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpFileFactoryTestCase extends
		OfficeFrameTestCase {

	/**
	 * Creates the {@link HttpFileFactory} to test.
	 *
	 * @return {@link HttpFileFactory} to test.
	 */
	protected abstract HttpFileFactory createHttpFileFactory();

	/**
	 * Ensure can locate a {@link HttpFile} by exact path.
	 */
	public void testExactPath() throws Exception {
		this.doTest("/index.html", "/index.html", "index.html", "html");
	}

	/**
	 * Ensure locates a not existing {@link HttpFile}.
	 */
	public void testNotExistingFile() throws Exception {
		this.doTest("/not_exist.html", "/not_exist.html", null, null);
	}

	/**
	 * Ensure can locate via non-canonical path.
	 */
	public void testNotCanonicalPath() throws Exception {
		this.doTest("/path/../index.html", "/index.html", "index.html", "html");
	}

	/**
	 * Ensure can locate default file for directory.
	 */
	public void testDefaultFile() throws Exception {
		this.doTest("/", "/index.html", "index.html", "html");
	}

	/**
	 * Ensure locates a not existing {@link HttpFile} for directory not
	 * containing default file.
	 */
	public void testNotExistingDefaultFile() throws Exception {
		this.doTest("/not_exist", "/not_exist/index.html", null, null);
	}

	/**
	 * Ensure can locate file within sub directory.
	 */
	public void testSubDirectoryExactPath() throws Exception {
		this.doTest("/directory/index.html", "/directory/index.html",
				"directory/index.html", "html");
	}

	/**
	 * Ensure can locate default file within sub directory.
	 */
	public void testSubDirectoryDefaultFile() throws Exception {
		this.doTest("/directory/", "/directory/index.html",
				"directory/index.html", "html");
	}

	/**
	 * Ensure exception thrown if request URI path is invalid.
	 */
	public void testFailOnInvalidPath() throws Exception {
		try {
			this.doTest("/..", null, null, null);
			fail("Should not succeed");
		} catch (InvalidHttpRequestUriException ex) {
			assertEquals("Incorrect http status", HttpStatus.SC_BAD_REQUEST, ex
					.getHttpStatus());
		}
	}

	/**
	 * Does the test.
	 *
	 * @param requestUriPath
	 *            Request URI path.
	 * @param expectedPath
	 *            Expected path on the {@link HttpFile}. <code>null</code>
	 *            indicates {@link HttpFile} should not be found.
	 * @param fileName
	 *            Relative file to test package to obtain expected contents of
	 *            {@link HttpFile}. <code>null</code> indicates the
	 *            {@link HttpFile} is not to exist.
	 * @param fileExtension
	 *            Expected extension for the {@link HttpFileDescription}.
	 */
	private void doTest(String requestUriPath, String expectedPath,
			String fileName, final String fileExtension) throws Exception {

		// Obtain the context directory (ensuring index file exists)
		File contextDirectory = this.findFile(this.getClass(), "index.html")
				.getParentFile();

		// Create the factory to create the files
		HttpFileFactory locator = this.createHttpFileFactory();

		// Add HTTP file describer for testing
		final String CONTENT_ENCODING = "test-encoding";
		final String CONTENT_TYPE = "test-type";
		final ByteBuffer[] descriptionContents = new ByteBuffer[1];
		final boolean[] isSpecificDescriberCalled = new boolean[1];
		isSpecificDescriberCalled[0] = false;
		locator.addHttpFileDescriber(new HttpFileDescriber() {
			@Override
			public void describe(HttpFileDescription description) {
				assertEquals("Incorrect extension", fileExtension, description
						.getExtension());
				assertTrue("Ensure specific describers called first",
						isSpecificDescriberCalled[0]);
				descriptionContents[0] = description.getContents();
				description.setContentType(CONTENT_TYPE);
			}
		});

		// Locate the file (with specific describer)
		HttpFile httpFile = locator.createHttpFile(contextDirectory,
				requestUriPath, new HttpFileDescriber() {
					@Override
					public void describe(HttpFileDescription description) {
						description.setContentEncoding(CONTENT_ENCODING);
						isSpecificDescriberCalled[0] = true;
					}
				});

		// Always expect to return instance and have path
		assertNotNull("Always expected to return instance", httpFile);
		assertEquals("Incorrect path", expectedPath, httpFile.getPath());

		// Validate file
		if (fileName == null) {
			// File not exist
			assertFalse("File should not exist", httpFile.isExist());
			assertEquals("Should be no Content-Encoding", "", httpFile
					.getContentEncoding());
			assertEquals("Should be no Content-Type", "", httpFile
					.getContentType());
			assertBufferContents(new byte[0], httpFile.getContents());
			assertNull("Should not attempt to describe", descriptionContents[0]);
		} else {
			// Expecting file so validate details of file
			assertTrue("File should exist", httpFile.isExist());
			assertEquals("Incorrect Content-Encoding", CONTENT_ENCODING,
					httpFile.getContentEncoding());
			assertEquals("Incorrect Content-Type", CONTENT_TYPE, httpFile
					.getContentType());

			// Read in the expected file content
			String expectedFilePath = this.getClass().getPackage().getName()
					.replace('.', '/')
					+ "/" + fileName;
			InputStream inputStream = Thread.currentThread()
					.getContextClassLoader().getResourceAsStream(
							expectedFilePath);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			for (int value = inputStream.read(); value != -1; value = inputStream
					.read()) {
				outputStream.write(value);
			}
			byte[] expectedContents = outputStream.toByteArray();

			// Validate the contents of the file
			assertBufferContents(expectedContents, httpFile.getContents());
			assertBufferContents(expectedContents, descriptionContents[0]);
		}
	}

	/**
	 * Asserts the contents of the {@link ByteBuffer}.
	 *
	 * @param expectedContents
	 *            Expected contents.
	 * @param buffer
	 *            {@link ByteBuffer} to validate.
	 */
	private static void assertBufferContents(byte[] expectedContents,
			ByteBuffer buffer) {
		assertEquals("Incorrect content length", expectedContents.length,
				buffer.remaining());
		for (int i = 0; i < expectedContents.length; i++) {
			byte expectedByte = expectedContents[i];
			byte actualByte = buffer.get(i); // starts with 0 position
			assertEquals("Incorrect content byte at index " + i, expectedByte,
					actualByte);
		}
	}

}