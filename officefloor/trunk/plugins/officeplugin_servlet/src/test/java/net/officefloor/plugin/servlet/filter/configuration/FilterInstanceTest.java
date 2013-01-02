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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.plugin.servlet.filter.configuration.FilterInstance;

/**
 * Tests the {@link FilterInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterInstanceTest extends AbstractConfigurationTestCase {

	/**
	 * Ensure can output properties from a {@link FilterInstance}.
	 */
	public void testOutputProperties() {

		// Create and configure the filter
		FilterInstance instance = new FilterInstance("Filter");
		instance.setClassName("MockFilter");
		instance.addInitParameter("one", "A");
		instance.addInitParameter("two", "B");

		// Obtain the properties
		PropertyList properties = createPropertyList();
		instance.outputProperties(properties);

		// Validate the properties
		assertPropertyList(properties, "filter.instance.name.Filter",
				"MockFilter", "filter.instance.init.Filter.one", "A",
				"filter.instance.init.Filter.two", "B");
	}

	/**
	 * Ensure can input properties to configure a {@link FilterInstance}.
	 */
	public void testInputProperties() {

		// Obtain the properties
		PropertyList properties = createPropertyList(
				"filter.instance.name.Filter", "MockFilter",
				"filter.instance.init.Filter.one", "A",
				"filter.instance.init.Filter.two", "B");

		// Create and configure the filter
		FilterInstance instance = new FilterInstance("Filter");
		instance.inputProperties(properties);

		// Validate configuration of the filter instance
		assertFilterInstance(instance, "Filter", "MockFilter", "one", "A",
				"two", "B");
	}

	/**
	 * Ensure able to load {@link FilterInstance} instances from the
	 * {@link PropertyList}.
	 */
	public void testLoadFilterInstances() {

		// Obtain the properties
		PropertyList properties = createPropertyList(
				"filter.instance.name.ONE", "MockFilter1",
				"filter.instance.init.ONE.name", "A",
				"filter.instance.name.TWO", "MockFilter2",
				"filter.instance.init.TWO.name", "B");

		// Load the filter instances
		FilterInstance[] instances = FilterInstance
				.loadFilterInstances(properties);

		// Validate the filter instance configuration
		assertEquals("Incorrect number of Filter instances", 2,
				instances.length);
		assertFilterInstance(instances[0], "ONE", "MockFilter1", "name", "A");
		assertFilterInstance(instances[1], "TWO", "MockFilter2", "name", "B");
	}

}