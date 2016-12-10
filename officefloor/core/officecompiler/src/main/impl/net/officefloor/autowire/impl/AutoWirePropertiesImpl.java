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
package net.officefloor.autowire.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.officefloor.autowire.AutoWireProperties;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;

/**
 * Abstract functionality for {@link PropertyList} for auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AutoWirePropertiesImpl implements AutoWireProperties {

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
	 * Optional {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

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
	public AutoWirePropertiesImpl(OfficeFloorCompiler compiler,
			PropertyList properties) {
		this.compiler = compiler;
		this.classLoader = null;
		this.properties = properties;
	}

	/**
	 * Initiate to use {@link ClassLoader}.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param properties
	 *            {@link PropertyList}.
	 */
	public AutoWirePropertiesImpl(ClassLoader classLoader,
			PropertyList properties) {
		this.compiler = null;
		this.classLoader = classLoader;
		this.properties = properties;
	}

	/**
	 * Obtains the {@link ClassLoader}.
	 * 
	 * @return {@link ClassLoader}.
	 */
	private ClassLoader getClassLoader() {
		return (this.classLoader != null ? this.classLoader : this.compiler
				.getClassLoader());
	}

	/*
	 * ======================= AutoWireProperties =========================
	 */

	@Override
	public PropertyList getProperties() {
		return this.properties;
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public void loadProperties(String propertiesFilePath) throws IOException {

		// Obtain the environment directory
		String environmentDirectory = System
				.getProperty(ENVIRONMENT_PROPERTIES_DIRECTORY);

		// Obtain the properties file
		InputStream inputStream;
		if ((environmentDirectory == null)
				|| (environmentDirectory.trim().length() == 0)) {
			// Environment directory not specified so load from class path
			inputStream = this.getClassLoader().getResourceAsStream(
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
			this.addProperty(name, loader.getProperty(name));
		}
	}

}