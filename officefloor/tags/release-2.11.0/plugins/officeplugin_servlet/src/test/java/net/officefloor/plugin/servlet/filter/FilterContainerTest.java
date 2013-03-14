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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.context.OfficeServletContext;

/**
 * Tests the {@link FilterContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterContainerTest extends OfficeFrameTestCase {

	/**
	 * {@link Filter} name.
	 */
	private final String FILTER_NAME = "Filter Name";

	/**
	 * Init parameter.
	 */
	private final Map<String, String> initParameters = new HashMap<String, String>();

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
	private final FilterChain chain = this.createMock(FilterChain.class);

	/**
	 * Verify {@link Filter} name.
	 */
	public void testFilterName() {
		this.doTest(new MockFilter() {
			@Override
			protected void test(FilterConfig config) {
				assertEquals("getFilterName()", FILTER_NAME, config
						.getFilterName());
			}
		});
	}

	/**
	 * Verify init parameters.
	 */
	public void testInitParameters() {
		this.initParameters.put("name", "value");
		this.doTest(new MockFilter() {
			@Override
			protected void test(FilterConfig config) {

				// Validate values
				assertEquals("getInitParameter(name)", "value", config
						.getInitParameter("name"));
				assertNull("getInitParameter(unknown)", config
						.getInitParameter("unknown"));

				// Validate names
				Enumeration<?> names = config.getInitParameterNames();
				assertTrue("Expecting a parameter name", names
						.hasMoreElements());
				assertEquals("Incorrect parameter name", "name", names
						.nextElement());
				assertFalse("Should only be a single parameter", names
						.hasMoreElements());
			}
		});
	}

	/**
	 * Verify {@link ServletContext}.
	 */
	public void testServletContext() {

		final String CONTEXT_PATH = "/context/path";

		// Record obtaining context path
		this.recordReturn(this.officeServletContext, this.officeServletContext
				.getContextPath(this.office), CONTEXT_PATH);

		// Test
		this.doTest(new MockFilter() {
			@Override
			protected void test(FilterConfig config) {
				ServletContext context = config.getServletContext();
				assertEquals("Incorrect context path", CONTEXT_PATH, context
						.getContextPath());
			}
		});
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param filter
	 *            {@link MockFilter}.
	 */
	private void doTest(MockFilter filter) {
		try {
			this.replayMockObjects();

			// Create the filter container for the filter
			FilterContainer container = new FilterContainerImpl(FILTER_NAME,
					filter, this.initParameters, this.officeServletContext,
					this.office);

			// Do the filter
			container.doFilter(this.request, this.response, this.chain);

			// Ensure appropriately initialised and filter invoked
			filter.validate();

			this.verifyMockObjects();

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Mock {@link Filter} for testing.
	 */
	private abstract class MockFilter implements Filter {

		/**
		 * Tests the {@link FilterConfig} for {@link Filter}.
		 * 
		 * @param config
		 *            {@link FilterConfig}.
		 */
		protected abstract void test(FilterConfig config);

		/**
		 * Flag indicating if initilised.
		 */
		private boolean isInitialised = false;

		/**
		 * Flag indicating if doFilter invoked.
		 */
		private boolean isDoFilterInvoked = false;

		/**
		 * Validate appropriately processed
		 */
		public void validate() {
			assertTrue("Should be initialised", this.isInitialised);
			assertTrue("Should invoke doFilter", this.isDoFilterInvoked);
		}

		/*
		 * ====================== Filter =======================
		 */

		@Override
		public void init(FilterConfig config) throws ServletException {
			assertFalse("Should only be initialised once", this.isInitialised);
			this.isInitialised = true;
			this.test(config);
		}

		@Override
		public void doFilter(ServletRequest req, ServletResponse resp,
				FilterChain chain) throws IOException, ServletException {
			this.isDoFilterInvoked = true;

			// Ensure correct inputs
			assertEquals("Incorrect request", FilterContainerTest.this.request,
					req);
			assertEquals("Incorrect response",
					FilterContainerTest.this.response, resp);
			assertEquals("Incorrect chain", FilterContainerTest.this.chain,
					chain);
		}

		@Override
		public void destroy() {
			fail("Should not be invoked");
		}
	}

}