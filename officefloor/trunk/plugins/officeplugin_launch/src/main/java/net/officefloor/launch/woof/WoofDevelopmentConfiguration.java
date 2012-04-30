/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Configuration for the {@link WoofDevelopmentLauncher}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofDevelopmentConfiguration {

	/**
	 * Prefix of the property name to provide the GWT Module Names.
	 */
	private static final String PROPERTY_GWT_MODULE_NAME_PREFIX = "gwt.module.name";

	/**
	 * Prefix of the property name to provide the Startup URL.
	 */
	private static final String PROPERTY_STARTUP_URL_PREFIX = "startup.url";

	/**
	 * Listing of the startup URLs.
	 */
	private final List<String> startupUrls = new LinkedList<String>();

	/**
	 * Listing of the GWT modules.
	 */
	private final List<String> moduleNames = new LinkedList<String>();

	/**
	 * Initiate with no configuration.
	 */
	public WoofDevelopmentConfiguration() {
	}

	/**
	 * Initiate from configuration {@link File}.
	 * 
	 * @param configurationFile
	 *            {@link File} containing {@link WoofDevelopmentConfiguration}.
	 * @throws IOException
	 *             If fails to load the configuration.
	 */
	public WoofDevelopmentConfiguration(File configurationFile)
			throws IOException {

		// Load the properties from configuration file
		Properties properties = new Properties();
		FileInputStream inputStream = new FileInputStream(configurationFile);
		properties.load(inputStream);
		inputStream.close();

		// Load the GWT module names
		this.loadValues(PROPERTY_GWT_MODULE_NAME_PREFIX, properties,
				this.moduleNames);

		// Load the Startup URLs
		this.loadValues(PROPERTY_STARTUP_URL_PREFIX, properties,
				this.startupUrls);
	}

	/**
	 * Obtains the startup URLs.
	 * 
	 * @return Startup URLs.
	 */
	public String[] getStartupUrls() {
		return this.startupUrls.toArray(new String[this.startupUrls.size()]);
	}

	/**
	 * Adds the startup URL.
	 * 
	 * @param startupUrl
	 *            Startup URL.
	 */
	public void addStartupUrl(String startupUrl) {

		// Only include if non-registered startup URL
		if (this.startupUrls.contains(startupUrl)) {
			return; // already included
		}
		this.startupUrls.add(startupUrl);
	}

	/**
	 * Adds the GWT module name.
	 * 
	 * @param gwtModuleName
	 *            GWT module name.
	 */
	public void addGwtModuleName(String gwtModuleName) {

		// Only include if non-registered GWT module name
		if (this.moduleNames.contains(gwtModuleName)) {
			return; // already included
		}
		this.moduleNames.add(gwtModuleName);
	}

	/**
	 * Obtains the GWT module names.
	 * 
	 * @return GWT module names.
	 */
	public String[] getModuleNames() {
		return this.moduleNames.toArray(new String[this.moduleNames.size()]);
	}

	/**
	 * Stores the {@link WoofDevelopmentConfiguration}.
	 * 
	 * @param configurationFile
	 *            {@link File} to contain the persisted
	 *            {@link WoofDevelopmentConfiguration}.
	 * @throws IOException
	 *             If fails to store configuration.
	 */
	public void storeConfiguration(File configurationFile) throws IOException {

		// Create the properties
		Properties properties = new Properties();

		// Load the GWT module names
		this.storeValues(PROPERTY_GWT_MODULE_NAME_PREFIX, this.moduleNames,
				properties);

		// Load the Startup URLs
		this.storeValues(PROPERTY_STARTUP_URL_PREFIX, this.startupUrls,
				properties);

		// Write properties to configuration file
		FileOutputStream outputStream = new FileOutputStream(configurationFile);
		properties.store(outputStream, this.getClass().getSimpleName());
		outputStream.close();
	}

	/**
	 * Loads the values.
	 * 
	 * @param propertyNamePrefix
	 *            Prefix of the property name.
	 * @param properties
	 *            {@link Properties}.
	 * @param values
	 *            Values to be loaded.
	 */
	private void loadValues(String propertyNamePrefix, Properties properties,
			List<String> values) {

		// Keep loading values until no property
		String name = propertyNamePrefix;
		String value = null;
		int index = 0;
		do {

			// Obtain the value
			value = properties.getProperty(name);

			// Load value if available
			if (value != null) {
				values.add(value);
			}

			// Setup for next iteration
			index++;
			name = propertyNamePrefix + "." + index;

		} while (value != null);
	}

	/**
	 * Stores the values.
	 * 
	 * @param propertyNamePrefix
	 *            Prefix of the property name.
	 * @param values
	 *            Values.
	 * @param properties
	 *            {@link Properties}.
	 */
	private void storeValues(String propertyNamePrefix, List<String> values,
			Properties properties) {

		// Load each property value
		for (int i = 0; i < values.size(); i++) {

			// Obtain the property name
			String name = propertyNamePrefix;
			if (i > 0) {
				name = name + "." + i;
			}

			// Obtain the property value
			String value = values.get(i);

			// Load the property
			properties.setProperty(name, value);
		}
	}

}