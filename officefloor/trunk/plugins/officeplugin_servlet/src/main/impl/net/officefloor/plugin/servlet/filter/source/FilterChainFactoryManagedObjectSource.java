/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.filter.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.servlet.filter.FilterChainFactory;
import net.officefloor.plugin.servlet.filter.FilterChainFactoryImpl;
import net.officefloor.plugin.servlet.filter.FilterContainerFactory;
import net.officefloor.plugin.servlet.filter.FilterContainerFactoryImpl;
import net.officefloor.plugin.servlet.filter.FilterServicer;
import net.officefloor.plugin.servlet.filter.FilterServicerImpl;
import net.officefloor.plugin.servlet.mapping.MappingType;

/**
 * <p>
 * {@link ManagedObjectSource} for a {@link FilterChainFactory}.
 * <p>
 * {@link FilterChainFactory} is not provided as a {@link WorkSource} due to the
 * nature of the {@link FilterChain} - relies on a single {@link Thread}
 * execution.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterChainFactoryManagedObjectSource extends
		AbstractManagedObjectSource<None, None> implements ManagedObject {

	/**
	 * Prefix on property name to obtain the {@link Filter} name and its
	 * corresponding {@link Filter} {@link Class} name.
	 */
	public static final String PROPERTY_FILTER_INSTANCE_NAME_PREFIX = "filter.instance.name.";

	/**
	 * Prefix on property name to obtain Init Parameter name and its
	 * corresponding value.
	 */
	public static final String PROPERTY_FILTER_INSTANCE_INIT_PREFIX = "filter.instance.init.";

	/**
	 * Prefix on property name to obtain the {@link FilterMapping} index and its
	 * corresponding {@link Filter} name.
	 */
	public static final String PROPERTY_FILTER_MAPPING_INDEX_PREFIX = "filter.mapping.index.";

	/**
	 * Prefix on property name to obtain the {@link FilterMapping} URL pattern.
	 */
	public static final String PROPERTY_FILTER_MAPPING_URL_PREFIX = "filter.mapping.url.";

	/**
	 * Prefix on property name to obtain the {@link FilterMapping}
	 * {@link Servlet} name.
	 */
	public static final String PROPERTY_FILTER_MAPPING_SERVLET_PREFIX = "filter.mapping.servlet.";

	/**
	 * Prefix on property name to obtain the {@link FilterMapping}
	 * {@link MappingType}.
	 */
	public static final String PROPERTY_FILTER_MAPPING_TYPE_PREFIX = "filter.mapping.type.";

	/**
	 * {@link FilterChainFactory}.
	 */
	private FilterChainFactory filterChainFactory;

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

	/*
	 * ======================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// Specification optional
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the properties as property list
		PropertyList properties = this.createPropertyList(mosContext
				.getProperties());

		// Obtain the filter instance configuration
		FilterInstance[] instances = FilterInstance
				.loadFilterInstances(properties);

		// Create the filter container factories for each filter instance
		Map<String, FilterContainerFactory> factories = new HashMap<String, FilterContainerFactory>();
		for (FilterInstance instance : instances) {

			// Obtain details for the filter instance
			String filterName = instance.getName();
			String className = instance.getClassName();
			Map<String, String> initParameters = instance.getInitParameters();

			// Obtain the Filter implementation class
			Class<? extends Filter> filterClass = (Class<? extends Filter>) mosContext
					.getClassLoader().loadClass(className);

			// Create the filter container factory
			FilterContainerFactory factory = new FilterContainerFactoryImpl(
					filterName, filterClass, initParameters);

			// Register the filter container factory by filter name
			factories.put(filterName, factory);
		}

		// Obtain the filter mappings configuration
		FilterMappings filterMappings = new FilterMappings();
		filterMappings.inputProperties(properties);

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

			// Obtain the filter container factory for the mapping
			FilterContainerFactory factory = factories.get(filterName);
			if (factory == null) {
				// Must have factory
				throw new Exception("No filter by name '" + filterName
						+ "' configured for filter mapping (url-pattern="
						+ urlPattern + ", servlet=" + servletName + ")");
			}

			// Create the filter servicer for the mapping
			FilterServicer servicer = new FilterServicerImpl(urlPattern,
					servletName, mappingTypes, factory);

			// Add the filter servicer
			servicers.add(servicer);
		}

		// Create the filter chain factory
		this.filterChainFactory = new FilterChainFactoryImpl(servicers
				.toArray(new FilterServicer[0]));

		// Specify meta-data
		context.setObjectClass(FilterChainFactory.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ======================== ManagedObject =============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.filterChainFactory;
	}

}