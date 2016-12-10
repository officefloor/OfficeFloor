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
package net.officefloor.autowire;

import java.io.IOException;
import java.util.Properties;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.properties.PropertyList;

/**
 * Functionality for {@link PropertyList} for auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireProperties extends PropertyConfigurable {

	/**
	 * {@link System} property to indicating the location of the
	 * {@link Properties} files.
	 */
	String ENVIRONMENT_PROPERTIES_DIRECTORY = "environment.properties.directory";

	/**
	 * Obtains the {@link PropertyList}.
	 * 
	 * @return {@link PropertyList}.
	 */
	PropertyList getProperties();

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
	void loadProperties(String propertiesFilePath) throws IOException;

}