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
package net.officefloor.launch.woof;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link WoofDevelopmentConfigurationLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofDevelopmentConfigurationLoaderTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to load configuration.
	 */
	public void testLoadConfiguration() throws Exception {

		// Obtain the configuration file
		File woofFile = this.findFile(this.getClass(), "application.woof");
		WoofDevelopmentConfiguration configuration = WoofDevelopmentConfigurationLoader
				.loadConfiguration(new FileInputStream(woofFile));

		// Add property (not provided from WoOF configuration)
		configuration.addPropertyArguments("name", "value", "no.value");
		configuration.addProperty("one", "two");

		// Validate the configuration
		assertWoofDevelopmentConfiguration(configuration);
	}

	/**
	 * Ensure able to serialise the {@link WoofDevelopmentConfiguration}.
	 */
	public void testSerialiseConfiguration() throws Exception {

		// Obtain the configuration file
		File woofFile = this.findFile(this.getClass(), "application.woof");
		WoofDevelopmentConfiguration configuration = WoofDevelopmentConfigurationLoader
				.loadConfiguration(new FileInputStream(woofFile));

		// Add property (not provided from WoOF configuration)
		configuration.addProperty("name", "value");
		configuration.addProperty("no.value", "");
		configuration.addProperty("one", "two");

		// Add WAR directory
		File warDirectory = woofFile.getParentFile();
		configuration.setWarDirectory(warDirectory);

		// Add webapp directory
		File webAppDirectory = woofFile.getParentFile().getParentFile();
		configuration.setWebAppDirectory(webAppDirectory);

		// Add additional resource directories
		File resourceDirectoryOne = warDirectory;
		configuration.addResourceDirectory(resourceDirectoryOne);
		File resourceDirectoryTwo = webAppDirectory;
		configuration.addResourceDirectory(resourceDirectoryTwo);

		// Serialise the configuration
		File configurationFile = File.createTempFile(this.getName(), ".test");
		configuration.storeConfiguration(configurationFile);

		// Obtain the configuration from store
		WoofDevelopmentConfiguration serialisedConfiguration = new WoofDevelopmentConfiguration(
				configurationFile);

		// Validate the serialised configuration
		assertWoofDevelopmentConfiguration(serialisedConfiguration);

		// Validate the WAR directory
		assertEquals("Incorrect WAR directory",
				warDirectory.getCanonicalPath(), serialisedConfiguration
						.getWarDirectory().getCanonicalPath());

		// Validate the web app directory
		assertEquals("Incorrect web app directory",
				webAppDirectory.getCanonicalPath(), serialisedConfiguration
						.getWebAppDirectory().getCanonicalPath());

		// Validate the resource directories
		File[] resourceDirectories = serialisedConfiguration
				.getResourceDirectories();
		String[] resourceDirectoryPaths = new String[resourceDirectories.length];
		for (int i = 0; i < resourceDirectories.length; i++) {
			resourceDirectoryPaths[i] = resourceDirectories[i]
					.getCanonicalPath();
		}
		assertStringArraySet("Incorrect resource directories",
				resourceDirectoryPaths,
				resourceDirectoryOne.getCanonicalPath(),
				resourceDirectoryTwo.getCanonicalPath());
	}

	/**
	 * Ensures correct configuration.
	 * 
	 * @param configuration
	 *            {@link WoofDevelopmentConfiguration}.
	 */
	private static void assertWoofDevelopmentConfiguration(
			WoofDevelopmentConfiguration configuration) {

		// Validate the start up urls
		String[] startupUrls = configuration.getStartupUrls();
		assertStringArraySet("startup URLs", startupUrls, "/sectionA",
				"/sectionB", "/templateA", "/templateB", "/templateC",
				"/templateD");

		// Validate the module names
		String[] moduleNames = configuration.getModuleNames();
		assertStringArraySet("module names", moduleNames,
				"net.officefloor.launch.woof.templateA",
				"net.officefloor.launch.woof.templateC",
				"net.officefloor.launch.woof.templateD");

		// Validate the WoOF servlet container properties
		PropertyList properties = configuration.getProperties();
		assertEquals("Incorrect number of properties", 3, properties
				.getProperties().size());
		assertEquals("Incorrect property name", "value", properties
				.getProperty("name").getValue());
		assertEquals("Incorrect property no.value", "",
				properties.getProperty("no.value").getValue());
		assertEquals("Incorrect property one", "two",
				properties.getProperty("one").getValue());
	}

	/**
	 * Asserts the contents of the string array to contain similar content (same
	 * elements not worrying about order).
	 * 
	 * @param message
	 *            Message.
	 * @param actual
	 *            Actual entries.
	 * @param expected
	 *            Expected entries.
	 */
	private static void assertStringArraySet(String message, String[] actual,
			String... expected) {

		// Ensure correct number
		assertEquals("Incorrect number of elements: " + message,
				expected.length, actual.length);

		// Sort arrays and compare content
		Arrays.sort(expected);
		Arrays.sort(actual);
		for (int i = 0; i < expected.length; i++) {
			assertEquals("Incorrect element: " + message, expected[0],
					actual[0]);
		}
	}

}