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
package net.officefloor.plugin.web.http.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.resource.HttpDirectory;
import net.officefloor.plugin.web.http.resource.HttpDirectoryImpl;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;

/**
 * Tests the {@link HttpDirectory}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpDirectoryTest extends OfficeFrameTestCase {

	/**
	 * Underlying directory for HTTP directory.
	 */
	private File classPathDirectory;

	/**
	 * Class path prefix
	 */
	private String classPathPrefix;

	/**
	 * {@link HttpDirectory} to test.
	 */
	private HttpDirectory directory;

	@Override
	protected void setUp() throws Exception {

		// Obtain the underlying directory for HTTP directory
		this.classPathDirectory = this.findFile(this.getClass(),
				"directory/index.html").getParentFile();

		// Obtain class path prefix
		this.classPathPrefix = this.getClass().getPackage().getName().replace(
				'.', '/');

		// Create the HTTP directory
		this.directory = new HttpDirectoryImpl("/directory/",
				this.classPathPrefix, "index.html");
	}

	/**
	 * Ensure able to retrieve the default {@link HttpFile}.
	 */
	public void testDefaultFile() {

		// Obtain the default file
		HttpFile defaultFile = this.directory.getDefaultFile();

		// Ensure correct default file
		assertNotNull("Should have default file", defaultFile);
		assertEquals("Incorrect default file path", "/directory/index.html",
				defaultFile.getPath());
	}

	/**
	 * Ensure able to list {@link HttpResource} instances.
	 */
	public void testListResources() {

		// List the resources
		HttpResource[] resources = this.directory.listResources();

		// Ensure correct resources
		assertEquals("Incorrect number of resources", 2, resources.length);
		HttpFile indexHtml = (HttpFile) resources[0];
		assertEquals("Incorrect child file path", "/directory/index.html",
				indexHtml.getPath());
		HttpDirectory subDirectory = (HttpDirectory) resources[1];
		assertEquals("Incorrect child directory path",
				"/directory/sub_directory/", subDirectory.getPath());
	}

	/**
	 * Ensure can serialise {@link HttpDirectory}.
	 */
	public void testSerialise() throws Exception {
		// Ensure can serialise
		this.doSerialiseTest(this.directory, "/directory/",
				this.classPathDirectory);
	}

	/**
	 * Ensure appropriately equals.
	 */
	public void testEquals() {
		HttpDirectory one = new HttpDirectoryImpl("/directory/",
				"class/path/prefix", "index.html");
		HttpDirectory two = new HttpDirectoryImpl("/directory/",
				"class/path/prefix", "index.html");
		assertTrue("Should be equal", one.equals(two));
		assertEquals("Hash should match", one.hashCode(), two.hashCode());
	}

	/**
	 * Ensure not equal on differences.
	 */
	public void testNotEquals() {
		final String RESOURCE_PATH = "/directory/";
		final String CLASS_PATH_PREFIX = "class/path/prefix";
		final HttpDirectory directory = new HttpDirectoryImpl(RESOURCE_PATH,
				CLASS_PATH_PREFIX);
		assertFalse("Should not match if different resource path", directory
				.equals(new HttpDirectoryImpl("/wrong/resource/path",
						CLASS_PATH_PREFIX)));
		assertFalse("Should not match if different class path prefix",
				directory.equals(new HttpDirectoryImpl(RESOURCE_PATH,
						"wrong/class/path/prefix")));
	}

	/**
	 * Ensure correct toString().
	 */
	public void testToString() {
		assertEquals(
				"Incorrect toString details",
				"HttpDirectoryImpl: /directory/ (Class path prefix: class/path/prefix)",
				new HttpDirectoryImpl("/directory/", "class/path/prefix")
						.toString());
	}

	/**
	 * Does the serialise testing.
	 * 
	 * @param httpDirectory
	 *            {@link HttpDirectory} to serialise test.
	 * @param resourcePath
	 *            Expected path.
	 */
	private void doSerialiseTest(HttpDirectory httpDirectory,
			String resourcePath, File classPathFile) throws Exception {

		// Serialise the HTTP directory
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ObjectOutputStream outputStream = new ObjectOutputStream(data);
		outputStream.writeObject(httpDirectory);
		outputStream.flush();

		// Retrieve the HTTP directory
		ObjectInputStream inputStream = new ObjectInputStream(
				new ByteArrayInputStream(data.toByteArray()));
		HttpDirectory retrievedDirectory = (HttpDirectory) inputStream
				.readObject();

		// Should be a new HTTP directory object
		assertNotSame("Should be new HTTP Directory object", httpDirectory,
				retrievedDirectory);

		// Validate the retrieved file
		assertEquals("Incorrect path", resourcePath, retrievedDirectory
				.getPath());
		assertTrue("Should exist", retrievedDirectory.isExist());

		// Ensure able to retrieve default file
		HttpFile defaultFile = retrievedDirectory.getDefaultFile();
		assertEquals("Incorrect default file", "/directory/index.html",
				defaultFile.getPath());

		// Ensure able to retrieve children
		HttpResource[] children = retrievedDirectory.listResources();
		assertEquals("Incorrect number of children", 2, children.length);
		assertEquals("Incorrect first child", "/directory/index.html",
				children[0].getPath());
		assertEquals("Incorrect second child", "/directory/sub_directory/",
				children[1].getPath());
	}

}