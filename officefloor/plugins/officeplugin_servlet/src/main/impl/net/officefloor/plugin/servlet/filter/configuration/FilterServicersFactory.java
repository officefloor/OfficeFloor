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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.ServletException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.filter.FilterContainerFactory;
import net.officefloor.plugin.servlet.filter.FilterContainerFactoryImpl;
import net.officefloor.plugin.servlet.filter.FilterServicer;
import net.officefloor.plugin.servlet.filter.FilterServicerImpl;
import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * Factory for the creation of the {@link FilterServicer} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterServicersFactory {

	/**
	 * Creates the {@link FilterServicer} instances from the {@link Properties}
	 * configuration.
	 * 
	 * @param properties
	 *            {@link Properties}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param officeServletContext
	 *            {@link OfficeServletContext}.
	 * @return {@link FilterServicer} instances.
	 * @throws ServletException
	 *             If fails to create the {@link FilterServicer} instances.
	 */
	@SuppressWarnings("unchecked")
	public FilterServicer[] createFilterServices(Properties properties,
			ClassLoader classLoader, OfficeServletContext officeServletContext)
			throws ServletException {

		// Obtain the properties as property list
		PropertyList propertyList = this.createPropertyList(properties);

		// Obtain the filter instance configuration
		FilterInstance[] instances = FilterInstance
				.loadFilterInstances(propertyList);

		// Create the filter container factory for each filter instance
		Map<String, FilterContainerFactory> factories = new HashMap<String, FilterContainerFactory>();
		for (FilterInstance instance : instances) {

			// Obtain details for the filter instance
			String filterName = instance.getName();
			String className = instance.getClassName();
			Map<String, String> initParameters = instance.getInitParameters();

			// Obtain the Filter implementation class
			Class<? extends Filter> filterClass;
			try {
				filterClass = (Class<? extends Filter>) classLoader
						.loadClass(className);
			} catch (ClassNotFoundException ex) {
				throw new ServletException(ex);
			}

			// Create the filter container factory
			FilterContainerFactory factory = new FilterContainerFactoryImpl(
					filterName, filterClass, initParameters,
					officeServletContext);

			// Register the filter container factory by filter name
			factories.put(filterName, factory);
		}

		// Obtain the filter mappings configuration
		FilterMappings filterMappings = new FilterMappings();
		filterMappings.inputProperties(propertyList);

		// Create the listing of filter servicers
		FilterMapping[] mappings = filterMappings.getFilterMappings();
		List<FilterServicer> servicers = new ArrayList<FilterServicer>(
				mappings.length);
		for (FilterMapping mapping : mappings) {

			// Obtain the name of the filter being mapped
			String filterName = mapping.getFilterName();

			// Obtain the mapping details
			String urlPattern = mapping.getUrlPattern();
			String servletName = mapping.getServletName();
			MappingType[] mappingTypes = mapping.getMappingTypes();

			// Obtain the filter container factory instance for the mapping
			FilterContainerFactory factory = factories.get(filterName);
			if (factory == null) {
				// Must have factory
				throw new ServletException("No filter by name '" + filterName
						+ "' configured for filter mapping (url-pattern="
						+ urlPattern + ", servlet=" + servletName + ")");
			}

			// Create the filter servicer for the mapping
			FilterServicer servicer = new FilterServicerImpl(urlPattern,
					servletName, mappingTypes, factory);

			// Add the servicer instance
			servicers.add(servicer);
		}

		// Return the servicer instances
		return servicers.toArray(new FilterServicer[0]);

	}

	/**
	 * Creates the {@link PropertyList} from the {@link Properties}.
	 * 
	 * @param properties
	 *            {@link Properties}.
	 * @return Populated {@link PropertyList}.
	 */
	private PropertyList createPropertyList(Properties properties) {

		// Create the property list
		PropertyList propertyList = OfficeFloorCompiler.newPropertyList();
		for (String name : properties.stringPropertyNames()) {
			String value = properties.getProperty(name);
			propertyList.addProperty(name).setValue(value);
		}

		// Return the property list
		return propertyList;
	}

}