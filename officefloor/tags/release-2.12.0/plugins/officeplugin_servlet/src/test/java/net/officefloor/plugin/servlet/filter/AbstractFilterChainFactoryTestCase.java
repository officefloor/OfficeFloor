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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.container.HttpServletServicer;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.mapping.MappingType;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;
import net.officefloor.plugin.servlet.mapping.ServicerMappingImpl;

/**
 * Test the {@link FilterChainManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFilterChainFactoryTestCase extends
		OfficeFrameTestCase {

	/**
	 * {@link ServletRequest}.
	 */
	private final ServletRequest request = this
			.createMock(ServletRequest.class);

	/**
	 * {@link ServletResponse}.
	 */
	private final ServletResponse response = this
			.createMock(ServletResponse.class);

	/**
	 * Target {@link FilterChain}.
	 */
	private final FilterChain target = this.createMock(FilterChain.class);

	/**
	 * {@link OfficeServletContext}.
	 */
	private final OfficeServletContext officeServletContext = this
			.createMock(OfficeServletContext.class);

	/**
	 * {@link Office}.
	 */
	private final Office office = this.createMock(Office.class);

	/**
	 * {@link FilterServicer} instances.
	 */
	private final List<FilterServicer> services = new LinkedList<FilterServicer>();

	/**
	 * Allow lazy create of {@link FilterChainFactory}.
	 */
	private FilterChainFactory factory = null;

	/**
	 * Convenience method to do a single {@link Filter} test.
	 * 
	 * @param mappedServletName
	 *            Mapped {@link Servlet} name.
	 * @param mappedServletPath
	 *            Mapped {@link Servlet} path.
	 * @param mappedPathInfo
	 *            Mapped path info.
	 * @param mappingType
	 *            {@link MappingType} being undertaken.
	 * @param filterMapping
	 *            {@link Filter} mapping.
	 * @param servletName
	 *            {@link Servlet} name.
	 * @param mappingTypes
	 *            {@link MappingType} instances for mapping.
	 */
	protected void doSingleFilterTest(String mappedServletName,
			String mappedServletPath, String mappedPathInfo,
			MappingType mappingType, String filterMapping, String servletName,
			MappingType... mappingTypes) {
		final String FILTER_NAME = "SingleFilter";
		this.addServicer(FILTER_NAME, filterMapping, servletName, mappingTypes);
		this.record_init(FILTER_NAME);
		this.record_doFilter(FILTER_NAME);
		this.doFilter(mappedServletPath, mappedPathInfo, mappingType,
				mappedServletName);
	}

	/**
	 * Undertakes the filtering with the {@link FilterChainFactory}.
	 * 
	 * @param servletPath
	 *            {@link Servlet} path.
	 * @param pathInfo
	 *            Path info.
	 * @param mappingType
	 *            {@link MappingType}.
	 * @param servletName
	 *            {@link Servlet} name.
	 */
	protected void doFilter(String servletPath, String pathInfo,
			MappingType mappingType, String servletName) {
		try {

			// Ensure construct factory and set for replay
			if (this.factory == null) {
				this.replayMockObjects();
				this.factory = new FilterChainFactoryImpl(this.office,
						this.services.toArray(new FilterServicer[0]));
			}

			// Create the servicer mapping
			HttpServletServicer servicer = new MockHttpServletServicer(
					servletName);
			ServicerMapping mapping = new ServicerMappingImpl(servicer,
					servletPath, pathInfo, null,
					new HashMap<String, String[]>());

			// Default mapping type to request
			if (mappingType == null) {
				mappingType = MappingType.REQUEST;
			}

			// Construct the filter chain
			FilterChain chain = this.factory.createFilterChain(mapping,
					mappingType, this.target);
			assertNotNull("Expecting filter chain for " + mappingType + ":"
					+ servletName + "@" + servletPath
					+ (pathInfo == null ? "" : pathInfo), chain);

			// Undertake the filtering
			chain.doFilter(this.request, this.response);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	@Override
	protected void tearDown() throws Exception {

		// Ensure appropriate functionality (all filtering occurring)
		this.verifyMockObjects();

		// Parent clean-up
		super.tearDown();
	}

	/**
	 * Records instantiating new {@link Filter} instances by the name.
	 * 
	 * @param filterNames
	 *            {@link Filter} names in the order of initialisation.
	 */
	protected void record_init(String... filterNames) {
		// Initialising triggers below recording
		for (String filterName : filterNames) {

			// Record obtaining filter name
			this.recordReturn(this.officeServletContext,
					this.officeServletContext.getInitParameter(this.office,
							filterName), "init parameter");
		}
	}

	/**
	 * Records undertaking the filtering.
	 * 
	 * @param filterNames
	 *            {@link Filter} names in the order of filtering.
	 */
	protected void record_doFilter(String... filterNames) {
		try {
			// Filtering triggers below recording
			for (String filterName : filterNames) {
				this.recordReturn(this.request, this.request
						.getAttribute(filterName), "request attribute");
				this.recordReturn(this.officeServletContext,
						this.officeServletContext.getAttribute(this.office,
								filterName), "context attribute");
			}

			// Finally triggers the target filter chain
			this.target.doFilter(this.request, this.response);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Records a {@link FilterServicer} for a new {@link Filter} by the name.
	 * 
	 * @param filterName
	 *            {@link Filter} name.
	 * @param filterMapping
	 *            {@link Filter} mapping. May be <code>null</code>.
	 * @param servletName
	 *            {@link Servlet} name. May be <code>null</code>.
	 * @param mappingTypes
	 *            {@link MappingType} instances.
	 */
	protected void addServicer(String filterName, String filterMapping,
			String servletName, MappingType... mappingTypes) {

		// Create the factory
		FilterContainerFactory factory = this.createFactory(filterName);

		// Add the servicer for the factory
		this.addServicer(factory, filterMapping, servletName, mappingTypes);
	}

	/**
	 * Records a {@link FilterServicer} for the {@link FilterContainerFactory}.
	 * 
	 * @param factory
	 *            {@link FilterContainerFactory}.
	 * @param filterMapping
	 *            {@link Filter} mapping. May be <code>null</code>.
	 * @param servletName
	 *            {@link Servlet} name. May be <code>null</code>.
	 * @param mappingTypes
	 *            {@link MappingType} instances.
	 */
	protected void addServicer(FilterContainerFactory factory,
			String filterMapping, String servletName,
			MappingType... mappingTypes) {

		// Default mapping type to request
		if (mappingTypes.length == 0) {
			mappingTypes = new MappingType[] { MappingType.REQUEST };
		}

		// Create the servicer
		FilterServicer servicer = new FilterServicerImpl(filterMapping,
				servletName, mappingTypes, factory);

		// Add the servicer
		this.services.add(servicer);
	}

	/**
	 * Creates the {@link FilterContainerFactory}.
	 * 
	 * @param filterName
	 *            Name of {@link Filter}.
	 * @return {@link FilterContainerFactory}.
	 */
	protected FilterContainerFactory createFactory(String filterName) {

		// Create the filter container factory
		Map<String, String> initParameters = new HashMap<String, String>();
		initParameters.put(MockFilter.PARAMETER_FILTER_NAME, filterName);
		FilterContainerFactory factory = new FilterContainerFactoryImpl(
				filterName, MockFilter.class, initParameters,
				this.officeServletContext);

		// Return the filter container factory
		return factory;
	}

	/**
	 * Mock {@link Filter}.
	 */
	public static class MockFilter implements Filter {

		/**
		 * Initialisation parameter for {@link Filter} name.
		 */
		public static final String PARAMETER_FILTER_NAME = "parameter.filter.name";

		/**
		 * {@link Filter} name.
		 */
		private String filterName;

		/**
		 * {@link FilterConfig}.
		 */
		private FilterConfig config = null;

		/*
		 * ==================== Filter ==========================
		 */

		@Override
		public void init(FilterConfig config) throws ServletException {

			// Load the config
			this.filterName = config.getFilterName();
			assertNull("Filter " + this.filterName
					+ " should only be initialised once", this.config);
			this.config = config;

			// Ensure appropriate initialisation parameter
			String name = config.getInitParameter(PARAMETER_FILTER_NAME);
			assertEquals("Incorrect filter name", name, this.filterName);

			// Ensure appropriate construction for office (invokes mock)
			config.getServletContext().getInitParameter(this.filterName);
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response,
				FilterChain chain) throws IOException, ServletException {

			// Ensure initialised
			assertNotNull("Filter " + this.filterName
					+ " should be initialised", this.config);

			// Ensure appropriate chaining (invokes mock)
			request.getAttribute(this.filterName);
			this.config.getServletContext().getAttribute(this.filterName);

			// Trigger the chain
			chain.doFilter(request, response);
		}

		@Override
		public void destroy() {
			fail("Should not destroy filter");
		}
	}

	/**
	 * Mock {@link HttpServletServicer} for testing.
	 */
	private class MockHttpServletServicer implements HttpServletServicer {

		/**
		 * {@link Servlet} name.
		 */
		private final String servletName;

		/**
		 * Initiate.
		 * 
		 * @param servletName
		 *            {@link Servlet} name.
		 */
		public MockHttpServletServicer(String servletName) {
			this.servletName = servletName;
		}

		/*
		 * ==================== HttpServletServicer =================
		 */

		@Override
		public String getServletName() {
			return this.servletName;
		}

		@Override
		public String[] getServletMappings() {
			fail("Should not be invoked");
			return null;
		}

		@Override
		public void include(OfficeServletContext context,
				HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			fail("Should not be invoked");
		}
	}

}