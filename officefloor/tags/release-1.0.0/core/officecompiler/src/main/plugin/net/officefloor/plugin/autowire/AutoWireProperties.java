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
package net.officefloor.plugin.autowire;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;

/**
 * Abstract functionality for {@link PropertyList} for auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AutoWireProperties {

	/**
	 * {@link System} property to indicating the location of the
	 * {@link Properties} files.
	 */
	public static final String ENVIRONMENT_PROPERTIES_DIRECTORY = "environment.properties.directory";

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList properties;

	/**
	 * Initiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param properties
	 *            {@link PropertyList}.
	 */
	public AutoWireProperties(OfficeFloorCompiler compiler,
			PropertyList properties) {
		this.compiler = compiler;
		this.properties = properties;
	}

	/**
	 * Obtains the {@link PropertyList}.
	 * 
	 * @return {@link PropertyList}.
	 */
	public PropertyList getProperties() {
		return this.properties;
	}

	/**
	 * Convenience method to add a {@link Property}.
	 * 
	 * @param name
	 *            {@link Property} name.
	 * @param value
	 *            {@link Property} value.
	 */
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	/**
	 * <p>
	 * Convenience method to add {@link Property} instances from a properties
	 * file.
	 * <p>
	 * The location of the properties file is determined based on the system
	 * property {@link #ENVIRONMENT_PROPERTIES_DIRECTORY}:
	 * <ol>
	 * <li>Not specified then the properties file is found on the class path.</li>
	 * <li>If specified, then the properties file is found within the directory
	 * specified by the system property. This allows specifying different
	 * properties files for different environments.</li>
	 * </ol>
	 * 
	 * @param propertiesFilePath
	 *            Path to the properties file.
	 * @throws IOException
	 *             If fails to load the properties.
	 */
	public void loadProperties(String propertiesFilePath) throws IOException {

		// Obtain the environment directory
		String environmentDirectory = System
				.getProperty(ENVIRONMENT_PROPERTIES_DIRECTORY);

		// Obtain the properties file
		InputStream inputStream;
		if ((environmentDirectory == null)
				|| (environmentDirectory.trim().length() == 0)) {
			// Environment directory not specified so load from class path
			inputStream = this.compiler.getClassLoader().getResourceAsStream(
					propertiesFilePath);
		} else {
			// Load properties from environment directory
			inputStream = new FileInputStream(new File(environmentDirectory,
					propertiesFilePath));
		}

		// Ensure have input stream to properties
		if (inputStream == null) {
			throw new FileNotFoundException("Can not find properties file '"
					+ propertiesFilePath + "'");
		}

		// Load the properties
		Properties loader = new Properties();
		loader.load(inputStream);
		inputStream.close();

		// Add the properties
		for (String name : loader.stringPropertyNames()) {
			this.properties.addProperty(name)
					.setValue(loader.getProperty(name));
		}
	}

}