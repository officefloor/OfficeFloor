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

import java.util.LinkedList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.plugin.servlet.context.source.OfficeServletContextManagedObjectSource;
import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * Listing of {@link FilterMapping} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterMappings {

	/**
	 * {@link FilterMapping} instances.
	 */
	private final List<FilterMapping> mappings = new LinkedList<FilterMapping>();

	/**
	 * <p>
	 * Adds a {@link FilterMapping}.
	 * <p>
	 * The order of adding is maintained.
	 * 
	 * @param filterName
	 *            {@link Filter} name.
	 * @param urlPattern
	 *            URL pattern for matching path.
	 * @param servletName
	 *            {@link Servlet} name for matching.
	 * @param mappingTypes
	 *            {@link MappingType} that is applicable for the added
	 *            {@link FilterMapping}.
	 */
	public void addFilterMapping(String filterName, String urlPattern,
			String servletName, MappingType... mappingTypes) {
		this.mappings.add(new FilterMapping(filterName, urlPattern,
				servletName, mappingTypes));
	}

	/**
	 * Obtains the {@link FilterMapping} instances in order.
	 * 
	 * @return {@link FilterMapping} instances in order.
	 */
	public FilterMapping[] getFilterMappings() {
		return this.mappings.toArray(new FilterMapping[0]);
	}

	/**
	 * Outputs the {@link Property} instances to the {@link PropertyList}.
	 * 
	 * @param properties
	 *            {@link PropertyList}.
	 */
	public void outputProperties(PropertyList properties) {

		// Write out the properties for mappings
		int index = 0;
		for (FilterMapping mapping : this.mappings) {

			// Output the filter name (including the index)
			String filterName = mapping.getFilterName();
			properties
					.addProperty(
							OfficeServletContextManagedObjectSource.PROPERTY_FILTER_MAPPING_INDEX_PREFIX
									+ index).setValue(filterName);

			// Output the URL pattern (if available)
			String urlPattern = mapping.getUrlPattern();
			if (urlPattern != null) {
				properties
						.addProperty(
								OfficeServletContextManagedObjectSource.PROPERTY_FILTER_MAPPING_URL_PREFIX
										+ index).setValue(urlPattern);
			}

			// Output the Servlet name (if available)
			String servletName = mapping.getServletName();
			if (servletName != null) {
				properties
						.addProperty(
								OfficeServletContextManagedObjectSource.PROPERTY_FILTER_MAPPING_SERVLET_PREFIX
										+ index).setValue(servletName);
			}

			// Output the mapping types property value (if available)
			MappingType[] types = mapping.getMappingTypes();
			if ((types != null) && (types.length > 0)) {
				StringBuilder typesValue = new StringBuilder();
				boolean isFirst = true;
				for (MappingType type : types) {
					if (!isFirst) {
						typesValue.append(",");
					}
					isFirst = false;
					typesValue.append(type.name());
				}
				properties
						.addProperty(
								OfficeServletContextManagedObjectSource.PROPERTY_FILTER_MAPPING_TYPE_PREFIX
										+ index)
						.setValue(typesValue.toString());
			}

			// Increment index for next mapping
			index++;
		}
	}

	/**
	 * Configures the {@link FilterMapping} instances from the
	 * {@link PropertyList}.
	 * 
	 * @param properties
	 *            {@link PropertyList}.
	 */
	public void inputProperties(PropertyList properties) {

		// Sequentially increment index to load mappings
		int index = 0;
		String filterName = properties
				.getPropertyValue(
						OfficeServletContextManagedObjectSource.PROPERTY_FILTER_MAPPING_INDEX_PREFIX
								+ index, null);
		while (filterName != null) {

			// Obtain the URL pattern
			String urlPattern = properties
					.getPropertyValue(
							OfficeServletContextManagedObjectSource.PROPERTY_FILTER_MAPPING_URL_PREFIX
									+ index, null);

			// Obtain the Servlet Name
			String servletName = properties
					.getPropertyValue(
							OfficeServletContextManagedObjectSource.PROPERTY_FILTER_MAPPING_SERVLET_PREFIX
									+ index, null);

			// Obtain the mapping types (if available)
			MappingType[] mappingTypes = null;
			String typesValue = properties
					.getPropertyValue(
							OfficeServletContextManagedObjectSource.PROPERTY_FILTER_MAPPING_TYPE_PREFIX
									+ index, null);
			if (typesValue != null) {
				String[] typesValues = typesValue.split(",");
				List<MappingType> types = new LinkedList<MappingType>();
				for (String value : typesValues) {
					MappingType type = MappingType.valueOf(value);
					types.add(type);
				}
				mappingTypes = types.toArray(new MappingType[0]);
			}

			// Create and add the mapping
			this.mappings.add(new FilterMapping(filterName, urlPattern,
					servletName, mappingTypes));

			// Obtain the next mapping's filter name
			index++;
			filterName = properties
					.getPropertyValue(
							OfficeServletContextManagedObjectSource.PROPERTY_FILTER_MAPPING_INDEX_PREFIX
									+ index, null);
		}
	}

}