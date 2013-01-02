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

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.plugin.servlet.filter.configuration.FilterMapping;
import net.officefloor.plugin.servlet.filter.configuration.FilterMappings;
import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * Tests the {@link FilterMapping}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterMappingsTest extends AbstractConfigurationTestCase {

	/**
	 * Ensure can output {@link Property} instances for a single
	 * {@link FilterMapping}.
	 */
	public void testOutputSingleFilterMapping() {

		// Create the filter mapping
		FilterMappings mappings = new FilterMappings();
		mappings.addFilterMapping("Filter", "/path/*", "Servlet",
				MappingType.INCLUDE);

		// Output the properties for filter mapping
		PropertyList properties = createPropertyList();
		mappings.outputProperties(properties);

		// Validate the properties
		assertPropertyList(properties, "filter.mapping.index.0", "Filter",
				"filter.mapping.url.0", "/path/*", "filter.mapping.servlet.0",
				"Servlet", "filter.mapping.type.0", "INCLUDE");
	}

	/**
	 * Ensure can output {@link Property} instances to a {@link PropertyList}.
	 */
	public void testOutputProperties() {

		// Create the filter mappings
		FilterMappings mappings = new FilterMappings();
		mappings.addFilterMapping("FilterOne", "/path/*", null);
		mappings.addFilterMapping("FilterTwo", null, "Servlet",
				MappingType.INCLUDE, MappingType.FORWARD, MappingType.REQUEST);

		// Output the properties for filter mapping
		PropertyList properties = createPropertyList();
		mappings.outputProperties(properties);

		// Validate the properties
		assertPropertyList(properties, "filter.mapping.index.0", "FilterOne",
				"filter.mapping.url.0", "/path/*", "filter.mapping.index.1",
				"FilterTwo", "filter.mapping.servlet.1", "Servlet",
				"filter.mapping.type.1", "INCLUDE,FORWARD,REQUEST");
	}

	/**
	 * Ensure can configure a single {@link FilterMapping} from the
	 * {@link PropertyList}.
	 */
	public void testInputSingleFilterMapping() {

		// Create the properties
		PropertyList properties = createPropertyList("filter.mapping.index.0",
				"Filter", "filter.mapping.url.0", "/path/*",
				"filter.mapping.servlet.0", "Servlet", "filter.mapping.type.0",
				"INCLUDE");

		// Configure the filter mappings
		FilterMappings filterMappings = new FilterMappings();
		filterMappings.inputProperties(properties);

		// Validate the mapping
		FilterMapping[] mappings = filterMappings.getFilterMappings();
		assertEquals("Should only be one mapping", 1, mappings.length);
		assertFilterMapping(mappings[0], "Filter", "/path/*", "Servlet",
				MappingType.INCLUDE);

	}

	/**
	 * Ensure can configure the {@link FilterMapping} instances from the
	 * {@link PropertyList}.
	 */
	public void testInputProperties() {

		// Create the properties
		PropertyList properties = createPropertyList("filter.mapping.index.0",
				"FilterOne", "filter.mapping.url.0", "/path/*",
				"filter.mapping.index.1", "FilterTwo",
				"filter.mapping.servlet.1", "Servlet", "filter.mapping.type.1",
				"INCLUDE,FORWARD,REQUEST");

		// Configure the filter mappings
		FilterMappings filterMappings = new FilterMappings();
		filterMappings.inputProperties(properties);

		// Validate the filter mappings
		FilterMapping[] mappings = filterMappings.getFilterMappings();
		assertEquals("Incorrect number of mappings", 2, mappings.length);
		assertFilterMapping(mappings[0], "FilterOne", "/path/*", null);
		assertFilterMapping(mappings[1], "FilterTwo", null, "Servlet",
				MappingType.INCLUDE, MappingType.FORWARD, MappingType.REQUEST);
	}

}