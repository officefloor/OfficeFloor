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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.properties.PropertyList;

/**
 * Implementation of the {@link PropertyList}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyListImpl implements PropertyList {

	/**
	 * List of {@link Property} instances.
	 */
	private final List<Property> properties = new LinkedList<Property>();

	/**
	 * Initiate.
	 * 
	 * @param nameValuePairs
	 *            {@link Property} name/values to initially populate this list.
	 */
	public PropertyListImpl(String... nameValuePairs) {
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String name = nameValuePairs[i];
			String value = nameValuePairs[i + 1];
			this.addProperty(name).setValue(value);
		}
	}

	/*
	 * ======================= Iterable ======================================
	 */

	@Override
	public Iterator<Property> iterator() {
		return this.properties.iterator();
	}

	/*
	 * ================== PropertyList ======================================
	 */

	@Override
	public Property addProperty(String name, String label) {
		Property property = new PropertyImpl(name, label);
		this.properties.add(property);
		return property;
	}

	@Override
	public Property addProperty(String name) {
		return this.addProperty(name, name);
	}

	@Override
	public void removeProperty(Property property) {
		this.properties.remove(property);
	}

	@Override
	public String[] getPropertyNames() {
		// Create the listing of property names
		String[] names = new String[this.properties.size()];
		int nameIndex = 0;
		for (Property property : this.properties) {
			names[nameIndex++] = property.getName();
		}
		return names;
	}

	@Override
	public Property getProperty(String name) {

		// Ensure have property name
		if (name == null) {
			return null; // no property by null name
		}

		// Find the first matching property
		for (Property property : this.properties) {
			if (name.equals(property.getName())) {
				return property; // found property
			}
		}

		// No matching property if at this point
		return null;
	}

	@Override
	public Property getOrAddProperty(String name) {

		// Attempt to get the property
		Property property = this.getProperty(name);
		if (property == null) {
			// No property found, so add
			property = this.addProperty(name);
		}

		// Return the property
		return property;
	}

	@Override
	public String getPropertyValue(String name, String defaultValue) {

		// Attempt to get the property
		Property property = this.getProperty(name);
		String value = (property == null ? null : property.getValue());

		// Return value or default if no value
		return (value != null ? value : defaultValue);
	}

	@Override
	public Properties getProperties() {
		// Create the properties.
		// This is done in reverse order to ensure first properties override.
		Properties utilProperties = new Properties();
		for (int i = this.properties.size() - 1; i >= 0; i--) {
			Property property = this.properties.get(i);

			// Ensure have name and value
			String name = property.getName();
			if (name == null) {
				continue; // ignore the property
			}
			String value = property.getValue();
			value = (value == null ? "" : value); // default empty string

			// Add the property
			utilProperties.setProperty(name, value);
		}
		return utilProperties;
	}

	@Override
	public void clear() {
		this.properties.clear();
	}

	@Override
	public void sort(Comparator<? super Property> comparator) {
		Collections.sort(this.properties, comparator);
	}

	@Override
	public void normalise() {
		// Iterate over the properties removing duplicates and null values
		Set<String> propertyNames = new HashSet<String>();
		for (Property property : new ArrayList<Property>(this.properties)) {

			// Remove if blank name
			String propertyName = property.getName();
			if (CompileUtil.isBlank(propertyName)) {
				this.properties.remove(property);
				continue;
			}

			// Remove if blank value
			if (CompileUtil.isBlank(property.getValue())) {
				this.properties.remove(property);
				continue;
			}

			// Property has value, determine if duplicate
			if (propertyNames.contains(propertyName)) {
				this.properties.remove(property);
				continue;
			}

			// Property to stay in list
			propertyNames.add(propertyName);
		}
	}

	@Override
	public void configureProperties(PropertyConfigurable configurable) {
		for (Property property : this.properties) {
			configurable.addProperty(property.getName(), property.getValue());
		}
	}

}
