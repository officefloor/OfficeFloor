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
package net.officefloor.plugin.servlet.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests {@link ResourceLocator} implementations.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractResourceLocatorTestCase extends
		OfficeFrameTestCase {

	/**
	 * {@link ResourceLocator} for testing.
	 */
	private ResourceLocator resourceLocator;

	/**
	 * Creates the {@link ResourceLocator} to be tested.
	 * 
	 * @return {@link ResourceLocator} to be tested.
	 */
	protected abstract ResourceLocator createResourceLocator() throws Exception;

	@Override
	protected void setUp() throws Exception {
		// Create the resource locator
		this.resourceLocator = this.createResourceLocator();
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
		String expectedPath = expected.toString();
		expectedPath = expectedPath.substring(expectedPath
				.indexOf("test-classes"));
		URL actual = this.resourceLocator.getResource("test.txt");
		String actualPath = actual.toString();
		actualPath = actualPath.substring(actualPath.indexOf("test-classes"));
		assertEquals("Incorrect resource URL (path from class-path)",
				expectedPath, actualPath);
	}

	/**
	 * Ensure able to obtain the resource children.
	 */
	public void testResourceChildren() {

		// Obtain children for root
		Set<String> children = this.resourceLocator.getResourceChildren("/");

		// Remove files to be ignored
		final String[] ignoreFileExtensions = new String[] { ".class", ".svn" };
		IGNORE_CHECK: for (Iterator<String> removeIgnored = children.iterator(); removeIgnored
				.hasNext();) {
			String resourceName = removeIgnored.next();

			// Determine if resource to ignore
			for (String ignoreFileExtension : ignoreFileExtensions) {
				if (resourceName.toLowerCase().endsWith(ignoreFileExtension)) {
					// Resource to be ignored
					removeIgnored.remove();
					continue IGNORE_CHECK; // move to next to check
				}
			}
		}

		// Validate
		assertEquals("Expecting only one child", 1, children.size());
		assertEquals("Incorrect child", "test.txt", children
				.toArray(new String[0])[0]);
	}

}