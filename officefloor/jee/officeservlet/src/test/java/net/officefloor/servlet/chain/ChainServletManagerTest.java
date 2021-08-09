/*-
 * #%L
 * Servlet
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.servlet.chain;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.supply.ServletSupplierSource;
import net.officefloor.servlet.supply.ServletWoofExtensionService;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests chaining the {@link ServletManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChainServletManagerTest extends OfficeFrameTestCase {

	/**
	 * Ensure can chain in {@link ServletManager} {@link Servlet}.
	 */
	public void testChainServlet() throws Exception {
		this.doChainServletManagerTest(true, "/servlet", 200, "CHAINED SERVLET - SETUP");
	}

	/**
	 * Ensure not chain in {@link ServletManager}.
	 */
	public void testNotChainServlet() throws Exception {
		this.doChainServletManagerTest(false, "/servlet", 404, "Not Found");
	}

	/**
	 * Ensure able to chain via {@link Property}.
	 */
	public void testChainViaProperty() throws Exception {
		this.doChainServletManagerTest(false, "/servlet", 200, "CHAINED SERVLET - SETUP",
				ServletWoofExtensionService.getChainServletsPropertyName("OFFICE"), "true");
	}

	/**
	 * Ensure can chain in {@link ServletManager} {@link Filter}.
	 */
	public void testChainFilter() throws Exception {
		this.doChainServletManagerTest(true, "/filter", 200, "CHAINED FILTER - SETUP");
	}

	/**
	 * Instantiate.
	 * 
	 * @param isChainServletManager  Flag to chain in {@link ServletManager}.
	 * @param path                   Path.
	 * @param expectedStatus         Expected status.
	 * @param expectedEntity         Expected entity.
	 * @param propertyNameValuePairs Name/value {@link Property} pairs.
	 */
	public void doChainServletManagerTest(boolean isChainServletManager, String path, int expectedStatus,
			String expectedEntity, String... propertyNameValuePairs) throws Exception {
		CompileWoof woof = new CompileWoof();
		woof.woof((context) -> new ServletWoofExtensionService().extend(context));
		woof.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			office.addSupplier("TEST", new ChainSupplierSource(isChainServletManager));
		});
		try (MockWoofServer server = woof.open(propertyNameValuePairs)) {
			MockWoofResponse response = server.send(MockWoofServer.mockRequest(path));
			if (expectedStatus == 200) {
				response.assertResponse(expectedStatus, expectedEntity);
			} else {
				response.assertJsonError(expectedStatus, new Exception(expectedEntity));
			}
		}
	}

	/**
	 * Supplies {@link Servlet} and {@link Filter} for testing.
	 */
	@TestSource
	private static class ChainSupplierSource extends AbstractSupplierSource {

		/**
		 * Indicates if chain {@link ServletManager}.
		 */
		private final boolean isChainServletManager;

		/**
		 * Instantiate.
		 * 
		 * @param isChainServletManager
		 */
		private ChainSupplierSource(boolean isChainServletManager) {
			this.isChainServletManager = isChainServletManager;
		}

		/*
		 * ================= SupplierSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Obtain the servlet manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();
			Context servletContext = servletManager.getContext();

			// Add the filter
			final String FILTER_NAME = "FILTER";
			servletManager.addFilter(FILTER_NAME, ChainedHttpFilter.class, (filterDef) -> {
				filterDef.addInitParameter("INIT", "SETUP");
			});
			FilterMap filterMap = new FilterMap();
			filterMap.addURLPattern("/filter");
			filterMap.setFilterName(FILTER_NAME);
			servletContext.addFilterMap(filterMap);

			// Add the servlet
			final String SERVLET_NAME = "SERVLET";
			servletManager.addServlet(SERVLET_NAME, ChainedHttpServlet.class, (wrapper) -> {
				wrapper.addInitParameter("INIT", "SETUP");
			});
			servletContext.addServletMappingDecoded("/servlet", SERVLET_NAME);

			// Chain in servlet manager
			if (this.isChainServletManager) {
				servletManager.chainInServletManager();
			}
		}

		@Override
		public void terminate() {
			// Nothing to terminate
		}
	}

	/**
	 * {@link Filter} for testing.
	 */
	public static class ChainedHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			response.getWriter().write("CHAINED FILTER - " + this.getInitParameter("INIT"));
		}
	}

	/**
	 * {@link Servlet} for testing.
	 */
	public static class ChainedHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write("CHAINED SERVLET - " + this.getInitParameter("INIT"));
		}
	}

}
