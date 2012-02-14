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

package net.officefloor.plugin.web.http.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpFileImpl;

/**
 * Test the {@link HttpFile}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileTest extends OfficeFrameTestCase {

	/**
	 * Ensure can serialise a {@link HttpFile} with no description.
	 */
	public void testSerialise() throws Exception {

		// Obtain the underlying file for HTTP file
		File classPathFile = this.findFile(this.getClass(), "index.html");

		// Obtain class path for file
		String classPath = this.getClass().getPackage().getName().replace('.',
				'/')
				+ "/index.html";

		// Create HTTP File (with no description)
		HttpFile httpFile = new HttpFileImpl("/index.html", classPath, null,
				null, null);

		// Ensure can serialise
		this.doSerialiseTest(httpFile, "/index.html", classPathFile, "", "",
				null);
	}

	/**
	 * Ensure can serialise a {@link HttpFile} with a description (especially a
	 * {@link Charset}).
	 */
	public void testSerialiseWithDescription() throws Exception {

		// Obtain the underlying file for HTTP file
		File classPathFile = this.findFile(this.getClass(), "index.html");

		// Obtain class path for file
		String classPath = this.getClass().getPackage().getName().replace('.',
				'/')
				+ "/index.html";

		// Create the charset
		Charset charset = Charset.defaultCharset();

		// Create Http File (with mock details)
		HttpFile httpFile = new HttpFileImpl("/index.html", classPath, "zip",
				"text/html", charset);

		// Ensure can serialise
		this.doSerialiseTest(httpFile, "/index.html", classPathFile, "zip",
				"text/html", charset);
	}

	/**
	 * Ensure equals is based on details of {@link HttpFile}.
	 */
	public void testEquals() {
		Charset charset = Charset.defaultCharset();
		HttpFile one = new HttpFileImpl("/resource.html", "resource.html",
				"encoding", "type", charset);
		HttpFile two = new HttpFileImpl("/resource.html", "resource.html",
				"encoding", "type", charset);
		assertEquals("Should equal", one, two);
		assertEquals("Same hash", one.hashCode(), two.hashCode());
	}

	/**
	 * Ensures not equals should details differ.
	 */
	public void testNotEquals() {
		final String RESOURCE_PATH = "/resource.html";
		final String CLASS_PATH = "resource.html";
		final String ENCODING = "encoding";
		final String TYPE = "type";
		final Charset CHARSET = Charset.defaultCharset();
		HttpFile file = new HttpFileImpl(RESOURCE_PATH, CLASS_PATH, ENCODING,
				TYPE, CHARSET);
		assertFalse("Should not match if different resource path", file
				.equals(new HttpFileImpl("/wrong.html", CLASS_PATH, ENCODING,
						TYPE, CHARSET)));
		assertFalse("Should not match if different class path", file
				.equals(new HttpFileImpl(RESOURCE_PATH, "wrong.html", ENCODING,
						TYPE, CHARSET)));
		assertFalse("Should not match if different encoding", file
				.equals(new HttpFileImpl(RESOURCE_PATH, CLASS_PATH, "wrong",
						TYPE, CHARSET)));
		assertFalse("Should not match if different type", file
				.equals(new HttpFileImpl(RESOURCE_PATH, CLASS_PATH, ENCODING,
						"wrong", CHARSET)));
		assertFalse("Should not match if different charset", file
				.equals(new HttpFileImpl(RESOURCE_PATH, CLASS_PATH, ENCODING,
						TYPE, null)));
	}

	/**
	 * Ensure obtain details from <code>toString</code> method.
	 */
	public void testToString() {
		final Charset charset = Charset.defaultCharset();
		assertEquals(
				"Incorrect toString with full details",
				"HttpFileImpl: /resource.html (Class path: resource.html, Content-Encoding: encoding, Content-Type: type; charset="
						+ charset.name() + ")", new HttpFileImpl(
						"/resource.html", "resource.html", "encoding", "type",
						charset).toString());
		assertEquals("Incorrect toString with no details",
				"HttpFileImpl: /resource.html (Class path: resource.html)",
				new HttpFileImpl("/resource.html", "resource.html", null, null,
						null).toString());
	}

	/**
	 * Does the serialise testing.
	 * 
	 * @param httpFile
	 *            {@link HttpFile} to serialise test.
	 * @param resourcePath
	 *            Expected path.
	 * @param classPathFile
	 *            Class path file.
	 * @param contentEncoding
	 *            Expected content encoding.
	 * @param contentType
	 *            Expected content type.
	 * @param charset
	 *            Expected {@link Charset}.
	 */
	private void doSerialiseTest(HttpFile httpFile, String resourcePath,
			File classPathFile, String contentEncoding, String contentType,
			Charset charset) throws Exception {

		// Serialise the HTTP file
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ObjectOutputStream outputStream = new ObjectOutputStream(data);
		outputStream.writeObject(httpFile);
		outputStream.flush();

		// Retrieve the HTTP file
		ObjectInputStream inputStream = new ObjectInputStream(
				new ByteArrayInputStream(data.toByteArray()));
		HttpFile retrievedFile = (HttpFile) inputStream.readObject();

		// Should be a new HTTP file object
		assertNotSame("Should be new HTTP File object", httpFile, retrievedFile);

		// Validate the retrieved file
		assertEquals("Incorrect path", resourcePath, retrievedFile.getPath());
		assertTrue("Should exist", retrievedFile.isExist());
		assertEquals("Incorrect content-encoding", contentEncoding,
				retrievedFile.getContentEncoding());
		assertEquals("Incorrect content-type", contentType, retrievedFile
				.getContentType());
		assertEquals("Incorrect charset", charset, retrievedFile.getCharset());

		// Obtain the expected contents
		String expectedFileContents = this.getFileContents(classPathFile);

		// Obtain the actual contents
		ByteBuffer contents = httpFile.getContents();
		byte[] fileData = new byte[contents.limit()];
		contents.get(fileData);
		String actualFileContents = new String(fileData);

		// Ensure correct contents
		assertEquals("Incorrect file contents", expectedFileContents,
				actualFileContents);
	}

}