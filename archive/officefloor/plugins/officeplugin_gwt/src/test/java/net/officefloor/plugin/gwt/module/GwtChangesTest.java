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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.change.Change;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Tests the {@link GwtChanges}.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtChangesTest extends OfficeFrameTestCase {

	/**
	 * {@link GwtModuleRepository}.
	 */
	private final GwtModuleRepository repository = new GwtModuleRepositoryImpl(
			new ModelRepositoryImpl(), Thread.currentThread()
					.getContextClassLoader(), "src");

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext context = new MemoryConfigurationContext();

	/**
	 * {@link GwtFailureListener}.
	 */
	private final GwtFailureListener listener = this
			.createMock(GwtFailureListener.class);

	/**
	 * {@link GwtChanges} to test.
	 */
	private final GwtChanges changes = new GwtChangesImpl(this.repository,
			this.context, this.listener);

	/**
	 * Ensure correct GWT Module path.
	 */
	public void testGwtModulePath() {
		GwtModuleModel module = new GwtModuleModel("template",
				"net.example.client.ExampleEntryPoint", null);
		String gwtModulePath = this.changes.createGwtModulePath(module);
		assertEquals("Incorrect GWT Module path",
				"net/example/template.gwt.xml", gwtModulePath);
	}

	/**
	 * Ensure correct GWT Module path for root template.
	 */
	public void testRootGwtModulePath() {
		GwtModuleModel module = new GwtModuleModel("/",
				"net.example.client.ExampleEntryPoint", null);
		String gwtModulePath = this.changes.createGwtModulePath(module);
		assertEquals("Incorrect GWT Module path", "net/example/root.gwt.xml",
				gwtModulePath);
	}

	/**
	 * Ensure able to retrieve the GWT Module.
	 */
	public void testRetrieveGwtModule() throws Exception {

		// Create the GWT Module to retrieve
		GwtModuleModel module = new GwtModuleModel("uri",
				"net.officefloor.client.ExampleEntryPoint", null);
		this.repository.storeGwtModule(module, this.context, null);

		// Retrieve the module
		GwtModuleModel retrieved = this.changes
				.retrieveGwtModule("net/officefloor/uri.gwt.xml");
		assertNotNull("Should obtain module", retrieved);
		assertEquals("Incorrect retrieved module rename-to", "uri",
				retrieved.getRenameTo());
		assertEquals("Incorrect retrieved module EntryPoint",
				"net.officefloor.client.ExampleEntryPoint",
				retrieved.getEntryPointClassName());
	}

	/**
	 * Ensure able to retrieve the root GWT Module.
	 */
	public void testRetrieveRootGwtModule() throws Exception {

		// Create the GWT Module to retrieve
		GwtModuleModel module = new GwtModuleModel("/",
				"net.officefloor.client.ExampleEntryPoint", null);
		this.repository.storeGwtModule(module, this.context, null);

		// Retrieve the module
		GwtModuleModel retrieved = this.changes
				.retrieveGwtModule("net/officefloor/root.gwt.xml");
		assertNotNull("Should obtain module", retrieved);
		assertEquals("Incorrect retrieved module rename-to", "root",
				retrieved.getRenameTo());
		assertEquals("Incorrect retrieved module EntryPoint",
				"net.officefloor.client.ExampleEntryPoint",
				retrieved.getEntryPointClassName());
	}

	/**
	 * Ensure can create.
	 */
	public void testCreate() throws Exception {

		final String CONFIGURATION_PATH = "net/officefloor/create.gwt.xml";
		final String ENTRY_POINT_CLASS = "net.officefloor.client.Created";

		// GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("create");
		module.setEntryPointClassName(ENTRY_POINT_CLASS);

		// Test
		this.replayMockObjects();

		// Update the GWT Module
		Change<?> change = this.changes.updateGwtModule(module, null);

		// Ensure not available before applying
		this.assertConfiguration(CONFIGURATION_PATH, null);

		// Ensure created GWT Module
		change.apply();
		this.assertConfiguration(CONFIGURATION_PATH, ENTRY_POINT_CLASS);

		// Ensure delete GWT Module on reverting
		change.revert();
		this.assertConfiguration(CONFIGURATION_PATH, null);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can create GWT Module for root template.
	 */
	public void testCreateRootGwtModule() throws Exception {

		final String CONFIGURATION_PATH = "net/officefloor/root.gwt.xml";
		final String ENTRY_POINT_CLASS = "net.officefloor.client.Root";

		// GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("/");
		module.setEntryPointClassName(ENTRY_POINT_CLASS);

		// Test
		this.replayMockObjects();

		// Update the GWT Module
		Change<?> change = this.changes.updateGwtModule(module, null);

		// Ensure not available before applying
		this.assertConfiguration(CONFIGURATION_PATH, null);

		// Ensure created GWT Module
		change.apply();
		this.assertConfiguration(CONFIGURATION_PATH, ENTRY_POINT_CLASS);

		// Ensure delete GWT Module on reverting
		change.revert();
		this.assertConfiguration(CONFIGURATION_PATH, null);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can update.
	 */
	public void testUpdateInPlace() throws Exception {

		final String MODULE_PATH = "net/officefloor/update.gwt.xml";
		final String EXISTING_ENTRY_POINT = "net.officefloor.client.Existing";
		final String UPDATED_ENTRY_POINT = "net.officefloor.client.Updated";

		// Create the existing GWT Module
		GwtModuleModel existing = new GwtModuleModel();
		existing.setRenameTo("update");
		existing.setEntryPointClassName(EXISTING_ENTRY_POINT);
		this.repository.storeGwtModule(existing, this.context, null);

		// GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("update");
		module.setEntryPointClassName(UPDATED_ENTRY_POINT);

		// Test
		this.replayMockObjects();

		// Update the GWT Module
		Change<?> change = this.changes.updateGwtModule(module, MODULE_PATH);

		// Ensure no change yet applied
		this.assertConfiguration(MODULE_PATH, EXISTING_ENTRY_POINT);

		// Ensure update GWT Module on applying change
		change.apply();
		this.assertConfiguration(MODULE_PATH, UPDATED_ENTRY_POINT);

		// Ensure reset to original GWT Module on reverting
		change.revert();
		this.assertConfiguration(MODULE_PATH, EXISTING_ENTRY_POINT);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can update.
	 */
	public void testUpdateRelocating() throws Exception {

		final String EXISTING_MODULE_PATH = "net/officefloor/existing.gwt.xml";
		final String EXISTING_ENTRY_POINT = "net.officefloor.client.Existing";
		final String UPDATED_MODULE_PATH = "net/officefloor/update.gwt.xml";
		final String UPDATED_ENTRY_POINT = "net.officefloor.client.Updated";

		// Create the existing GWT Module
		GwtModuleModel existing = new GwtModuleModel();
		existing.setRenameTo("existing");
		existing.setEntryPointClassName(EXISTING_ENTRY_POINT);
		this.repository.storeGwtModule(existing, this.context, null);

		// GWT Module
		GwtModuleModel module = new GwtModuleModel();
		module.setRenameTo("update");
		module.setEntryPointClassName(UPDATED_ENTRY_POINT);

		// Test
		this.replayMockObjects();

		// Update the GWT Module
		Change<?> change = this.changes.updateGwtModule(module,
				EXISTING_MODULE_PATH);

		// Ensure no change yet applied
		this.assertConfiguration(EXISTING_MODULE_PATH, EXISTING_ENTRY_POINT);
		this.assertConfiguration(UPDATED_MODULE_PATH, null);

		// Ensure update GWT Module on applying change
		change.apply();
		this.assertConfiguration(EXISTING_MODULE_PATH, null);
		this.assertConfiguration(UPDATED_MODULE_PATH, UPDATED_ENTRY_POINT);

		// Ensure reset to original GWT Module on reverting
		change.revert();
		this.assertConfiguration(EXISTING_MODULE_PATH, EXISTING_ENTRY_POINT);
		this.assertConfiguration(UPDATED_MODULE_PATH, null);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Asserts the {@link ConfigurationItem}.
	 * 
	 * @param path
	 *            Path to the {@link ConfigurationItem}.
	 * @param expectedEntryPointClass
	 *            Expected EntryPoint class value. <code>null</code> indicates
	 *            the {@link ConfigurationItem} should not be available.
	 */
	private void assertConfiguration(String path, String expectedEntryPointClass)
			throws Exception {
		GwtModuleModel module = this.repository.retrieveGwtModuleModel(path,
				this.context);
		if (expectedEntryPointClass == null) {
			// Ensure configuration deleted
			assertNull("GWT Module should not be available", module);
		} else {
			// Ensure correct configuration
			assertNotNull("GWT Module should be available", module);
			assertEquals("Incorrect EntryPoint class", expectedEntryPointClass,
					module.getEntryPointClassName());
		}
	}

}