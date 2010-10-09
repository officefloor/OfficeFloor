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
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
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
public class FilterChainFactoryManagedObjectSource
		extends
		AbstractManagedObjectSource<FilterChainFactoryManagedObjectSource.DependencyKeys, None> {

	/**
	 * Dependency keys for the {@link FilterChainFactoryManagedObjectSource}.
	 */
	public static enum DependencyKeys {
		OFFICE_SERVLET_CONTEXT
	}

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
	 * {@link ServicerInstance} instances for creating the
	 * {@link FilterChainFactory} specific to an {@link OfficeServletContext}.
	 */
	private ServicerInstance[] servicers;

	/**
	 * {@link FilterChainFactory} instances by {@link OfficeServletContext}.
	 * Typically there should only be the one {@link OfficeServletContext}.
	 */
	private final Map<OfficeServletContext, FilterChainFactory> filterChainFactories = new HashMap<OfficeServletContext, FilterChainFactory>(
			1);

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
	protected void loadMetaData(MetaDataContext<DependencyKeys, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the properties as property list
		PropertyList properties = this.createPropertyList(mosContext
				.getProperties());

		// Obtain the filter instance configuration
		FilterInstance[] instances = FilterInstance
				.loadFilterInstances(properties);

		// Create the container instance for each filter instance
		Map<String, ContainerInstance> containers = new HashMap<String, ContainerInstance>();
		for (FilterInstance instance : instances) {

			// Obtain details for the filter instance
			String filterName = instance.getName();
			String className = instance.getClassName();
			Map<String, String> initParameters = instance.getInitParameters();

			// Obtain the Filter implementation class
			Class<? extends Filter> filterClass = (Class<? extends Filter>) mosContext
					.getClassLoader().loadClass(className);

			// Create the container instance
			ContainerInstance container = new ContainerInstance(filterName,
					filterClass, initParameters);

			// Register the container by filter name
			containers.put(filterName, container);
		}

		// Obtain the filter mappings configuration
		FilterMappings filterMappings = new FilterMappings();
		filterMappings.inputProperties(properties);

		// Create the listing of servicer instances
		FilterMapping[] mappings = filterMappings.getFilterMappings();
		List<ServicerInstance> servicers = new ArrayList<ServicerInstance>(
				mappings.length);
		for (FilterMapping mapping : mappings) {

			// Obtain the name of the filter being mapped
			String filterName = mapping.getFilterName();

			// Obtain the mapping details
			String urlPattern = mapping.getUrlPattern();
			String servletName = mapping.getServletName();
			MappingType[] mappingTypes = mapping.getMappingTypes();

			// Obtain the container instance for the mapping
			ContainerInstance container = containers.get(filterName);
			if (container == null) {
				// Must have factory
				throw new Exception("No filter by name '" + filterName
						+ "' configured for filter mapping (url-pattern="
						+ urlPattern + ", servlet=" + servletName + ")");
			}

			// Create the servicer instance for the mapping
			ServicerInstance servicer = new ServicerInstance(urlPattern,
					servletName, mappingTypes, container);

			// Add the servicer instance
			servicers.add(servicer);
		}

		// Specify the servicer instances
		this.servicers = servicers.toArray(new ServicerInstance[0]);

		// Specify meta-data
		context.setObjectClass(FilterChainFactory.class);
		context.setManagedObjectClass(FilterChainFactoryManagedObject.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new FilterChainFactoryManagedObject();
	}

	/**
	 * Container instance to construct the {@link FilterContainerFactory}.
	 */
	private static class ContainerInstance {

		/**
		 * {@link FilterContainerFactory} instances by their
		 * {@link OfficeServletContext}.
		 */
		private final Map<OfficeServletContext, FilterContainerFactory> factories = new HashMap<OfficeServletContext, FilterContainerFactory>(
				1);

		/**
		 * {@link Filter} name.
		 */
		private final String filterName;

		/**
		 * {@link Filter} implementing {@link Class}.
		 */
		private final Class<? extends Filter> filterClass;

		/**
		 * Init parameters for the {@link Filter}.
		 */
		private final Map<String, String> initParameters;

		/**
		 * Initiate.
		 * 
		 * @param filterName
		 *            {@link Filter} name.
		 * @param filterClass
		 *            {@link Filter} implementing {@link Class}.
		 * @param initParameters
		 *            Init parameters for the {@link Filter}.
		 */
		public ContainerInstance(String filterName,
				Class<? extends Filter> filterClass,
				Map<String, String> initParameters) {
			this.filterName = filterName;
			this.filterClass = filterClass;
			this.initParameters = initParameters;
		}

		/**
		 * Creates the {@link FilterContainerFactory} for the
		 * {@link OfficeServletContext}.
		 * 
		 * @param officeServletContext
		 *            {@link OfficeServletContext}.
		 * @return {@link FilterContainerFactory} for the
		 *         {@link OfficeServletContext}.
		 */
		public synchronized FilterContainerFactory createFilterContainerFactory(
				OfficeServletContext officeServletContext) {

			// Lazy create the factory
			FilterContainerFactory factory = this.factories
					.get(officeServletContext);
			if (factory == null) {
				factory = new FilterContainerFactoryImpl(this.filterName,
						this.filterClass, this.initParameters,
						officeServletContext);
				this.factories.put(officeServletContext, factory);
			}

			// Return the factory
			return factory;
		}
	}

	/**
	 * Servicer instance to construct the {@link FilterServicer}.
	 */
	private static class ServicerInstance {

		/**
		 * URL pattern.
		 */
		private final String urlPattern;

		/**
		 * {@link Servlet} name.
		 */
		private final String servletName;

		/**
		 * {@link MappingType} instances.
		 */
		private final MappingType[] mappingTypes;

		/**
		 * {@link ContainerInstance}.
		 */
		private final ContainerInstance container;

		/**
		 * Initiate.
		 * 
		 * @param urlPattern
		 *            URL pattern.
		 * @param servletName
		 *            {@link Servlet} name.
		 * @param mappingTypes
		 *            {@link MappingType} instances.
		 * @param container
		 *            {@link ContainerInstance}.
		 */
		public ServicerInstance(String urlPattern, String servletName,
				MappingType[] mappingTypes, ContainerInstance container) {
			this.urlPattern = urlPattern;
			this.servletName = servletName;
			this.mappingTypes = mappingTypes;
			this.container = container;
		}

		/**
		 * Creates the {@link FilterServicer}.
		 * 
		 * @param officeServletContext
		 *            {@link OfficeServletContext}.
		 * @return {@link FilterServicer}.
		 */
		public FilterServicer createFilterServicer(
				OfficeServletContext officeServletContext) {

			// Create the filter container factory
			FilterContainerFactory factory = this.container
					.createFilterContainerFactory(officeServletContext);

			// Create the filter servicer for the mapping
			FilterServicer servicer = new FilterServicerImpl(this.urlPattern,
					this.servletName, this.mappingTypes, factory);

			// Return the servicer
			return servicer;
		}
	}

	/**
	 * {@link FilterChainFactory} {@link ManagedObject}.
	 */
	private class FilterChainFactoryManagedObject implements
			CoordinatingManagedObject<DependencyKeys> {

		/**
		 * {@link FilterChainFactory}.
		 */
		private FilterChainFactory filterChainFactory;

		/*
		 * ======================== ManagedObject =============================
		 */

		@Override
		public void loadObjects(ObjectRegistry<DependencyKeys> registry)
				throws Throwable {

			// Obtain the office servlet context
			OfficeServletContext officeServletContext = (OfficeServletContext) registry
					.getObject(DependencyKeys.OFFICE_SERVLET_CONTEXT);

			// Lazy create the filter chain factory
			synchronized (FilterChainFactoryManagedObjectSource.this.filterChainFactories) {
				this.filterChainFactory = FilterChainFactoryManagedObjectSource.this.filterChainFactories
						.get(officeServletContext);
				if (this.filterChainFactory == null) {

					// Create the filter servicers
					FilterServicer[] filterServicers = new FilterServicer[FilterChainFactoryManagedObjectSource.this.servicers.length];
					for (int i = 0; i < filterServicers.length; i++) {
						ServicerInstance instance = FilterChainFactoryManagedObjectSource.this.servicers[i];

						// Create and load the filter servicer
						filterServicers[i] = instance
								.createFilterServicer(officeServletContext);
					}

					// Create the filter chain factory
					this.filterChainFactory = new FilterChainFactoryImpl(
							filterServicers);

					// Cache the filter chain factory for improved performance
					FilterChainFactoryManagedObjectSource.this.filterChainFactories
							.put(officeServletContext, this.filterChainFactory);
				}
			}
		}

		@Override
		public Object getObject() throws Throwable {
			return this.filterChainFactory;
		}
	}

}