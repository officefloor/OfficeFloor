/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.source;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.source.UnknownPropertyError;

/**
 * {@link SourceProperties} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SourcePropertiesImpl implements SourceProperties {

	/**
	 * {@link PropertyStruct} instances.
	 */
	private final List<PropertyStruct> properties = new LinkedList<PropertyStruct>();

	/**
	 * Initiate empty property list.
	 */
	public SourcePropertiesImpl() {
	}

	/**
	 * Initiate to contain the input {@link SourceProperties}.
	 * 
	 * @param sourceProperties
	 *            {@link SourceProperties}.
	 */
	public SourcePropertiesImpl(SourceProperties sourceProperties) {

		// Only load if provided properties
		if (sourceProperties == null) {
			return; // no properties
		}

		// Load the properties
		for (String name : sourceProperties.getPropertyNames()) {
			// Allow value to be not available
			String value = sourceProperties.getProperty(name, null);
			this.properties.add(new PropertyStruct(name, value));
		}
	}

	/**
	 * Convenience constructor for use in unit testing to instantiate a ready to
	 * use instance.
	 * 
	 * @param propertyNameValuePairs
	 *            Property name/value pairs.
	 */
	public SourcePropertiesImpl(String... propertyNameValuePairs) {
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			this.properties.add(new PropertyStruct(name, value));
		}
	}

	/**
	 * Adds a property.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param value
	 *            Value for the property.
	 */
	public void addProperty(String name, String value) {
		this.properties.add(new PropertyStruct(name, value));
	}

	/*
	 * ====================== SourceProperties ========================
	 */

	@Override
	public String[] getPropertyNames() {

		// Create the listing of property names
		List<String> names = new ArrayList<String>(this.properties.size());
		for (PropertyStruct property : this.properties) {
			names.add(property.name);
		}

		// Return the properties
		return names.toArray(new String[names.size()]);
	}

	@Override
	public String getProperty(String name) throws UnknownPropertyError {

		// Obtain the property value
		String value = this.getProperty(name, null);

		// Ensure have property
		if (value == null) {
			throw new UnknownPropertyError(name);
		}

		// Return the property value
		return value;
	}

	@Override
	public String getProperty(String name, String defaultValue) {
		// Find the property
		for (PropertyStruct property : this.properties) {
			if (property.name.equals(name)) {
				return property.value; // found property
			}
		}

		// Not found, so return default
		return defaultValue;
	}

	@Override
	public Properties getProperties() {
		// Load up the properties
		Properties props = new Properties();
		for (PropertyStruct property : this.properties) {
			props.setProperty(property.name, (property.value == null ? "" : property.value));
		}

		// Return the properties
		return props;
	}

	/**
	 * Property.
	 */
	private static class PropertyStruct {

		/**
		 * Name of the property.
		 */
		public final String name;

		/**
		 * Value for the property.
		 */
		public final String value;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name of the property.
		 * @param value
		 *            Value for the property.
		 */
		public PropertyStruct(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}

}
