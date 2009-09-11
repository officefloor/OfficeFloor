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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ClasspathHttpFileLocator}.
 *
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileLocatorTest extends OfficeFrameTestCase {

	/**
	 * Ensure can locate a HTML file.
	 */
	public void testLoadHtmlFile() throws Exception {
		this.doTest("/index.html", "/index.html", "",
				"test/html; charset=UTF-8", "index.html");
	}

	/**
	 * Does the test.
	 *
	 * @param requestUriPath
	 *            Request URI path.
	 * @param expectedPath
	 *            Expected path on the {@link HttpFile}. <code>null</code>
	 *            indicates {@link HttpFile} should not be found.
	 * @param expectedContentEncoding
	 *            Expected Content-Encoding.
	 * @param expectedContentType
	 *            Expected Content-Type.
	 * @param fileName
	 *            Relative file to test package to obtain expected contents of
	 *            {@link HttpFile}.
	 * @throws IOException
	 */
	private void doTest(String requestUriPath, String expectedPath,
			String expectedContentEncoding, String expectedContentType,
			String fileName) throws IOException {

		// Create the locator to obtain files from test package
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		ClasspathHttpFileLocator locator = new ClasspathHttpFileLocator(
				classLoader, this.getClass().getPackage().getName().replace(
						'.', '/'), "index.html");

		// Locate the file
		HttpFile httpFile = locator.locateHttpFile(requestUriPath);

		// Validate file
		if (expectedPath == null) {
			// Not expecting to find file
			assertNull("Not expecting to find file", httpFile);
		} else {
			// Expecting file so validate details of file
			assertNotNull("Expected to find file", httpFile);
			assertEquals("Incorrect path", expectedPath, httpFile.getPath());
			assertEquals("Incorrect Content-Encoding", expectedContentEncoding,
					httpFile.getContentEncoding());
			assertEquals("Incorrect Content-Type", expectedContentType,
					httpFile.getContentType());

			// Read in the expected file content
			String expectedFilePath = this.getClass().getPackage().getName()
					.replace('.', '/')
					+ "/" + fileName;
			InputStream inputStream = classLoader
					.getResourceAsStream(expectedFilePath);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			for (int value = inputStream.read(); value != -1; value = inputStream
					.read()) {
				outputStream.write(value);
			}
			byte[] expectedContents = outputStream.toByteArray();

			// Validate the contents of the file
			ByteBuffer contents = httpFile.getContents();
			assertEquals("Incorrect content length", expectedContents.length,
					contents.remaining());
			for (int i = 0; i < expectedContents.length; i++) {
				byte expectedByte = expectedContents[i];
				byte actualByte = contents.get(i); // starts with 0 position
				assertEquals("Incorrect content byte at index " + i,
						expectedByte, actualByte);
			}
		}
	}

}