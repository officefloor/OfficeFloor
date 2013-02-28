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
package net.officefloor.plugin.servlet.filter.configuration;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.filter.configuration.FilterInstance;
import net.officefloor.plugin.servlet.filter.configuration.FilterMapping;
import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * Abstract functionality for configuration testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConfigurationTestCase extends OfficeFrameTestCase {

	/**
	 * Creates the {@link PropertyList}.
	 * 
	 * @param propertyNameValues
	 *            Property name value pairs to be loaded into the
	 *            {@link PropertyList}.
	 * @return New {@link PropertyList}.
	 */
	protected static PropertyList createPropertyList(
			String... propertyNameValues) {
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			properties.addProperty(name).setValue(value);
		}
		return properties;
	}

	/**
	 * Asserts the {@link FilterInstance}.
	 * 
	 * @param instance
	 *            {@link FilterInstance}.
	 * @param name
	 *            Expected name of the {@link Filter}.
	 * @param className
	 *            Expected class name of the {@link Filter}.
	 * @param initParameterNameValues
	 *            Expected init parameter name values.
	 */
	protected static void assertFilterInstance(FilterInstance instance,
			String name, String className, String... initParameterNameValues) {
		assertEquals("Incorrect name", name, instance.getName());
		assertEquals("Incorrect class name", className, instance.getClassName());
		assertEquals("Incorrect number of init parameters",
				(initParameterNameValues.length / 2), instance
						.getInitParameters().size());
		for (int i = 0; i < initParameterNameValues.length; i += 2) {
			String initName = initParameterNameValues[i];
			String initValue = initParameterNameValues[i + 1];
			assertEquals("Incorrect init parameter '" + initName + "'",
					initValue, instance.getInitParameters().get(initName));
		}
	}

	/**
	 * Asserts the {@link FilterMapping}.
	 * 
	 * @param mapping
	 *            {@link FilterMapping}.
	 * @param filterName
	 *            Expected {@link Filter} name.
	 * @param urlPattern
	 *            Expected URL pattern.
	 * @param servletName
	 *            Expected {@link Servlet} name.
	 * @param mappingTypes
	 *            Expected {@link MappingType} instances.
	 */
	protected static void assertFilterMapping(FilterMapping mapping,
			String filterName, String urlPattern, String servletName,
			MappingType... mappingTypes) {
		assertEquals("Incorrect filter name", filterName, mapping
				.getFilterName());
		assertEquals("Incorrect url pattern", urlPattern, mapping
				.getUrlPattern());
		assertEquals("Incorrect servlet name", servletName, mapping
				.getServletName());
		MappingType[] actualMappingTypes = mapping.getMappingTypes();
		assertEquals("Incorrect number of mapping types", mappingTypes.length,
				(actualMappingTypes == null ? 0 : actualMappingTypes.length));
		for (int i = 0; i < mappingTypes.length; i++) {
			assertEquals("Incorrect mapping type at index " + i,
					mappingTypes[i], actualMappingTypes[i]);
		}
	}

	/**
	 * Asserts the {@link PropertyList} properties.
	 * 
	 * @param actual
	 *            {@link PropertyList} to be validated.
	 * @param expectedNameValues
	 *            Expected property name values.
	 */
	protected static void assertPropertyList(PropertyList actual,
			String... expectedNameValues) {

		// Ensure correct number of properties
		String[] names = actual.getPropertyNames();
		assertEquals("Incorrect number of properties", names.length,
				(expectedNameValues.length / 2));

		// Ensure properties are correct
		for (int i = 0; i < expectedNameValues.length; i += 2) {
			String name = expectedNameValues[i];
			String value = expectedNameValues[i + 1];
			assertEquals("Incorrect property '" + name + "'", value, actual
					.getPropertyValue(name, null));
		}
	}

}