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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;

/**
 * Configuration for the {@link WoofDevelopmentLauncher}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofDevelopmentConfiguration {

	/**
	 * Property name to provide the WAR directory.
	 */
	private static final String PROPERTY_WAR_DIRECTORY = "war.directory";

	/**
	 * Property name to provide the webapp directory.
	 */
	private static final String PROPERTY_WEB_APP_DIRECTORY = "webapp.directory";

	/**
	 * Prefix of the property name to provide the resource directories.
	 */
	private static final String PROPERTY_RESOURCE_DIRECTORY_PREFIX = "resource.directory";

	/**
	 * Prefix of the property name to provide the GWT Module Names.
	 */
	private static final String PROPERTY_GWT_MODULE_NAME_PREFIX = "gwt.module.name";

	/**
	 * Prefix of the property name to provide the Startup URL.
	 */
	private static final String PROPERTY_STARTUP_URL_PREFIX = "startup.url";

	/**
	 * Prefix of the property name for the {@link OfficeFloorCompiler}
	 * properties.
	 */
	private static final String PROPERTY_PROPERTIES_PREFIX = "properties";

	/**
	 * WAR directory for deployed GWT files.
	 */
	private File warDirectory = null;

	/**
	 * Web App directory for web resources.
	 */
	private File webAppDirectory = null;

	/**
	 * Resource directories.
	 */
	private final List<File> resourceDirectories = new LinkedList<File>();

	/**
	 * Listing of the startup URLs.
	 */
	private final List<String> startupUrls = new LinkedList<String>();

	/**
	 * Listing of the GWT modules.
	 */
	private final List<String> moduleNames = new LinkedList<String>();

	/**
	 * Listing of property name/values.
	 */
	private final List<String> propertyNameValues = new LinkedList<String>();

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

		// Obtain the WAR directory
		String warDirectoryPath = properties
				.getProperty(PROPERTY_WAR_DIRECTORY);
		if (warDirectoryPath != null) {
			this.warDirectory = new File(warDirectoryPath);
		}

		// Obtain the web app directory
		String webAppDirectoryPath = properties
				.getProperty(PROPERTY_WEB_APP_DIRECTORY);
		if (webAppDirectoryPath != null) {
			this.webAppDirectory = new File(webAppDirectoryPath);
		}

		// Load the GWT module names
		this.loadValues(PROPERTY_GWT_MODULE_NAME_PREFIX, properties,
				this.moduleNames);

		// Load the Startup URLs
		this.loadValues(PROPERTY_STARTUP_URL_PREFIX, properties,
				this.startupUrls);

		// Load the properties
		this.loadValues(PROPERTY_PROPERTIES_PREFIX, properties,
				this.propertyNameValues);

		// Load the resource directories
		List<String> resourceDirectoryPaths = new LinkedList<String>();
		this.loadValues(PROPERTY_RESOURCE_DIRECTORY_PREFIX, properties,
				resourceDirectoryPaths);
		for (String resourceDirectoryPath : resourceDirectoryPaths) {
			this.resourceDirectories.add(new File(resourceDirectoryPath));
		}
	}

	/**
	 * Specifies the WAR directory for deployed GWT files.
	 * 
	 * @param warDirectory
	 *            WAR directory.
	 */
	public void setWarDirectory(File warDirectory) {
		this.warDirectory = warDirectory;
	}

	/**
	 * Obtains the WAR directory for deployed GWT files.
	 * 
	 * @return WAR directory.
	 */
	public File getWarDirectory() {
		return this.warDirectory;
	}

	/**
	 * Specifies the web app directory for web resources.
	 * 
	 * @param webAppDirectory
	 *            Web App directory.
	 */
	public void setWebAppDirectory(File webAppDirectory) {
		this.webAppDirectory = webAppDirectory;
	}

	/**
	 * Obtains the web app directory for web resources.
	 * 
	 * @return Web app directory.
	 */
	public File getWebAppDirectory() {
		return this.webAppDirectory;
	}

	/**
	 * Obtains the resource directories.
	 * 
	 * @return Resource directories.
	 */
	public File[] getResourceDirectories() {
		return this.resourceDirectories
				.toArray(new File[this.resourceDirectories.size()]);
	}

	/**
	 * Adds a resource directory.
	 * 
	 * @param resourceDirectory
	 *            Resource directory.
	 */
	public void addResourceDirectory(File resourceDirectory) {
		this.resourceDirectories.add(resourceDirectory);
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
	 * Adds a property.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public void addProperty(String name, String value) {
		this.propertyNameValues.add(name);
		this.propertyNameValues.add(value);
	}

	/**
	 * Adds property name/values from the arguments.
	 * 
	 * @param arguments
	 *            Arguments.
	 */
	public void addPropertyArguments(String... arguments) {
		for (int i = 0; i < arguments.length; i += 2) {

			// Obtain the name
			String name = arguments[i];

			// Obtain the value (must have value to keep name/value pairs)
			int valueIndex = i + 1;
			String value = (valueIndex >= arguments.length ? ""
					: arguments[valueIndex]);

			// Add the property
			this.addProperty(name, value);
		}
	}

	/**
	 * Obtains the properties.
	 * 
	 * @return Properties.
	 */
	public PropertyList getProperties() {

		// Load the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		String[] nameValues = this.propertyNameValues
				.toArray(new String[this.propertyNameValues.size()]);
		for (int i = 0; i < nameValues.length; i += 2) {

			// Obtain the name
			String name = nameValues[i];

			// Obtain the value (may be deleted from properties file)
			int valueIndex = i + 1;
			String value = (valueIndex >= nameValues.length ? ""
					: nameValues[valueIndex]);

			// Add property
			properties.addProperty(name).setValue(value);
		}

		// Return the properties
		return properties;
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

		// Load the WAR directory
		if (this.warDirectory != null) {
			properties.setProperty(PROPERTY_WAR_DIRECTORY,
					this.warDirectory.getAbsolutePath());
		}

		// Load the web app directory
		if (this.webAppDirectory != null) {
			properties.setProperty(PROPERTY_WEB_APP_DIRECTORY,
					this.webAppDirectory.getAbsolutePath());
		}

		// Load the GWT module names
		this.storeValues(PROPERTY_GWT_MODULE_NAME_PREFIX, this.moduleNames,
				properties);

		// Load the Startup URLs
		this.storeValues(PROPERTY_STARTUP_URL_PREFIX, this.startupUrls,
				properties);

		// Load the properties
		this.storeValues(PROPERTY_PROPERTIES_PREFIX, this.propertyNameValues,
				properties);

		// Load the resource directory paths
		List<String> resourceDirectoryPaths = new ArrayList<String>(
				this.resourceDirectories.size());
		for (File resourceDirectory : this.resourceDirectories) {
			resourceDirectoryPaths.add(resourceDirectory.getAbsolutePath());
		}
		this.storeValues(PROPERTY_RESOURCE_DIRECTORY_PREFIX,
				resourceDirectoryPaths, properties);

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