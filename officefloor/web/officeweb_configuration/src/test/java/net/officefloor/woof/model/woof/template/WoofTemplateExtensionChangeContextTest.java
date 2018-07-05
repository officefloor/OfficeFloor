/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.woof.model.woof.template;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.impl.classloader.ClassLoaderConfigurationContext;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.woof.model.woof.WoofChangeIssues;
import net.officefloor.woof.model.woof.WoofTemplateExtension;
import net.officefloor.woof.model.woof.WoofTemplateExtensionChangeContextImpl;
import net.officefloor.woof.template.WoofTemplateExtensionChangeContext;
import net.officefloor.woof.template.WoofTemplateExtensionConfiguration;

/**
 * Tests the {@link WoofTemplateExtensionChangeContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionChangeContextTest extends OfficeFrameTestCase {

	/**
	 * {@link WoofChangeIssues}.
	 */
	private final WoofChangeIssues issues = this.createMock(WoofChangeIssues.class);

	/**
	 * Validate the context.
	 */
	public void testContext() throws Exception {

		// Create the properties
		SourceProperties oldProperties = new SourcePropertiesImpl("OLD", "VALUE");
		SourceProperties newProperties = new SourcePropertiesImpl("NEW", "value");

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		// Create the source context
		ResourceSource resourceSource = new ResourceSource() {
			@Override
			public InputStream sourceResource(String location) {
				try {
					return WoofTemplateExtensionChangeContextTest.this
							.findInputStream(WoofTemplateExtensionChangeContextTest.this.getClass(), location);
				} catch (FileNotFoundException ex) {
					return null; // not found
				}
			}
		};
		SourceContext sourceContext = new SourceContextImpl(false, classLoader, resourceSource);

		// Create the configuration context
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(classLoader, null);

		// Create the context
		WoofTemplateExtensionChangeContext context = new WoofTemplateExtensionChangeContextImpl(true, sourceContext,
				"oldUri", oldProperties, "newUri", newProperties, configurationContext, this.issues);

		// Validate details
		assertTrue("Incorrect indicating of loading type", context.isLoadingType());

		// Validate issue
		assertSame("Incorrect issues", this.issues, context.getWoofChangeIssues());

		// Validate source context
		assertWoofTemplateExtensionChangeContextResource(
				context.getResource("WoofTemplateExtensionChangeContextResource.txt"));

		// Validate context uses new properties
		assertEquals("Context to use new properties", "value", context.getProperty("NEW"));

		// Validate the configuration context
		String packageLocation = this.getPackageRelativePath(this.getClass());
		assertWoofTemplateExtensionChangeContextResource(context.getConfigurationContext()
				.getConfigurationItem(packageLocation + "/WoofTemplateExtensionChangeContextResource.txt", null)
				.getInputStream());

		// Validate the configurations
		assertConfiguration(context.getOldConfiguration(), "oldUri", "OLD", "VALUE");
		assertConfiguration(context.getNewConfiguration(), "newUri", "NEW", "value");
	}

	/**
	 * Ensure can load with no properties. This is the context when
	 * creating/removing the {@link WoofTemplateExtension}.
	 */
	public void testNoProperties() throws IOException {

		final String NEW_PROPERTY_NAME = "NEW";

		// Create the properties
		SourceProperties oldProperties = new SourcePropertiesImpl("OLD", "VALUE");
		SourceProperties newProperties = new SourcePropertiesImpl(NEW_PROPERTY_NAME, "value");

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		// Create the source context
		SourceContext sourceContext = new SourceContextImpl(false, classLoader);

		// Create the configuration context
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(classLoader, null);

		// Create the context
		WoofTemplateExtensionChangeContext context = new WoofTemplateExtensionChangeContextImpl(true, sourceContext,
				null, oldProperties, null, newProperties, configurationContext, this.issues);

		// Validate issue
		assertSame("Incorrect issues", this.issues, context.getWoofChangeIssues());

		// Validate context has no properties
		assertNull("Context should not have properties if no new configuration",
				context.getProperty(NEW_PROPERTY_NAME, null));

		// Validate the configurations do not exist
		assertNull("Should not be old configuration", context.getOldConfiguration());
		assertNull("Should not be new configuration", context.getNewConfiguration());
	}

	/**
	 * Validates the resource is correct.
	 * 
	 * @param resource
	 *            Contents of the resource.
	 */
	private static void assertWoofTemplateExtensionChangeContextResource(InputStream resource) throws IOException {
		assertNotNull("Should find resource", resource);
		Reader resourceReader = new InputStreamReader(resource);
		StringWriter resourceWriter = new StringWriter();
		for (int character = resourceReader.read(); character != -1; character = resourceReader.read()) {
			resourceWriter.write(character);
		}
		assertEquals("Incorrect resource", "WoofTemplateExtensionChangeContext RESOURCE", resourceWriter.toString());
	}

	/**
	 * Validates the {@link WoofTemplateExtensionConfiguration}.
	 * 
	 * @param configuration
	 *            {@link WoofTemplateExtensionConfiguration} to validate.
	 * @param expectedUri
	 *            Expected URI.
	 * @param expectedPropertyNameValues
	 *            Expected property name/value pairs.
	 */
	private static void assertConfiguration(WoofTemplateExtensionConfiguration configuration, String expectedUri,
			String... expectedPropertyNameValues) {
		assertEquals("Incorrect URI", expectedUri, configuration.getApplicationPath());
		assertEquals("Incorrect number of properties", (expectedPropertyNameValues.length / 2),
				configuration.getPropertyNames().length);
		for (int i = 0; i < expectedPropertyNameValues.length; i += 2) {
			String expectedName = expectedPropertyNameValues[i];
			String expectedValue = expectedPropertyNameValues[i + 1];
			assertEquals("Incorrect property " + expectedName + " [" + i + "]", expectedValue,
					configuration.getProperty(expectedName, null));
		}
	}

}