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
package net.officefloor.plugin.gwt.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Tests the {@link GwtModuleRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtModuleRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link GwtModuleRepository} to test.
	 */
	private final GwtModuleRepository repository = new GwtModuleRepositoryImpl(
			new ModelRepositoryImpl());

	/**
	 * Ensure able to retrieve the {@link GwtModuleModel}.
	 */
	public void testRetrieveGwtModule() throws Exception {

		// Determine configuration item location
		final String LOCATION = this.getFileLocation(this.getClass(),
				"test.gwt.xml");

		// Obtain the configuration item
		ClassLoaderConfigurationContext context = new ClassLoaderConfigurationContext(
				Thread.currentThread().getContextClassLoader());
		ConfigurationItem configuration = context
				.getConfigurationItem(LOCATION);
		assertNotNull("Must have configuration", configuration);

		// Retrieve the GWT Module
		GwtModuleModel module = this.repository
				.retrieveGwtModule(configuration);
		assertNotNull("Should have module", module);

		// Ensure correct values
		assertEquals("Incorrect rename-to", "example", module.getRenameTo());
		assertEquals("Incorrect EntryPoint class",
				"net.officefloor.plugin.gwt.client.Example",
				module.getEntryPointClassName());
	}

	/**
	 * Ensure able to create the {@link GwtModuleModel}.
	 */
	public void testCreateGwtModule() throws Exception {

		// Create the configuration item
		ConfigurationItem configuration = new MemoryConfigurationItem("TEST",
				new ClassLoaderConfigurationContext(Thread.currentThread()
						.getContextClassLoader()));

		// Configure the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("example");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.client.Example");

		// Create the GWT Module
		this.repository.createGwtModule(module, configuration);

		// Obtain the file content
		String actual = this.getText(configuration.getConfiguration());

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"create.gwt.xml"));
		assertEquals("Incorrect created module", expected, actual);
	}

	/**
	 * Ensure able to update the {@link GwtModuleModel}.
	 */
	public void testUpdateGwtModule() throws Exception {

		// Create the configuration item
		InputStream initial = this.findInputStream(this.getClass(),
				"change.gwt.xml");
		ConfigurationItem configuration = new MemoryConfigurationItem("TEST");
		configuration.setConfiguration(initial);

		// Configure to update the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("update");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.client.Updated");

		// Update the GWT Module
		this.repository.updateGwtModule(module, configuration);

		// Obtain the file content
		String actual = this.getText(configuration.getConfiguration());

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"updated.gwt.xml"));
		assertEquals("Incorrect created module", expected, actual);
	}

	/**
	 * Ensure able to update the {@link GwtModuleModel} with empty
	 * configuration.
	 */
	public void testUpdateEmptyGwtModule() throws Exception {

		// Create the configuration item
		InputStream initial = this.findInputStream(this.getClass(),
				"empty.gwt.xml");
		ConfigurationItem configuration = new MemoryConfigurationItem("TEST");
		configuration.setConfiguration(initial);

		// Configure to update the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("update");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.client.Updated");

		// Update the GWT Module
		this.repository.updateGwtModule(module, configuration);

		// Obtain the file content
		String actual = this.getText(configuration.getConfiguration());

		// Touch up due to not adding white-space
		actual = actual.replace("<source", "\t<source");

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"updated.gwt.xml"));
		assertEquals("Incorrect created module", expected, actual);
	}

	/**
	 * Obtains the text.
	 * 
	 * @param content
	 *            Content.
	 * @return Text.
	 */
	private String getText(InputStream content) throws IOException {
		Reader reader = new InputStreamReader(content);
		StringWriter writer = new StringWriter();
		for (int character = reader.read(); character != -1; character = reader
				.read()) {
			writer.write(character);
		}
		return writer.toString();
	}

}