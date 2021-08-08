/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.properties;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.source.SourceProperties;

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
	 * @param source        {@link SourceProperties}.
	 * @param target        {@link PropertyConfigurable}.
	 * @param propertyNames Names of the properties to copy. If no names are
	 *                      provided, all {@link Property} instances are copied.
	 */
	public static void copyProperties(SourceProperties source, PropertyConfigurable target, String... propertyNames) {

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
	 * Copies the specified {@link Property} instances from the
	 * {@link SourceProperties} to the {@link PropertyList}.
	 * 
	 * @param source        {@link SourceProperties}.
	 * @param target        {@link PropertyList}.
	 * @param propertyNames Names of the properties to copy. If no names are
	 *                      provided, all {@link Property} instances are copied.
	 */
	public static void copyProperties(SourceProperties source, PropertyList target, String... propertyNames) {
		copyProperties(source, (name, value) -> target.addProperty(name).setValue(value), propertyNames);
	}

	/**
	 * <p>
	 * Copies all {@link Property} instances with the name prefix from the
	 * {@link SourceProperties} to the {@link PropertyConfigurable}.
	 * <p>
	 * This is useful for copying a list of properties that are identified by a
	 * prefix on the {@link Property} name.
	 * 
	 * @param source         {@link SourceProperties}.
	 * @param propertyPrefix Name prefix to identify the {@link Property} instances
	 *                       to copy.
	 * @param target         {@link PropertyConfigurable}.
	 */
	public static void copyPrefixedProperties(SourceProperties source, String propertyPrefix,
			PropertyConfigurable target) {

		// Obtain the property names
		String[] propertyNames = source.getPropertyNames();

		// Copy the properties with the prefix on the name
		for (String propertyName : propertyNames) {
			if (propertyName.startsWith(propertyPrefix)) {
				// Copy the property
				String propertyValue = source.getProperty(propertyName, null);
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
	 * @param source         {@link SourceProperties}.
	 * @param propertyPrefix Name prefix to identify the {@link Property} instances
	 *                       to copy.
	 * @param target         {@link PropertyConfigurable}.
	 */
	public static void copyPrefixedProperties(SourceProperties source, String propertyPrefix, PropertyList target) {
		copyPrefixedProperties(source, propertyPrefix, (name, value) -> target.addProperty(name).setValue(value));
	}

	/**
	 * All access via static methods.
	 */
	private PropertiesUtil() {
	}

}
