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
package net.officefloor.plugin.web.http.resource.direct;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.resource.HttpDirectory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpFileDescriber;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
import net.officefloor.plugin.web.http.resource.NotExistHttpResource;

/**
 * Tests the {@link DirectHttpResourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class DirectHttpResourceFactoryTest extends OfficeFrameTestCase {

	/**
	 * Delegate {@link HttpResourceFactory}.
	 */
	private final HttpResourceFactory delegate = this
			.createMock(HttpResourceFactory.class);

	/**
	 * Default directory file names.
	 */
	private final String[] defaultDirectoryFileNames = new String[] {
			"index.html", "default.html" };

	/**
	 * {@link DirectHttpResourceFactory} to test.
	 */
	private final HttpResourceFactory factory = new DirectHttpResourceFactory(
			this.delegate, this.defaultDirectoryFileNames);

	/**
	 * Mock {@link HttpFile}.
	 */
	private final HttpFile file = this.createMock(HttpFile.class);

	/**
	 * Mock {@link HttpFile} text content.
	 */
	private static final String MOCK_FILE_CONTENTS = "TEST";

	/**
	 * Mock {@link ByteBuffer} contents of {@link HttpFile}.
	 */
	private final ByteBuffer fileContents = ByteBuffer.wrap(MOCK_FILE_CONTENTS
			.getBytes());

	/**
	 * Mock {@link HttpDirectory}.
	 */
	private final HttpDirectory directory = this
			.createMock(HttpDirectory.class);

	/**
	 * Ensure adds {@link FileDescriptor} to the delegate.
	 */
	public void testAddFileDescriber() {
		final HttpFileDescriber describer = this
				.createMock(HttpFileDescriber.class);
		this.delegate.addHttpFileDescriber(describer);
		this.replayMockObjects();
		this.factory.addHttpFileDescriber(describer);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain direct {@link HttpFile}.
	 */
	public void testHttpFile() throws Exception {

		final Charset charset = Charset.defaultCharset();

		// Record only one creation of the file
		final String REQUEST_URI_PATH = "/index.html";
		this.recordReturn(this.delegate,
				this.delegate.createHttpResource(REQUEST_URI_PATH), this.file);
		this.recordReturn(this.file, this.file.getContents(), this.fileContents);

		// Record details of HTTP File
		this.recordReturn(this.file, this.file.isExist(), true);
		this.recordReturn(this.file, this.file.getContentEncoding(), "zip");
		this.recordReturn(this.file, this.file.getContentType(), "content/type");
		this.recordReturn(this.file, this.file.getCharset(), charset);

		// Test
		this.replayMockObjects();

		// Lazy create the file
		HttpResource resource = this.factory
				.createHttpResource(REQUEST_URI_PATH);
		HttpFile file = this.assertHttpFile(resource);

		// Validate the file
		assertEquals("Incorrect path", "/index.html", file.getPath());
		assertTrue("File should exist", file.isExist());
		assertEquals("Incorrect Content-Encoding", "zip",
				file.getContentEncoding());
		assertEquals("Incorrect Content-Type", "content/type",
				file.getContentType());
		assertEquals("Incorrect charset", charset, file.getCharset());

		// Ensure obtain same file again (as stored in memory)
		HttpResource sameResource = this.factory
				.createHttpResource(REQUEST_URI_PATH);
		this.assertHttpFile(sameResource);

		// Ensure same file
		assertSame("Should be same file", resource, sameResource);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain direct {@link HttpDirectory} with its default
	 * {@link HttpFile}.
	 */
	public void testHttpDirectory() throws Exception {

		// Record only one creation of the directory and file
		final String REQUEST_URI_PATH = "/directory";
		this.recordReturn(this.delegate,
				this.delegate.createHttpResource(REQUEST_URI_PATH),
				this.directory);
		this.recordReturn(this.delegate,
				this.delegate.createHttpResource("/directory/index.html"),
				new NotExistHttpResource("/directory/index.html"));
		this.recordReturn(this.delegate,
				this.delegate.createHttpResource("/directory/default.html"),
				this.file);
		this.recordReturn(this.file, this.file.getContents(), this.fileContents);

		// Record details of HTTP directory
		this.recordReturn(this.directory, this.directory.isExist(), true);

		// Pass through delegate list as not used in web application
		final HttpResource[] resources = new HttpResource[0];
		this.recordReturn(this.directory, this.directory.listResources(),
				resources);

		// Test
		this.replayMockObjects();

		// Lazy create the directory
		HttpResource resource = this.factory
				.createHttpResource(REQUEST_URI_PATH);
		HttpDirectory directory = this.assertHttpDirectory(resource, true);

		// Validate the directory
		assertEquals("Incorrect path", "/directory/", directory.getPath());
		assertTrue("Should always exist", directory.isExist());
		assertSame("Listed resources should be from delegate directory",
				resources, directory.listResources());

		// Ensure correct path for default file
		assertEquals("Incorrect default file path", "/directory/default.html",
				directory.getDefaultFile().getPath());

		// Ensure obtain same directory again (as stored in memory)
		HttpResource sameResource = this.factory
				.createHttpResource(REQUEST_URI_PATH);
		this.assertHttpDirectory(sameResource, true);
		assertSame("Should be same directory", resource, sameResource);

		// Ensure file is registered in memory for lookup
		HttpResource file = this.factory.createHttpResource(REQUEST_URI_PATH
				+ "/default.html");
		this.assertHttpFile(file);
		assertSame("Should be same file", file, directory.getDefaultFile());

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain direct {@link HttpDirectory} that has no default
	 * {@link HttpFile}.
	 */
	public void testHttpDirectoryNoDefaultFile() throws Exception {

		// Record only one creation of the directory and no file
		final String REQUEST_URI_PATH = "/directory";
		this.recordReturn(this.delegate,
				this.delegate.createHttpResource(REQUEST_URI_PATH),
				this.directory);
		this.recordReturn(this.delegate,
				this.delegate.createHttpResource("/directory/index.html"),
				new NotExistHttpResource("/directory/index.html"));
		this.recordReturn(this.delegate,
				this.delegate.createHttpResource("/directory/default.html"),
				new NotExistHttpResource("/directory/default.html"));

		// Test
		this.replayMockObjects();

		// Lazy create the directory
		HttpResource resource = this.factory
				.createHttpResource(REQUEST_URI_PATH);
		this.assertHttpDirectory(resource, false);

		// Ensure obtain same directory again (as stored in memory)
		HttpResource sameResource = this.factory
				.createHttpResource(REQUEST_URI_PATH);
		this.assertHttpDirectory(sameResource, false);

		// Ensure same file
		assertSame("Should be same directory", resource, sameResource);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure that if {@link HttpDirectory} default {@link HttpFile} has already
	 * been looked up and registered that the instance is re-used rather than
	 * another {@link HttpFile} being created and registered.
	 */
	public void testHttpDirectoryDefaultFileAlreadyRegistered()
			throws Exception {

		// Record only one creation of the directory and file
		this.recordReturn(this.delegate,
				this.delegate.createHttpResource("/directory/index.html"),
				this.file);
		this.recordReturn(this.file, this.file.getContents(), this.fileContents);
		this.recordReturn(this.delegate,
				this.delegate.createHttpResource("/directory"), this.directory);

		// Test
		this.replayMockObjects();

		// Lazy create the file
		HttpResource file = this.factory
				.createHttpResource("/directory/index.html");
		this.assertHttpFile(file);

		// Lazy create the directory
		HttpResource resource = this.factory.createHttpResource("/directory");
		HttpDirectory directory = this.assertHttpDirectory(resource, true);

		// Ensure same file
		assertSame("Should be same default file", file,
				directory.getDefaultFile());

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Asserts the {@link HttpFile} contains appropriate content.
	 * 
	 * @param resource
	 *            {@link HttpFile}.
	 * @return {@link HttpFile}.
	 */
	private HttpFile assertHttpFile(HttpResource resource) {

		// Ensure a file
		assertNotNull("Must have resource file", resource);
		assertTrue("Resource should be file", resource instanceof HttpFile);
		HttpFile file = (HttpFile) resource;

		// Ensure read-only direct byte buffer
		ByteBuffer contents = file.getContents();
		assertTrue("File contents should be direct", contents.isDirect());
		assertTrue("File contents should be read-only", contents.isReadOnly());

		// Ensure contents are correct
		byte[] buffer = new byte[contents.remaining()];
		contents.duplicate().get(buffer);
		String textContents = new String(buffer);
		assertEquals("Incorrect content for file", MOCK_FILE_CONTENTS,
				textContents);

		// Return the file
		return file;
	}

	/**
	 * Asserts the {@link HttpDirectory} contains appropriate default
	 * {@link HttpFile}.
	 * 
	 * @param resource
	 *            {@link HttpDirectory}.
	 * @param isDefaultFile
	 *            Flag indicating if a default {@link HttpFile} is expected.
	 * @return {@link HttpDirectory}.
	 */
	private HttpDirectory assertHttpDirectory(HttpResource resource,
			boolean isDefaultFile) {

		// Ensure a directory
		assertNotNull("Must have resource directory", resource);
		assertTrue("Resource should be a directory",
				resource instanceof HttpDirectory);
		HttpDirectory directory = (HttpDirectory) resource;

		// Ensure correct default file
		if (isDefaultFile) {
			// Validate the expected default file
			this.assertHttpFile(directory.getDefaultFile());

		} else {
			// Ensure no default file
			assertNull("Should not have default file",
					directory.getDefaultFile());
		}

		// Return the directory
		return directory;
	}

}