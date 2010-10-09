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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.servlet.container.HttpServletServicer;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.filter.FilterChainFactory;
import net.officefloor.plugin.servlet.filter.source.FilterChainFactoryManagedObjectSource.DependencyKeys;
import net.officefloor.plugin.servlet.mapping.MappingType;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;

/**
 * Tests the {@link FilterChainFactoryManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterChainFactoryManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		// All properties are optional
		ManagedObjectLoaderUtil
				.validateSpecification(FilterChainFactoryManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(FilterChainFactory.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				FilterChainFactoryManagedObjectSource.class);
	}

	/**
	 * Ensure able to source the {@link FilterChainFactory}.
	 */
	public void testSource() throws Throwable {

		final String FILTER_NAME = "Filter";
		final Office office = this.createMock(Office.class);
		final ServicerMapping mapping = this.createMock(ServicerMapping.class);
		final HttpServletServicer servletServicer = this
				.createMock(HttpServletServicer.class);
		final FilterChain target = this.createMock(FilterChain.class);
		final OfficeServletContext officeServletContext = this
				.createMock(OfficeServletContext.class);
		final ServletRequest request = this.createMock(ServletRequest.class);
		final ServletResponse response = this.createMock(ServletResponse.class);

		// Record
		this.recordReturn(mapping, mapping.getServletPath(), "/path");
		this.recordReturn(mapping, mapping.getPathInfo(), null);
		this.recordReturn(mapping, mapping.getServicer(), servletServicer);
		this.recordReturn(servletServicer, servletServicer.getServletName(),
				"Servlet");
		this.recordReturn(request, request.getAttribute(FILTER_NAME), null);
		target.doFilter(request, response);

		// Test
		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty("filter.instance.name.Filter", MockFilter.class
				.getName());
		loader.addProperty("filter.mapping.index.0", "Filter");
		loader.addProperty("filter.mapping.url.0", "/path/*");
		FilterChainFactoryManagedObjectSource source = loader
				.loadManagedObjectSource(FilterChainFactoryManagedObjectSource.class);

		// Source the filter chain factory
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(DependencyKeys.OFFICE_SERVLET_CONTEXT,
				officeServletContext);
		ManagedObject managedObject = user.sourceManagedObject(source);
		Object object = managedObject.getObject();
		assertTrue("Should be FilterChainFactory",
				object instanceof FilterChainFactory);
		FilterChainFactory factory = (FilterChainFactory) object;

		// Ensure runs filter
		FilterChain chain = factory.createFilterChain(office, mapping,
				MappingType.REQUEST, target);
		chain.doFilter(request, response);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link Filter} for testing.
	 */
	public static class MockFilter implements Filter {

		/**
		 * {@link Filter} name.
		 */
		private String filterName;

		/*
		 * ==================== Filter =============================
		 */

		@Override
		public void init(FilterConfig config) throws ServletException {
			this.filterName = config.getFilterName();
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response,
				FilterChain chain) throws IOException, ServletException {

			// Call on request (trigger for recording to ensure invoked)
			request.getAttribute(this.filterName);

			// Trigger next in filter chain
			chain.doFilter(request, response);
		}

		@Override
		public void destroy() {
			fail("Should not be destroyed");
		}
	}

}