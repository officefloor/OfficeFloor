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
package net.officefloor.plugin.servlet.filter;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.servlet.mapping.MappingType;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;

/**
 * {@link FilterChainFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterChainFactoryImpl implements FilterChainFactory {

	/**
	 * {@link Filter} mapping wild card.
	 */
	private static final String WILD_CARD = "*";

	/**
	 * Default {@link MappingType} instances.
	 */
	private static final MappingType[] DEFAULT_MAPPING_TYPES = new MappingType[] { MappingType.REQUEST };

	/**
	 * Listing of {@link Filter} mapping {@link FilterOption} by
	 * {@link MappingType} ordinal.
	 */
	private final FilterOption[][] filterMappingsByMappingType = new FilterOption[MappingType
			.values().length][];

	/**
	 * Listing of {@link Servlet} name {@link FilterOption} by
	 * {@link MappingType} ordinal.
	 */
	private final FilterOption[][] servletNamesByMappingType = new FilterOption[MappingType
			.values().length][];

	/**
	 * Initiate.
	 * 
	 * @param office
	 *            {@link Office}.
	 * @param servicers
	 *            {@link FilterServicer} instances.
	 * @throws ServletException
	 *             If fails to initiate.
	 */
	public FilterChainFactoryImpl(Office office, FilterServicer... servicers)
			throws ServletException {

		// Load the filter options
		for (FilterServicer servicer : servicers) {

			// Create the filter container
			FilterContainer filter = servicer.getFilterContainerFactory()
					.createFilterContainer(office);

			// Load servlet name option (if available)
			String servletName = servicer.getServletName();
			if (servletName != null) {
				FilterOption option = new ServletNameFilterOption(filter,
						servletName);
				this.loadFilterOption(option, servicer,
						this.servletNamesByMappingType);
			}

			// Load the filter mapping option (if available)
			String filterMapping = servicer.getFilterMapping();
			if (filterMapping != null) {

				// Create the filter option
				FilterOption option;
				if (filterMapping.endsWith(WILD_CARD)) {
					// Path prefix mapping
					String pathPrefix = filterMapping.substring(0,
							filterMapping.length() - WILD_CARD.length());
					option = new PathPrefixFilterOption(filter, pathPrefix);

				} else if (filterMapping.startsWith(WILD_CARD)) {
					// Extension mapping
					String extension = filterMapping.substring(WILD_CARD
							.length());
					option = new ExtensionFilterOption(filter, extension);

				} else {
					// Exact path mapping
					option = new ExactPathFilterOption(filter, filterMapping);
				}

				// Load the filter option
				this.loadFilterOption(option, servicer,
						this.filterMappingsByMappingType);
			}
		}
	}

	/**
	 * Loads the {@link FilterOption} for the {@link MappingType} instances into
	 * the {@link FilterOption} listings.
	 * 
	 * @param option
	 *            {@link FilterOption} to load.
	 * @param servicer
	 *            {@link FilterServicer} to provide {@link MappingType}
	 *            instances.
	 * @param options
	 *            {@link FilterOption} listing.
	 */
	private void loadFilterOption(FilterOption option, FilterServicer servicer,
			FilterOption[][] options) {

		// Obtain the mapping types
		MappingType[] mappingTypes = servicer.getMappingTypes();
		if ((mappingTypes == null) || (mappingTypes.length == 0)) {
			mappingTypes = DEFAULT_MAPPING_TYPES;
		}

		// Iterate over mapping types loading the options
		for (MappingType mappingType : mappingTypes) {

			// Obtain the options for the mapping type
			int mappingTypeIndex = mappingType.ordinal();
			FilterOption[] mappingOptions = options[mappingTypeIndex];

			// Determine if first option
			if (mappingOptions == null) {
				// First option, so just add option
				mappingOptions = new FilterOption[] { option };
			} else {
				// Append the option
				FilterOption[] tmp = new FilterOption[mappingOptions.length + 1];
				System.arraycopy(mappingOptions, 0, tmp, 0,
						mappingOptions.length);
				tmp[mappingOptions.length] = option;
				mappingOptions = tmp;
			}

			// Load updated mapping options back to options
			options[mappingTypeIndex] = mappingOptions;
		}
	}

	/**
	 * Creates the {@link FilterChain}.
	 * 
	 * @param path
	 *            Path for {@link Filter} mappings.
	 * @param servletName
	 *            {@link Servlet} name for matching.
	 * @param options
	 *            {@link FilterOption} instances.
	 * @param target
	 *            {@link FilterChain} target.
	 * @param containersUsed
	 *            Set of {@link FilterContainer} instances already used within
	 *            {@link FilterChain} to not use again.
	 * @return {@link FilterChain} to the {@link FilterChain} target.
	 */
	private FilterChain createFilterChain(String path, String servletName,
			FilterOption[] options, FilterChain target,
			Set<FilterContainer> containersUsed) {

		// Determine if have options
		if (options == null) {
			// No options, so no chain to target
			return target;
		}

		// Load the options in reverse order (so start is first in chain)
		for (int i = (options.length - 1); i >= 0; i--) {
			FilterOption option = options[i];

			// Determine if include option
			if (option.isInclude(path, servletName)) {
				// Determine if include container
				if (!containersUsed.contains(option.filter)) {
					// Include the option in the filter chain
					target = new FilterChainImpl(option.filter, target);

					// Container used
					containersUsed.add(option.filter);
				}
			}
		}

		// Return the filter chain
		return target;
	}

	/*
	 * ===================== FilterChainFactory ====================
	 */

	@Override
	public FilterChain createFilterChain(ServicerMapping mapping,
			MappingType mappingType, FilterChain target)
			throws ServletException {

		// Obtain details for filter chain
		String path = mapping.getServletPath();
		String pathInfo = mapping.getPathInfo();
		if (pathInfo != null) {
			path = path + pathInfo;
		}
		String servletName = mapping.getServicer().getServletName();

		// Create the set of containers to only filter once
		Set<FilterContainer> containersUsed = new HashSet<FilterContainer>();

		// Load servlet name options into chain first (last to filter)
		FilterOption[] options = this.servletNamesByMappingType[mappingType
				.ordinal()];
		target = this.createFilterChain(path, servletName, options, target,
				containersUsed);

		// Load filter mapping options into chain last (first to filter)
		options = this.filterMappingsByMappingType[mappingType.ordinal()];
		target = this.createFilterChain(path, servletName, options, target,
				containersUsed);

		// Return the filter chain
		return target;
	}

	/**
	 * {@link Filter} option for inclusion in the {@link FilterChain}.
	 */
	private static abstract class FilterOption {

		/**
		 * {@link FilterContainer} containing the {@link Filter} for this
		 * option.
		 */
		public final FilterContainer filter;

		/**
		 * Initiate.
		 * 
		 * @param filter
		 *            {@link FilterContainer} containing the {@link Filter} for
		 *            this option.
		 */
		public FilterOption(FilterContainer filter) {
			this.filter = filter;
		}

		/**
		 * Determines if include this {@link FilterOption} within the
		 * {@link FilterChain}.
		 * 
		 * @param path
		 *            Path.
		 * @param servletName
		 *            {@link Servlet} name.
		 * @return <code>true</code> if include.
		 */
		public abstract boolean isInclude(String path, String servletName);
	}

	/**
	 * {@link Servlet} name {@link FilterOption}.
	 */
	private static class ServletNameFilterOption extends FilterOption {

		/**
		 * {@link Servlet} name.
		 */
		private final String servletName;

		/**
		 * Initiate.
		 * 
		 * @param filter
		 *            {@link FilterContainer}.
		 * @param servletName
		 *            {@link Servlet} name.
		 */
		public ServletNameFilterOption(FilterContainer filter,
				String servletName) {
			super(filter);
			this.servletName = servletName;
		}

		@Override
		public boolean isInclude(String path, String servletName) {
			return (this.servletName.equals(servletName));
		}
	}

	/**
	 * Exact path {@link FilterOption}.
	 */
	private static class ExactPathFilterOption extends FilterOption {

		/**
		 * Exact path.
		 */
		private final String exactPath;

		/**
		 * Initiate.
		 * 
		 * @param filter
		 *            {@link FilterContainer}.
		 * @param exactPath
		 *            Exact path.
		 */
		public ExactPathFilterOption(FilterContainer filter, String exactPath) {
			super(filter);
			this.exactPath = exactPath;
		}

		@Override
		public boolean isInclude(String path, String servletName) {
			return (this.exactPath.equals(path));
		}
	}

	/**
	 * Path prefix {@link FilterOption}.
	 */
	private static class PathPrefixFilterOption extends FilterOption {

		/**
		 * Path prefix.
		 */
		private final String pathPrefix;

		/**
		 * Initiate.
		 * 
		 * @param filter
		 *            {@link FilterContainer}.
		 * @param pathPrefix
		 *            Path prefix.
		 */
		public PathPrefixFilterOption(FilterContainer filter, String pathPrefix) {
			super(filter);

			// Remove trailing path separators
			final String PATH_SEPARATOR = "/";
			while (pathPrefix.endsWith(PATH_SEPARATOR)) {
				pathPrefix = pathPrefix.substring(0, pathPrefix.length()
						- PATH_SEPARATOR.length());
			}

			// Specify the path prefix
			this.pathPrefix = pathPrefix;
		}

		@Override
		public boolean isInclude(String path, String servletName) {
			return (path.startsWith(this.pathPrefix));
		}
	}

	/**
	 * Extension {@link FilterOption}.
	 */
	private static class ExtensionFilterOption extends FilterOption {

		/**
		 * Extension.
		 */
		private final String extension;

		/**
		 * Initiate.
		 * 
		 * @param filter
		 *            {@link FilterContainer}.
		 * @param extension
		 *            Extension.
		 */
		public ExtensionFilterOption(FilterContainer filter, String extension) {
			super(filter);
			this.extension = extension;
		}

		@Override
		public boolean isInclude(String path, String servletName) {
			return (path.endsWith(this.extension));
		}
	}

}