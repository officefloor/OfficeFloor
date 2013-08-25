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
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.context.OfficeServletContext;

/**
 * Tests the {@link FilterContainerFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterContainerFactoryTest extends OfficeFrameTestCase {

	/**
	 * {@link Filter} name.
	 */
	private static final String FILTER_NAME = "Filter Name";

	/**
	 * Init parameters.
	 */
	private final Map<String, String> initParameters = new HashMap<String, String>();

	/**
	 * {@link OfficeServletContext}.
	 */
	private final OfficeServletContext officeServletContext = this
			.createMock(OfficeServletContext.class);

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
	 * {@link FilterChain}.
	 */
	private final FilterChain chain = this.createMock(FilterChain.class);

	/**
	 * Ensure able to construct {@link FilterContainer}.
	 */
	public void testCreate() throws Exception {

		final Office office = this.createMock(Office.class);

		// Reset for testing
		MockFilter.reset();

		// Test
		this.replayMockObjects();

		// Create the filter container
		FilterContainerFactory factory = new FilterContainerFactoryImpl(
				FILTER_NAME, MockFilter.class, this.initParameters,
				this.officeServletContext);
		FilterContainer container = factory.createFilterContainer(office);

		// Undertake filter to ensure correct
		container.doFilter(request, response, chain);

		this.verifyMockObjects();

		// Verify state
		assertNotNull("Must be initialised", MockFilter.config);
		assertEquals("Incorrect filter name", FILTER_NAME, MockFilter.config
				.getFilterName());
		assertEquals("Incorrect request", this.request, MockFilter.request);
		assertEquals("Incorrect response", this.response, MockFilter.response);
		assertEquals("Incorrect filter chain", this.chain, MockFilter.chain);
	}

	/**
	 * Ensure constructs a singleton {@link FilterContainer}.
	 */
	public void testSingleton() throws Exception {

		final Office one = this.createMock(Office.class);
		final Office two = this.createMock(Office.class);

		// Test
		this.replayMockObjects();

		// Create the factory
		FilterContainerFactory factory = new FilterContainerFactoryImpl(
				FILTER_NAME, MockFilter.class, this.initParameters,
				this.officeServletContext);

		// Create the filter container
		MockFilter.reset();
		FilterContainer container = factory.createFilterContainer(one);
		assertNotNull("Should be initialised", MockFilter.config);

		// Ensure is singleton
		MockFilter.reset();
		assertSame("Must be singleton within office", container, factory
				.createFilterContainer(one));
		assertNull("Should not re-initialise the singleton", MockFilter.config);

		// Ensure different container for another office
		MockFilter.reset();
		FilterContainer another = factory.createFilterContainer(two);
		assertNotNull("Should initialise new instance", MockFilter.config);
		assertFalse("Should be different instance for another office",
				container == another);

		this.verifyMockObjects();
	}

	/**
	 * Mock {@link Filter}.
	 */
	public static class MockFilter implements Filter {

		/**
		 * {@link FilterConfig}.
		 */
		public static FilterConfig config;

		/**
		 * {@link ServletRequest}.
		 */
		public static ServletRequest request;

		/**
		 * {@link ServletResponse}.
		 */
		public static ServletResponse response;

		/**
		 * {@link FilterChain}.
		 */
		public static FilterChain chain;

		/**
		 * Resets for the next test.
		 */
		public static void reset() {
			config = null;
			request = null;
			response = null;
			chain = null;
		}

		/*
		 * ======================== Filter ========================
		 */

		@Override
		public void init(FilterConfig config) throws ServletException {
			MockFilter.config = config;
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response,
				FilterChain chain) throws IOException, ServletException {
			MockFilter.request = request;
			MockFilter.response = response;
			MockFilter.chain = chain;
		}

		@Override
		public void destroy() {
			fail("Should not be invoked");
		}
	}

}