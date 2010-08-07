/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Set;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link FileSystemResourceLocator}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileSystemResourceLocatorTest extends OfficeFrameTestCase {

	/**
	 * {@link ResourceLocator} for testing.
	 */
	private ResourceLocator resourceLocator;

	@Override
	protected void setUp() throws Exception {

		// Obtain file system root
		File root = this.findFile(this.getClass(), ".");

		// Create the resource locator
		this.resourceLocator = new FileSystemResourceLocator(root);
	}

	/**
	 * Ensure able to obtain content.
	 */
	public void testResourceAsStream() throws IOException {

		// No resource
		assertNull("No resource", this.resourceLocator
				.getResourceAsStream("NoResource"));

		// Ensure able to obtain content
		InputStream inputStream = this.resourceLocator
				.getResourceAsStream("test.txt");
		assertNotNull("Resource expected to be available", inputStream);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		assertEquals("Incorrect resource content", "available", reader
				.readLine());
	}

	/**
	 * Ensure able to obtain resource as {@link URL}.
	 */
	public void testResourceAsUrl() throws IOException {

		// No resource
		assertNull("No resource", this.resourceLocator
				.getResource("NoResource"));

		// Ensure able to obtain resource URL
		URL expected = this.findFile(this.getClass(), "test.txt")
				.getCanonicalFile().toURI().toURL();
		assertEquals("Incorrect resource URL", expected, this.resourceLocator
				.getResource("test.txt"));
	}

	/**
	 * Ensure able to obtain the resource children.
	 */
	public void testResourceChildren() {
		Set<String> children = this.resourceLocator.getResourceChildren(".");

		// Remove svn file (as will be picked up)
		children.remove(".svn");

		assertEquals("Expecting only one child", 1, children.size());
		assertEquals("Incorrect child", "test.txt", children
				.toArray(new String[0])[0]);
	}

}