/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof.model.woof;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.junit.Assert;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.change.Change;
import net.officefloor.woof.template.WoofTemplateExtensionChangeContext;
import net.officefloor.woof.template.WoofTemplateExtensionConfiguration;
import net.officefloor.woof.template.WoofTemplateExtensionSource;

/**
 * Mock {@link WoofTemplateExtensionSource} that allows a {@link Change}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockChangeWoofTemplateExtensionSource extends MockNoChangeWoofTemplateExtensionSource {

	/**
	 * {@link Change}.
	 */
	private static Change<?> change = null;

	/**
	 * Expected old application path.
	 */
	private static String oldApplicationPath = null;

	/**
	 * Expected old property name value pairs.
	 */
	private static String[] oldPropertyNameValuePairs = null;

	/**
	 * Expected new application path.
	 */
	private static String newApplicationPath = null;

	/**
	 * Expected old property name value pairs.
	 */
	private static String[] newPropertyNameValuePairs = null;

	/**
	 * {@link WoofTemplateChangeContext}.
	 */
	private static WoofTemplateChangeContext changeContext = null;

	/**
	 * Resets for next test.
	 * 
	 * @param change
	 *            {@link Change}.
	 * @param oldApplicationPath
	 *            Expected old URL.
	 * @param oldPropertyNameValuePairs
	 *            Expected old {@link Property} values.
	 * @param newApplicationPath
	 *            Expected new URL.
	 * @param newPropertyNameValuePairs
	 *            Expected new {@link Property} values.
	 * @param changeContext
	 *            {@link WoofTemplateChangeContext}.
	 */
	public static void reset(Change<?> change, String oldApplicationPath, String[] oldPropertyNameValuePairs,
			String newApplicationPath, String[] newPropertyNameValuePairs, WoofTemplateChangeContext changeContext) {
		MockChangeWoofTemplateExtensionSource.change = change;
		MockChangeWoofTemplateExtensionSource.oldApplicationPath = oldApplicationPath;
		MockChangeWoofTemplateExtensionSource.oldPropertyNameValuePairs = oldPropertyNameValuePairs;
		MockChangeWoofTemplateExtensionSource.newApplicationPath = newApplicationPath;
		MockChangeWoofTemplateExtensionSource.newPropertyNameValuePairs = newPropertyNameValuePairs;
		MockChangeWoofTemplateExtensionSource.changeContext = changeContext;
	}

	/**
	 * Records asserting the {@link Change}.
	 * 
	 * @param change
	 *            Mock {@link Change}.
	 */
	public static void recordAssertChange(Change<?> change, OfficeFrameTestCase testCase) {
		testCase.recordReturn(change, change.getConflicts(), null);
		change.apply();
		change.revert();
		change.apply();
		change.revert();
	}

	/**
	 * Ensure the {@link WoofTemplateExtensionConfiguration} is as expected.
	 * 
	 * @param configuration
	 *            {@link WoofTemplateExtensionConfiguration} to test.
	 * @param expectedApplicationPath
	 *            Expected URL.
	 * @param expectedPropertyNameValuePairs
	 *            Expected property name value pairs.
	 */
	private static void assertConfiguration(String configurationType, WoofTemplateExtensionConfiguration configuration,
			String expectedApplicationPath, String... expectedPropertyNameValuePairs) {

		// Determine if expecting configuration
		if (expectedApplicationPath == null) {
			Assert.assertNull("Should be no " + configurationType + " configuration", configuration);
			return; // correctly no configuration
		}

		// Ensure have configuration to validate
		Assert.assertNotNull("Should have " + configurationType + " configuration", configuration);

		// Validate the URL and properties
		Assert.assertEquals("Incorrect " + configurationType + " application path", expectedApplicationPath,
				configuration.getApplicationPath());
		Assert.assertEquals("Incorrect number of " + configurationType + " properties",
				(expectedPropertyNameValuePairs.length / 2), configuration.getPropertyNames().length);
		for (int i = 0; i < expectedPropertyNameValuePairs.length; i += 2) {
			String expectedName = expectedPropertyNameValuePairs[i];
			String expectedValue = expectedPropertyNameValuePairs[i + 1];
			Assert.assertEquals(
					"Incorrect value for " + configurationType + " property " + expectedName + " [" + i + "]",
					expectedValue, configuration.getProperty(expectedName, null));
		}
	}

	/*
	 * ================== WoofTemplateExtensionSource ===============
	 */

	@Override
	public Change<?> createConfigurationChange(WoofTemplateExtensionChangeContext context) {

		// Ensure able to obtain resource
		try {
			InputStream resource = context.getResource(this.getClass().getPackage().getName().replace('.', '/')
					+ "/MockChangeWoofTemplateExtensionSource.resource");
			Assert.assertNotNull("Should have resource", resource);
			StringWriter resourceContents = new StringWriter();
			Reader resourceReader = new InputStreamReader(resource);
			for (int character = resourceReader.read(); character != -1; character = resourceReader.read()) {
				resourceContents.write(character);
			}
			Assert.assertEquals("Incorrect resource content", "RESOURCE", resourceContents.toString());
		} catch (IOException ex) {
			Assert.fail("Should be able to read resource: " + ex.getMessage());
		}

		// Validate the old configuration
		assertConfiguration("old", context.getOldConfiguration(), oldApplicationPath, oldPropertyNameValuePairs);

		// Validate the new configuration
		assertConfiguration("new", context.getNewConfiguration(), newApplicationPath, newPropertyNameValuePairs);

		// Validate the configuration context
		Assert.assertSame("Incorrect configuration context", changeContext.getConfigurationContext(),
				context.getConfigurationContext());

		// Return the change
		return change;
	}

}
