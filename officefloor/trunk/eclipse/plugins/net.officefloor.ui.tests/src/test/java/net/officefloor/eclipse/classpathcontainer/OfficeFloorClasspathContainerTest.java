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
package net.officefloor.eclipse.classpathcontainer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorClasspathContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorClasspathContainerTest extends OfficeFrameTestCase {

	/**
	 * Configuration.
	 */
	private static final String CONFIGURATION = "<officefloor><source-attachments><source-attachment class-path=\"class/path\" path=\"source/path\" root-path=\"source/root/path\"/></source-attachments><extension-classpath-providers><extension-classpath-provider class-name=\"provider.clazz.name\"/></extension-classpath-providers></officefloor>";

	/**
	 * Ensure can load the {@link OfficeFloorClasspathContainer}.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	@Test
	public void testLoad() throws Exception {

		// Obtain the raw container data
		InputStream configuration = new ByteArrayInputStream(
				CONFIGURATION.getBytes());

		// Create the container
		OfficeFloorClasspathContainer container = new OfficeFloorClasspathContainer(
				null);
		container.load(configuration);

		// Validate the source attachment entries
		List<SourceAttachmentEntry> sourceEntries = container
				.getSourceAttachmentEntries();
		assertEquals("Incorrect number of source attachment entries", 1,
				sourceEntries.size());
		SourceAttachmentEntry sourceEntry = sourceEntries.get(0);
		assertEquals("Incorrect class path", "class/path",
				sourceEntry.getClasspathPath());
		assertEquals("Incorrect source attachment path", "source/path",
				sourceEntry.getSourceAttachmentPath());
		assertEquals("Incorrect source attachment root path",
				"source/root/path", sourceEntry.getSourceAttachmentRootPath());

		// Validate the extension class path provider entries
		List<ExtensionClasspathProviderEntry> providerEntries = container
				.getExtensionClasspathProviderEntries();
		assertEquals("Incorrect number of provider entries", 1,
				providerEntries.size());
		ExtensionClasspathProviderEntry providerEntry = providerEntries.get(0);
		assertEquals("Incorrect provider class name", "provider.clazz.name",
				providerEntry.getExtensionClassName());
	}

	/**
	 * Ensure can store the {@link OfficeFloorClasspathContainer}.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	@Test
	public void testStore() throws Exception {

		// Create the container
		OfficeFloorClasspathContainer container = new OfficeFloorClasspathContainer(
				null);
		container.addSourceAttachmentEntry(new SourceAttachmentEntry(
				"class/path", "source/path", "source/root/path"));
		container
				.addExtensionClasspathProviderEntry(new ExtensionClasspathProviderEntry(
						"provider.clazz.name"));

		// Store the container
		ByteArrayOutputStream configuration = new ByteArrayOutputStream();
		container.store(configuration);
		String actualContent = new String(configuration.toByteArray());

		// Ensure stored configuration is correct
		assertEquals("Incorrect configuration", CONFIGURATION, actualContent);
	}

}