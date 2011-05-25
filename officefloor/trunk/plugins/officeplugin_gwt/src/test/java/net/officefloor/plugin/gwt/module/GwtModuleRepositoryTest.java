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
import net.officefloor.model.impl.repository.memory.MemoryConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
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
			new ModelRepositoryImpl(), Thread.currentThread()
					.getContextClassLoader(), "src");

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

		// Configure the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("example");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.client.Example");

		// Create the GWT Module
		InputStream content = this.repository.createGwtModule(module);

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"create.gwt.xml"));
		String actual = this.getText(content);
		assertEquals("Incorrect created module", expected, actual);
	}

	/**
	 * Ensure able to update the {@link GwtModuleModel}.
	 */
	public void testUpdateGwtModule() throws Exception {

		// Create the configuration item
		InputStream initial = this.findInputStream(this.getClass(),
				"change.gwt.xml");

		// Configure to update the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("update");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.client.Updated");

		// Update the GWT Module
		InputStream updated = this.repository.updateGwtModule(module, initial);

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"updated.gwt.xml"));
		String actual = this.getText(updated);
		assertEquals("Incorrect updated module", expected, actual);
	}

	/**
	 * Ensure able to update the {@link GwtModuleModel} with empty
	 * configuration.
	 */
	public void testUpdateEmptyGwtModule() throws Exception {

		// Create the configuration item
		InputStream initial = this.findInputStream(this.getClass(),
				"empty.gwt.xml");

		// Configure to update the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("update");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.client.Updated");

		// Update the GWT Module
		InputStream updated = this.repository.updateGwtModule(module, initial);

		// Obtain the file content (touch up due to not adding white-space)
		String actual = this.getText(updated);
		actual = actual.replace("<source", "\t<source");

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"updated.gwt.xml"));
		assertEquals("Incorrect updated module", expected, actual);
	}

	/**
	 * Ensure appropriately creates a new GWT Module.
	 */
	public void testStoreGwtModule_Create() throws Exception {

		// Configure the GWT Module to create
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("example");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.client.Example");

		// Configure context
		ConfigurationContext context = new MemoryConfigurationContext();

		// Store (creating) GWT Module
		this.repository.storeGwtModule(module, context, null);

		// Ensure create appropriate content
		ConfigurationItem item = context
				.getConfigurationItem("src/net/officefloor/plugin/gwt/example.gwt.xml");
		assertNotNull("Ensure module created", item);
		String actual = this.getText(item.getConfiguration());

		// Ensure appropriate content
		String expected = this.getText(this.findInputStream(this.getClass(),
				"create.gwt.xml"));
		assertEquals("Incorrect stored (created) module", expected, actual);
	}

	/**
	 * Ensure appropriately updates the existing GWT Module.
	 */
	public void testStoreGwtModule_Update() throws Exception {

		final String MODULE_PATH = "net/officefloor/plugin/gwt/update.gwt.xml";

		// Configure to update the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("update");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.client.Updated");

		// Create the configuration context
		ConfigurationContext context = new MemoryConfigurationContext();
		InputStream initial = this.findInputStream(this.getClass(),
				"change.gwt.xml");
		context.createConfigurationItem("src/" + MODULE_PATH, initial);

		// Store (update) the GWT Module
		this.repository.storeGwtModule(module, context, MODULE_PATH);

		// Obtain the updated content
		ConfigurationItem item = context.getConfigurationItem("src/"
				+ MODULE_PATH);
		String actual = this.getText(item.getConfiguration());

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"updated.gwt.xml"));
		assertEquals("Incorrect stored (updated) module", expected, actual);
	}

	/**
	 * Ensure appropriately relocates and updates the GWT Module.
	 */
	public void testStoreGwtModule_Relocate() throws Exception {

		final String MODULE_PATH = "old/location/change.gwt.xml";

		// Configure to update the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("update");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.client.Updated");

		// Create the configuration context
		ConfigurationContext context = new MemoryConfigurationContext();
		InputStream initial = this.findInputStream(this.getClass(),
				"change.gwt.xml");
		context.createConfigurationItem("src/" + MODULE_PATH, initial);

		// Store (relocated and update) the GWT Module
		this.repository.storeGwtModule(module, context, MODULE_PATH);

		// Obtain the relocated and updated content
		ConfigurationItem item = context
				.getConfigurationItem("src/net/officefloor/plugin/gwt/update.gwt.xml");
		String actual = this.getText(item.getConfiguration());

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"updated.gwt.xml"));
		assertEquals("Incorrect stored (updated) module", expected, actual);

		// Ensure the previous GWT Module is removed
		assertNull("Previous GWT Module should be removed",
				context.getConfigurationItem("src/" + MODULE_PATH));
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