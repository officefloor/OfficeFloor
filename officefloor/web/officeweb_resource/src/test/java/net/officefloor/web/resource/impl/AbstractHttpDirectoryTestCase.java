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
package net.officefloor.web.resource.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.resource.HttpDirectory;

/**
 * Tests the {@link HttpDirectory}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpDirectoryTestCase extends OfficeFrameTestCase {

	/**
	 * Underlying directory for HTTP directory.
	 */
	private File testDirectory;

	/**
	 * {@link HttpDirectory} to test.
	 */
	private HttpDirectory httpDirectory;

	/**
	 * Creates the {@link HttpDirectory}.
	 * 
	 * @param resourcePath
	 *            Path to the {@link HttpDirectory}.
	 * @param defaultFileNames
	 *            Default file names.
	 * @return {@link HttpDirectory}.
	 * @throws IOException
	 *             If fails to create {@link HttpDirectory}.
	 */
	protected abstract HttpDirectory createHttpDirectory(String resourcePath, String... defaultFileNames)
			throws IOException;

	@Override
	protected void setUp() throws Exception {

		// Obtain the underlying directory for HTTP directory
		this.testDirectory = this.findFile(AbstractHttpDirectoryTestCase.class, "directory/index.html").getParentFile();

		// Create the HTTP directory
		this.httpDirectory = this.createHttpDirectory("/directory/", "index.html");
	}

	/**
	 * Ensure can serialise {@link HttpDirectory}.
	 */
	public void testSerialise() throws Exception {
		// Ensure can serialise
		this.doSerialiseTest(this.httpDirectory, "/directory/", this.testDirectory);
	}

	/**
	 * Ensure appropriately equals.
	 */
	public void testEquals() throws IOException {
		HttpDirectory one = this.createHttpDirectory("/directory/", "index.html");
		HttpDirectory two = this.createHttpDirectory("/directory/", "index.html");
		assertTrue("Should be equal", one.equals(two));
		assertEquals("Hash should match", one.hashCode(), two.hashCode());
	}

	/**
	 * Ensure not equal on differences.
	 */
	public void testNotEquals() throws IOException {
		final String RESOURCE_PATH = "/directory/";
		final HttpDirectory directory = this.createHttpDirectory(RESOURCE_PATH);
		assertFalse("Should not match if different resource path",
				directory.equals(this.createHttpDirectory("/empty")));
	}

	/**
	 * Does the serialise testing.
	 * 
	 * @param httpDirectory
	 *            {@link HttpDirectory} to serialise test.
	 * @param resourcePath
	 *            Expected path.
	 */
	private void doSerialiseTest(HttpDirectory httpDirectory, String resourcePath, File classPathFile)
			throws Exception {

		// Serialise the HTTP directory
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ObjectOutputStream outputStream = new ObjectOutputStream(data);
		outputStream.writeObject(httpDirectory);
		outputStream.flush();

		// Retrieve the HTTP directory
		ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(data.toByteArray()));
		HttpDirectory retrievedDirectory = (HttpDirectory) inputStream.readObject();

		// Should be a new HTTP directory object
		assertNotSame("Should be new HTTP Directory object", httpDirectory, retrievedDirectory);

		// Validate the retrieved file
		assertEquals("Incorrect path", resourcePath, retrievedDirectory.getPath());
		assertTrue("Should exist", retrievedDirectory.isExist());
	}

}