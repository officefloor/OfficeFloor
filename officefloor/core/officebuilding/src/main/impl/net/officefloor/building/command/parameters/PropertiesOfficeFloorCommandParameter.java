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
package net.officefloor.building.command.parameters;

import java.util.Properties;

import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} for a property.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertiesOfficeFloorCommandParameter extends
		AbstractMultiplePathsOfficeFloorCommandParameter {

	/**
	 * Parameter name for a property.
	 */
	public static final String PARAMETER_PROPERTY = "property";

	/**
	 * Initiate.
	 */
	public PropertiesOfficeFloorCommandParameter() {
		super(PARAMETER_PROPERTY, null, ",",
				"Property for the OfficeFloor in the form of name=value");
	}

	/**
	 * Obtain the {@link Properties}.
	 * 
	 * @return {@link Properties}.
	 * @throws IllegalArgumentException
	 *             If property entry not in the form name=value.
	 */
	public Properties getProperties() throws IllegalArgumentException {

		// Obtain the property entries
		String[] entries = this.getPaths();

		// Create and load the properties
		Properties properties = new Properties();
		for (String entry : entries) {

			// Obtain the property name and value
			int splitIndex = entry.indexOf('=');
			if (splitIndex < 1) {
				// Must have at least one character to name
				throw new IllegalArgumentException(
						"Invalid property argument format " + entry);
			}
			String name = entry.substring(0, splitIndex);
			String value = entry.substring(splitIndex + "=".length());

			// Add the property
			properties.setProperty(name, value);
		}

		// Return the properties
		return properties;
	}

}