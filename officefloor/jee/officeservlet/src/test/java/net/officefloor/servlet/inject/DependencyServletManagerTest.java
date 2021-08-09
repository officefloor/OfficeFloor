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

package net.officefloor.servlet.inject;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.descriptor.web.FilterMap;

import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.procedure.FilterProcedureSource;
import net.officefloor.servlet.procedure.ServletProcedureSource;
import net.officefloor.servlet.supply.ServletSupplierSource;
import net.officefloor.servlet.supply.ServletWoofExtensionService;
import net.officefloor.web.build.HttpInput;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure able to obtain dependency from {@link ServletManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyServletManagerTest extends OfficeFrameTestCase {

	/**
	 * Ensure dependency available to {@link Servlet} when chaining
	 * {@link ServletManager}.
	 */
	public void testChainServletDependency() throws Exception {
		this.doChainServletDependencyTest(false, HttpMethod.GET, "Servlet dependency via ServletManager");
	}

	/**
	 * Ensure {@link AvailableType} instances available to {@link Servlet}.
	 */
	public void testServletAvailableTypes() throws Exception {
		this.doChainServletDependencyTest(false, HttpMethod.POST, "Servlet found available ServletDependency");
	}

	/**
	 * Ensure {@link SourceContext} available to {@link Servlet}.
	 */
	public void testServletSourceContext() throws Exception {
		this.doChainServletDependencyTest(false, HttpMethod.PUT, "Servlet source context OFFICE");
	}

	/**
	 * Ensure dependency available to {@link Filter} when chaining
	 * {@link ServletManager}.
	 */
	public void testChainFilterDependency() throws Exception {
		this.doChainServletDependencyTest(true, HttpMethod.GET, "Filter dependency via ServletManager");
	}

	/**
	 * Ensure {@link AvailableType} instances available to {@link Filter}.
	 */
	public void testFilterAvailableTypes() throws Exception {
		this.doChainServletDependencyTest(true, HttpMethod.POST, "Filter found available ServletDependency");
	}

	/**
	 * Ensure {@link SourceContext} available to {@link Filer}.
	 */
	public void testFilterSourceContext() throws Exception {
		this.doChainServletDependencyTest(true, HttpMethod.PUT, "Filter source context OFFICE");
	}

	/**
	 * Ensure dependency available to {@link ServletProcedureSource}.
	 */
	public void testServletProcedureDependency() throws Exception {
		CompileWoof woof = new CompileWoof();
		woof.woof((context) -> {
			new ServletWoofExtensionService().extend(context);

			// Wire in servlet procedure
			OfficeSection servlet = context.getProcedureArchitect().addProcedure("SERVLET",
					DependencyHttpServlet.class.getName(), ServletProcedureSource.SOURCE_NAME, "servlet", false, null);
			HttpInput input = context.getWebArchitect().getHttpInput(false, "/servlet/procedure");
			context.getOfficeArchitect().link(input.getInput(),
					servlet.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		});
		woof.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			Singleton.load(office, new ServletDependency());
		});
		try (MockWoofServer server = woof.open()) {
			MockWoofResponse response = server.send(MockWoofServer.mockRequest("/servlet/procedure"));
			response.assertResponse(200, "Servlet dependency via ServletManager");
		}
	}

	/**
	 * Ensure dependency available to {@link FilterProcedureSource}.
	 */
	public void testFilterProcedureDependency() throws Exception {
		CompileWoof woof = new CompileWoof();
		woof.woof((context) -> {
			new ServletWoofExtensionService().extend(context);
			OfficeArchitect office = context.getOfficeArchitect();

			// Wire in filter procedure
			OfficeSection filter = context.getProcedureArchitect().addProcedure("SERVLET",
					DependencyHttpFilter.class.getName(), FilterProcedureSource.SOURCE_NAME, "filter", false, null);
			HttpInput input = context.getWebArchitect().getHttpInput(false, "/filter/procedure");
			office.link(input.getInput(), filter.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));

			// Link next
			OfficeSection next = office.addOfficeSection("NEXT", ClassSectionSource.class.getName(),
					ServletDependency.class.getName());
			office.link(filter.getOfficeSectionOutput("NEXT"), next.getOfficeSectionInput("getMessage"));
		});
		woof.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			Singleton.load(office, new ServletDependency());
		});
		try (MockWoofServer server = woof.open()) {
			MockWoofResponse response = server.send(MockWoofServer.mockRequest("/filter/procedure"));
			response.assertResponse(200, "Filter dependency via ServletManager");
		}
	}

	/**
	 * Undertakes test to ensure dependency available on chaining
	 * {@link ServletManager}.
	 * 
	 * @param isWithFilter   Indicates if test {@link Filter}.
	 * @param httpMethod     {@link HttpMethod}.
	 * @param expectedEntity Expected entity.
	 */
	private void doChainServletDependencyTest(boolean isWithFilter, HttpMethod httpMethod, String expectedEntity)
			throws Exception {
		CompileWoof woof = new CompileWoof();
		woof.woof((context) -> new ServletWoofExtensionService().extend(context));
		woof.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			office.addSupplier("DEPENDENCY", new SetupSupplierSource(isWithFilter));
			Singleton.load(office, new ServletDependency());
		});
		try (MockWoofServer server = woof.open()) {
			MockWoofResponse response = server.send(MockWoofServer.mockRequest("/dependency").method(httpMethod));
			response.assertResponse(200, expectedEntity);
		}
	}

	/**
	 * Generates the {@link AvailableType} response.
	 * 
	 * @param availableTypes {@link AvailableType} instances.
	 * @return {@link AvailableType} response.
	 */
	private static String generateAvailableTypeResponse(AvailableType... availableTypes) {
		for (AvailableType availableType : availableTypes) {
			if (ServletDependency.class.isAssignableFrom(availableType.getType())) {
				return "found available " + ServletDependency.class.getSimpleName();
			}
		}
		return "not found";
	}

	/**
	 * Setup {@link DependencyHttpServlet}.
	 */
	private static class SetupSupplierSource extends AbstractSupplierSource {

		/**
		 * Indicates to load the {@link DependencyHttpFilter}.
		 */
		private final boolean isLoadFilter;

		/**
		 * Instantiate.
		 * 
		 * @param isLoadFilter Indicates to load the {@link DependencyHttpFilter}.
		 */
		private SetupSupplierSource(boolean isLoadFilter) {
			this.isLoadFilter = isLoadFilter;
		}

		/*
		 * ===================== SupplierSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Obtain the servlet manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();

			// Ensure available types only available on completion
			try {
				servletManager.getAvailableTypes();
				fail("Should not be successful");
			} catch (IllegalStateException ex) {
				assertEquals("Incorrect cause",
						"AvailableType listing only available on ServletSupplierSource completion", ex.getMessage());
			}

			// Register the servlet
			final String SERVLET_NAME = "SERVLET";
			servletManager.addServlet(SERVLET_NAME, DependencyHttpServlet.class, null);
			servletManager.getContext().addServletMappingDecoded("/dependency", SERVLET_NAME);

			// Register the filter (if specified)
			if (this.isLoadFilter) {
				final String FILTER_NAME = "FILTER";
				servletManager.addFilter(FILTER_NAME, DependencyHttpFilter.class, null);
				FilterMap filterMap = new FilterMap();
				filterMap.setFilterName(FILTER_NAME);
				filterMap.addURLPattern("/dependency");
				servletManager.getContext().addFilterMap(filterMap);
			}

			// Chain in servlet
			servletManager.chainInServletManager();
		}

		@Override
		public void terminate() {
			// Nothing to terminate
		}
	}

	/**
	 * {@link HttpServlet} to use {@link ServletManager} obtained dependency.
	 */
	public static class DependencyHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		/**
		 * Dependency via {@link ServletManager}.
		 */
		private ServletDependency dependency;

		/**
		 * {@link AvailableType} instances.
		 */
		private AvailableType[] availableTypes;

		/**
		 * {@link OfficeExtensionContext}.
		 */
		private OfficeExtensionContext sourceContext;

		/*
		 * =================== HttpServlet =====================
		 */

		@Override
		public void init() throws ServletException {

			// Obtain the servlet manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();

			// Obtain the dependency
			this.dependency = servletManager.getDependency(null, ServletDependency.class);

			// Available types should be available on starting Servlet container
			this.availableTypes = servletManager.getAvailableTypes();

			// Obtain the source context
			this.sourceContext = servletManager.getSourceContext();
		}

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write("Servlet dependency " + this.dependency.getMessage());
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write("Servlet " + generateAvailableTypeResponse(this.availableTypes));
		}

		@Override
		protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write("Servlet source context " + this.sourceContext.getOfficeName());
		}
	}

	/**
	 * {@link HttpFilter} to use {@link ServletManager} obtained dependency.
	 */
	public static class DependencyHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		/**
		 * Dependency via {@link ServletManager}.
		 */
		private ServletDependency dependency;

		/**
		 * {@link AvailableType} instances.
		 */
		private AvailableType[] availableTypes;

		/**
		 * {@link OfficeExtensionContext}.
		 */
		private OfficeExtensionContext sourceContext;

		/*
		 * =================== HttpFilter =====================
		 */

		@Override
		public void init() throws ServletException {

			// Obtain the servlet manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();

			// Obtain the dependency
			this.dependency = servletManager.getDependency(null, ServletDependency.class);

			// Available types should be available on starting Servlet container
			this.availableTypes = servletManager.getAvailableTypes();

			// Obtain the source context
			this.sourceContext = servletManager.getSourceContext();
		}

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			switch (request.getMethod()) {
			case "GET":
				response.getWriter().write("Filter dependency " + this.dependency.getMessage());
				break;
			case "POST":
				response.getWriter().write("Filter " + generateAvailableTypeResponse(this.availableTypes));
				break;
			case "PUT":
				response.getWriter().write("Filter source context " + this.sourceContext.getOfficeName());
				break;
			default:
				throw new IllegalStateException("Invalid test request");
			}
		}
	}

	/**
	 * Dependency to be injected via {@link ServletManager}.
	 */
	public static class ServletDependency {

		public String getMessage() {
			return "via " + ServletManager.class.getSimpleName();
		}
	}

}
