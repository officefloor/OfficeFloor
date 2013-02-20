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

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.annotation.processing.Filer;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.container.HttpServletServicer;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.filter.FilterChainFactory;
import net.officefloor.plugin.servlet.filter.FilterChainFactoryImpl;
import net.officefloor.plugin.servlet.filter.FilterServicer;
import net.officefloor.plugin.servlet.mapping.MappingType;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;
import net.officefloor.plugin.servlet.mapping.ServicerMappingImpl;

/**
 * Tests the {@link FilterServicersFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterServicersFactoryTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeServletContext}.
	 */
	private final OfficeServletContext officeServletContext = this
			.createMock(OfficeServletContext.class);

	/**
	 * {@link FilterServicersFactory}.
	 */
	private final FilterServicersFactory factory = new FilterServicersFactory();

	/**
	 * Ensure no servicers if no configuration.
	 */
	public void testNoConfiguration() {
		FilterServicer[] servicers = this.createFilterServicers();
		assertEquals("Incorrect number of servicers", 0, servicers.length);
	}

	/**
	 * Ensure can configure a URL pattern {@link FilterServicer}.
	 */
	public void testUrlPattern() {
		FilterServicer[] servicers = this.createFilterServicers(
				"filter.instance.name.Filter", MockFilter.class.getName(),
				"filter.mapping.index.0", "Filter", "filter.mapping.url.0",
				"/path/*");
		assertEquals("Incorrect number of servicers", 1, servicers.length);
		assertServicer(servicers[0], "/path/*", null);
	}

	/**
	 * Ensure can configure a {@link Servlet} name {@link FilterServicer}.
	 */
	public void testServletName() {
		FilterServicer[] servicers = this.createFilterServicers(
				"filter.instance.name.Filter", MockFilter.class.getName(),
				"filter.mapping.index.0", "Filter", "filter.mapping.servlet.0",
				"Servlet");
		assertEquals("Incorrect number of servicers", 1, servicers.length);
		assertServicer(servicers[0], null, "Servlet");
	}

	/**
	 * Ensure can execute the {@link Filer}.
	 */
	public void testExecute() throws Exception {

		final Office office = this.createMock(Office.class);
		final HttpServletServicer servicer = this
				.createMock(HttpServletServicer.class);
		final FilterChain target = this.createMock(FilterChain.class);
		final ServletRequest request = this.createMock(ServletRequest.class);
		final ServletResponse response = this.createMock(ServletResponse.class);

		// Record
		this.recordReturn(servicer, servicer.getServletName(), "Servlet");
		this.recordReturn(request, request.getAttribute("test"), "TEST");

		// Test
		this.replayMockObjects();

		// Create the servicers
		FilterServicer[] servicers = this.createFilterServicers(
				"filter.instance.name.Filter", MockFilter.class.getName(),
				"filter.mapping.index.0", "Filter", "filter.mapping.url.0",
				"/path/*", "filter.mapping.servlet.0", "Servlet");

		// Create the filter chain factory
		FilterChainFactory factory = new FilterChainFactoryImpl(office,
				servicers);

		// Create the servicer mapping
		ServicerMapping mapping = new ServicerMappingImpl(servicer, "/path",
				null, null, new HashMap<String, String[]>());

		// Create the filter chain
		FilterChain chain = factory.createFilterChain(mapping,
				MappingType.REQUEST, target);

		// Execute the filtering
		chain.doFilter(request, response);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Asserts the {@link FilterServicer}.
	 * 
	 * @param servicer
	 *            {@link FilterServicer}.
	 * @param urlPattern
	 *            URL pattern.
	 * @param servletName
	 *            {@link Servlet} name.
	 * @param mappingTypes
	 *            {@link MappingType} instances.
	 */
	private static void assertServicer(FilterServicer servicer,
			String urlPattern, String servletName, MappingType... mappingTypes) {
		assertEquals("Incorrect url pattern", urlPattern, servicer
				.getFilterMapping());
		assertEquals("Incorrect servlet name", servletName, servicer
				.getServletName());
		MappingType[] types = servicer.getMappingTypes();
		assertEquals("Incorrect number of mapping types", mappingTypes.length,
				(types == null ? 0 : types.length));
		for (int i = 0; i < mappingTypes.length; i++) {
			assertEquals("Incorrect mapping type at index " + i,
					mappingTypes[i], types[i]);
		}
	}

	/**
	 * Creates the {@link FilterServicer} instances.
	 * 
	 * @param propertyNameValues
	 *            Property name value pairs.
	 * @return {@link FilterServicer} instances.
	 */
	private FilterServicer[] createFilterServicers(String... propertyNameValues) {
		try {

			// Create the properties
			Properties properties = new Properties();
			for (int i = 0; i < propertyNameValues.length; i += 2) {
				String name = propertyNameValues[i];
				String value = propertyNameValues[i + 1];
				properties.setProperty(name, value);
			}

			// Obtain the class loader
			ClassLoader classLoader = this.getClass().getClassLoader();

			// Create and return the filter servicers
			return this.factory.createFilterServices(properties, classLoader,
					this.officeServletContext);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Mock {@link Filter}.
	 */
	public static class MockFilter implements Filter {

		@Override
		public void init(FilterConfig config) throws ServletException {
			assertEquals("Incorrect filter name", "Filter", config
					.getFilterName());
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response,
				FilterChain chain) throws IOException, ServletException {
			assertEquals("TEST", request.getAttribute("test"));
		}

		@Override
		public void destroy() {
			fail("Should not be invoked");
		}
	}

}