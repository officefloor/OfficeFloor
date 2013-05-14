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
package net.officefloor.plugin.woof.gwt;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.woof.WoofChangesImpl;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.gwt.module.GwtChanges;
import net.officefloor.plugin.gwt.module.GwtChangesImpl;
import net.officefloor.plugin.gwt.module.GwtModuleRepositoryImpl;

/**
 * Tests the static methods of {@link WoofChangesImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofChangesMethodsTest extends OfficeFrameTestCase {

	/**
	 * {@link GwtChanges}.
	 */
	private GwtChanges changes;

	/**
	 * {@link WoofTemplateModel}.
	 */
	private WoofTemplateModel template;

	@Override
	protected void setUp() throws Exception {

		// Obtain the changes
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		ClassLoaderConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				classLoader);
		this.changes = new GwtChangesImpl(new GwtModuleRepositoryImpl(
				new ModelRepositoryImpl(), classLoader, null),
				configurationContext, null);

		// Obtain the configuration
		final String applicationWoofPath = this.getClass().getPackage()
				.getName().replace('.', '/')
				+ "/methods.woof.xml";
		ConfigurationContext context = new ClassLoaderConfigurationContext(
				Thread.currentThread().getContextClassLoader());
		ConfigurationItem configuration = context
				.getConfigurationItem(applicationWoofPath);
		assertNotNull("Invalid test, no configuration", configuration);

		// Load the woof configuration
		WoofModel model = new WoofRepositoryImpl(new ModelRepositoryImpl())
				.retrieveWoOF(configuration);

		// Obtain the template
		this.template = model.getWoofTemplates().get(0);
	}

	/**
	 * Ensure able to obtain correct GWT Entry Point class name.
	 */
	public void testGwtEntryPointClassName() {
		assertEquals("Incorrect GWT EntryPoint class name",
				"net.example.client.ExampleGwtEntryPoint",
				GwtWoofTemplateExtensionSource.getGwtEntryPointClassName(
						this.template, this.changes));
	}

	/**
	 * Ensure able to obtain correct GWT async interface names.
	 */
	public void testGwtAsyncServiceInterfaceNames() {
		String[] expected = new String[] { "net.example.AsyncInterface",
				"net.example.AnotherAsyncInterface" };
		String[] actual = GwtWoofTemplateExtensionSource
				.getGwtAsyncServiceInterfaceNames(this.template);
		assertEquals("Incorrect number of interfaces", expected.length,
				actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals("Incorrect interface name " + i, expected[i],
					actual[i]);
		}
	}

	/**
	 * Ensure able to determine if Comet is enabled.
	 */
	public void testCometEnabled() {
		assertTrue("Comet should be enabled",
				GwtWoofTemplateExtensionSource.isCometEnabled(this.template));
	}

	/**
	 * Ensure able to determine the Comet manual publish method name.
	 */
	public void testCometManualPublishMethodName() {
		assertEquals("Incorrect comet manual publish method name",
				"manualPublish",
				GwtWoofTemplateExtensionSource
						.getCometManualPublishMethodName(this.template));
	}

}