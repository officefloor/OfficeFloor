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
	 * Ensure correct GWT Module path.
	 */
	public void testGwtModulePath() throws Exception {
		GwtModuleModel module = new GwtModuleModel("template",
				"net.example.client.ExampleEntryPoint", null);
		String gwtModulePath = this.repository.createGwtModulePath(module);
		assertEquals("Incorrect GWT Module path",
				"net/example/template.gwt.xml", gwtModulePath);
	}

	/**
	 * Ensure able to retrieve the {@link GwtModuleModel} not existing.
	 */
	public void testRetrieveNonExistentGwtModuleModule() throws Exception {

		// Create the configuration context
		ConfigurationContext context = new MemoryConfigurationContext();

		// Retrieve the GWT Module
		GwtModuleModel module = this.repository.retrieveGwtModuleModel(
				"not/exist/Resource.gwt.xml", context);
		assertNull("Should not retrieve GWT Module", module);
	}

	/**
	 * Ensure able to retrieve the {@link GwtModule} not existing.
	 */
	public void testRetrieveNonExistentGwtModule() throws Exception {

		// Create the configuration context
		ConfigurationContext context = new MemoryConfigurationContext();

		// Retrieve the GWT Module
		GwtModule module = this.repository.retrieveGwtModule(
				"not/exist/Resource.gwt.xml", context);
		assertNull("Should not retrieve GWT Module", module);
	}

	/**
	 * Ensure able to retrieve the {@link GwtModuleModel}.
	 */
	public void testRetrieveGwtModuleModel() throws Exception {

		// Determine configuration item location
		final String LOCATION = this.getFileLocation(this.getClass(),
				"test.gwt.xml");

		// Create the configuration context
		ConfigurationContext context = new ClassLoaderConfigurationContext(
				Thread.currentThread().getContextClassLoader()) {
			@Override
			public ConfigurationItem getConfigurationItem(String location)
					throws Exception {
				assertTrue("Ensure prefix path", location.startsWith("src/"));
				location = location.substring("src/".length());
				return super.getConfigurationItem(location);
			}
		};

		// Retrieve the GWT Module
		GwtModuleModel module = this.repository.retrieveGwtModuleModel(
				LOCATION, context);
		assertNotNull("Should have module", module);

		// Ensure correct values
		assertEquals("Incorrect rename-to", "example", module.getRenameTo());
		assertEquals("Incorrect EntryPoint class",
				"net.officefloor.plugin.gwt.client.Example",
				module.getEntryPointClassName());
	}

	/**
	 * Ensure able to retrieve the {@link GwtModule}.
	 */
	public void testRetrieveGwtModule() throws Exception {

		// Determine configuration item location
		final String LOCATION = this.getFileLocation(this.getClass(),
				"test.gwt.xml");

		// Obtain the file contents
		String fileContents = this.getFileContents(this.findFile(
				this.getClass(), "test.gwt.xml"));

		// Create the configuration context
		ConfigurationContext context = new ClassLoaderConfigurationContext(
				Thread.currentThread().getContextClassLoader()) {
			@Override
			public ConfigurationItem getConfigurationItem(String location)
					throws Exception {
				assertTrue("Ensure prefix path", location.startsWith("src/"));
				location = location.substring("src/".length());
				return super.getConfigurationItem(location);
			}
		};

		// Retrieve the GWT Module
		GwtModule module = this.repository.retrieveGwtModule(LOCATION, context);
		assertNotNull("Should have module", module);

		// Ensure correct values
		assertEquals("Incorrect location", LOCATION, module.getLocation());
		assertEquals("Incorrect contents", fileContents,
				new String(module.getContents()));
	}

	/**
	 * Ensure able to create the {@link GwtModuleModel}.
	 */
	public void testCreateGwtModule() throws Exception {

		// Configure the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("example");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.client.Example");
		module.addInherit("net.officefloor.plugin.gwt.ExampleModule");

		// Create the GWT Module
		InputStream content = this.repository.createGwtModule(module);

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"create.gwt.xml"));
		String actual = this.getText(content);
		assertXml("Incorrect created module", expected, actual);
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
		module.setEntryPointClassName("net.officefloor.plugin.gwt.update.Updated");

		// Update the GWT Module
		InputStream updated = this.repository.updateGwtModule(module, initial);

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"updated.gwt.xml"));
		String actual = this.getText(updated);
		assertXml("Incorrect updated module", expected, actual);
	}

	/**
	 * Ensure able to update {@link GwtModuleModel} with additional inheritance.
	 */
	public void testUpdateGwtModuleWithInherits() throws Exception {

		// Create the configuration item
		InputStream initial = this.findInputStream(this.getClass(),
				"change.gwt.xml");

		// Configure to update the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("update");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.update.Updated");
		module.addInherit("net.officefloor.plugin.gwt.ExampleModule");

		// Update the GWT Module
		InputStream updated = this.repository.updateGwtModule(module, initial);

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"updated_inherits.gwt.xml"));
		String actual = this.getText(updated);
		assertXml("Incorrect updated module", expected, actual);
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
		module.setEntryPointClassName("net.officefloor.plugin.gwt.update.Updated");

		// Update the GWT Module
		InputStream updated = this.repository.updateGwtModule(module, initial);

		// Obtain the file content (touch up due to not adding white-space)
		String actual = this.getText(updated);

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"updated.gwt.xml"));
		assertXml("Incorrect updated module", expected, actual);
	}

	/**
	 * Ensure appropriately stores the {@link GwtModule}.
	 */
	public void testStoreGwtModule() throws Exception {

		// Determine configuration item location
		final String LOCATION = this.getFileLocation(this.getClass(),
				"test.gwt.xml");

		// Obtain the file contents
		final String fileContents = this.getText(this.findInputStream(
				this.getClass(), "test.gwt.xml"));

		// Create the GWT Module
		GwtModule module = new GwtModule() {
			@Override
			public String getLocation() {
				return LOCATION;
			}

			@Override
			public byte[] getContents() {
				return fileContents.getBytes();
			}
		};

		// Configure context
		ConfigurationContext context = new MemoryConfigurationContext();

		// Store GWT Module
		this.repository.storeGwtModule(module, context);

		// Ensure create appropriate content
		ConfigurationItem item = context
				.getConfigurationItem("src/net/officefloor/plugin/gwt/module/test.gwt.xml");
		assertNotNull("Ensure module created", item);
		String actual = this.getText(item.getConfiguration());

		// Ensure appropriate content
		assertXml("Incorrect stored module", fileContents, actual);
	}

	/**
	 * Ensure appropriately creates a new GWT Module.
	 */
	public void testStoreGwtModule_Create() throws Exception {

		// Configure the GWT Module to create
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("example");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.client.Example");
		module.addInherit("net.officefloor.plugin.gwt.ExampleModule");

		// Configure context
		ConfigurationContext context = new MemoryConfigurationContext();

		// Store (creating) GWT Module
		String modulePath = this.repository.storeGwtModule(module, context,
				null);
		assertEquals("Incorrect GWT Module path",
				"net/officefloor/plugin/gwt/example.gwt.xml", modulePath);

		// Ensure create appropriate content
		ConfigurationItem item = context
				.getConfigurationItem("src/net/officefloor/plugin/gwt/example.gwt.xml");
		assertNotNull("Ensure module created", item);
		String actual = this.getText(item.getConfiguration());

		// Ensure appropriate content
		String expected = this.getText(this.findInputStream(this.getClass(),
				"create.gwt.xml"));
		assertXml("Incorrect stored (created) module", expected, actual);
	}

	/**
	 * Ensure appropriately updates the existing GWT Module.
	 */
	public void testStoreGwtModule_Update() throws Exception {

		final String MODULE_PATH = "net/officefloor/plugin/gwt/update.gwt.xml";

		// Configure to update the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("update");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.update.Updated");

		// Create the configuration context
		ConfigurationContext context = new MemoryConfigurationContext();
		InputStream initial = this.findInputStream(this.getClass(),
				"change.gwt.xml");
		context.createConfigurationItem("src/" + MODULE_PATH, initial);

		// Store (update) the GWT Module
		String modulePath = this.repository.storeGwtModule(module, context,
				MODULE_PATH);
		assertEquals("Incorrect GWT Module path",
				"net/officefloor/plugin/gwt/update.gwt.xml", modulePath);

		// Obtain the updated content
		ConfigurationItem item = context.getConfigurationItem("src/"
				+ MODULE_PATH);
		String actual = this.getText(item.getConfiguration());

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"updated.gwt.xml"));
		assertXml("Incorrect stored (updated) module", expected, actual);
	}

	/**
	 * Ensure appropriately relocates and updates the GWT Module.
	 */
	public void testStoreGwtModule_Relocate() throws Exception {

		final String MODULE_PATH = "old/location/change.gwt.xml";

		// Configure to update the GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("update");
		module.setEntryPointClassName("net.officefloor.plugin.gwt.update.Updated");

		// Create the configuration context
		ConfigurationContext context = new MemoryConfigurationContext();
		InputStream initial = this.findInputStream(this.getClass(),
				"change.gwt.xml");
		context.createConfigurationItem("src/" + MODULE_PATH, initial);

		// Store (relocated and update) the GWT Module
		String modulePath = this.repository.storeGwtModule(module, context,
				MODULE_PATH);
		assertEquals("Incorrect GWT Module path",
				"net/officefloor/plugin/gwt/update.gwt.xml", modulePath);

		// Obtain the relocated and updated content
		ConfigurationItem item = context
				.getConfigurationItem("src/net/officefloor/plugin/gwt/update.gwt.xml");
		String actual = this.getText(item.getConfiguration());

		// Validate content as expected
		String expected = this.getText(this.findInputStream(this.getClass(),
				"updated.gwt.xml"));
		assertXml("Incorrect stored (updated) module", expected, actual);

		// Ensure the previous GWT Module is removed
		assertNull("Previous GWT Module should be removed",
				context.getConfigurationItem("src/" + MODULE_PATH));
	}

	/**
	 * Ensure can delete GWT Module that does not exist.
	 */
	public void testDeleteNonExistentGwtModule() throws Exception {

		final String MODULE_PATH = "net/officefloor/test.gwt.xml";

		// Create the configuration context with no GWT Module
		ConfigurationContext context = new MemoryConfigurationContext();

		// Ensure delete the GWT Module
		this.repository.deleteGwtModule(MODULE_PATH, context);

		// Ensure GWT Module still not exist
		assertNull("GWT Module should not exist",
				context.getConfigurationItem(MODULE_PATH));
	}

	/**
	 * Ensure can delete GWT Module.
	 */
	public void testDeleteGwtModule() throws Exception {

		final String MODULE_PATH = "net/officefloor/test.gwt.xml";
		final String CONFIG_PATH = "src/" + MODULE_PATH;

		// Create the configuration context with GWT Module
		ConfigurationContext context = new MemoryConfigurationContext();
		InputStream initial = this.findInputStream(this.getClass(),
				"change.gwt.xml");
		context.createConfigurationItem(CONFIG_PATH, initial);
		assertNotNull("Should have configuration for GWT Module",
				context.getConfigurationItem(CONFIG_PATH));

		// Ensure delete the GWT Module
		this.repository.deleteGwtModule(MODULE_PATH, context);

		// Ensure GWT Module deleted
		assertNull("GWT Module should be deleted",
				context.getConfigurationItem(CONFIG_PATH));
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

	/**
	 * Asserts the XML text to match.
	 * 
	 * @param message
	 *            Message.
	 * @param expected
	 *            Expected XML text.
	 * @param actual
	 *            Actual XML text.
	 */
	private static void assertXml(String message, String expected, String actual) {
		// Remove tabs from xml (formatting discrepency)
		expected = expected.replace("\t", "");
		actual = actual.replace("\t", "");
		assertEquals(message, expected, actual);
	}

}