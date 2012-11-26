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
package net.officefloor.compile.impl.properties;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.spi.source.SourceProperties;

/**
 * Utility methods for working with {@link Property} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertiesUtil {

	/**
	 * Copies the specified {@link Property} instances from the
	 * {@link SourceProperties} to the {@link PropertyConfigurable}.
	 * 
	 * @param source
	 *            {@link SourceProperties}.
	 * @param target
	 *            {@link PropertyConfigurable}.
	 * @param propertyNames
	 *            Names of the properties to copy. If no names are provided, all
	 *            {@link Property} instances are copied.
	 */
	public static void copyProperties(SourceProperties source,
			PropertyConfigurable target, String... propertyNames) {

		// Determine if copy all properties
		if ((propertyNames == null) || (propertyNames.length == 0)) {
			// None specified, so copy all properties
			propertyNames = source.getPropertyNames();
		}

		// Copy the properties
		for (String propertyName : propertyNames) {

			// Obtain the property value
			String propertyValue = source.getProperty(propertyName, null);

			// Only copy if have a value
			if (propertyValue != null) {
				target.addProperty(propertyName, propertyValue);
			}
		}
	}

	/**
	 * <p>
	 * Copies all {@link Property} instances with the name prefix from the
	 * {@link SourceProperties} to the {@link PropertyConfigurable}.
	 * <p>
	 * This is useful for copying a list of properties that are identified by a
	 * prefix on the {@link Property} name.
	 * 
	 * @param source
	 *            {@link SourceProperties}.
	 * @param propertyPrefix
	 *            Name prefix to identify the {@link Property} instances to
	 *            copy.
	 * @param target
	 *            {@link PropertyConfigurable}.
	 */
	public static void copyPrefixedProperties(SourceProperties source,
			String propertyPrefix, PropertyConfigurable target) {

		// Obtain the property names
		String[] propertyNames = source.getPropertyNames();

		// Copy the properties with the prefix on the name
		for (String propertyName : propertyNames) {
			if (propertyName.startsWith(propertyPrefix)) {
				// Copy the property
				String propertyValue = source.getProperty(propertyName);
				target.addProperty(propertyName, propertyValue);
			}
		}
	}

	/**
	 * All access via static methods.
	 */
	private PropertiesUtil() {
	}

}