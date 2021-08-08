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

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.descriptor.web.FilterMap;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.supply.ServletSupplierSource;
import net.officefloor.servlet.supply.ServletWoofExtensionService;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure able to inject dependency.
 * 
 * @author Daniel Sagenschneider
 */
public class InjectServletManagerTest extends OfficeFrameTestCase {

	/**
	 * Ensure inject {@link Servlet} {@link Dependency}.
	 */
	public void testServletDependency() throws Exception {
		this.doInjectTest(ServicingType.SERVLET, "dependency", "Servlet Dependency");
	}

	/**
	 * Ensure inject duplicate {@link Servlet} {@link Dependency}.
	 */
	public void testDuplicateServletDependency() throws Exception {
		this.doInjectTest(ServicingType.SERVLET, "duplicate", "Servlet Duplicate Dependency");
	}

	/**
	 * Ensure inject qualified {@link Servlet} {@link Dependency}.
	 */
	public void testQualifiedServletDependency() throws Exception {
		this.doInjectTest(ServicingType.SERVLET, "qualified", "Servlet Qualified Dependency");
	}

	/**
	 * Ensure inject {@link Servlet} {@link Inject}.
	 */
	public void testServletInject() throws Exception {
		this.doInjectTest(ServicingType.SERVLET, "inject", "Servlet Dependency");
	}

	/**
	 * Ensure inject qualified {@link Servlet} {@link Inject}.
	 */
	public void testQualifiedServletInject() throws Exception {
		this.doInjectTest(ServicingType.SERVLET, "qualified-inject", "Servlet Qualified Dependency");
	}

	/**
	 * Ensure inject {@link Servlet} instance {@link Dependency}.
	 */
	public void testServletInstanceDependency() throws Exception {
		this.doInjectTest(ServicingType.SERVLET_INSTANCE, "dependency", "Servlet Dependency");
	}

	/**
	 * Ensure inject {@link Servlet} instance {@link Inject}.
	 */
	public void testServletInstanceInject() throws Exception {
		this.doInjectTest(ServicingType.SERVLET_INSTANCE, "inject", "Servlet Dependency");
	}

	/**
	 * Ensure not inject dependencies into {@link Servlet} instance.
	 */
	public void testServletInstanceNoDependencies() throws Exception {
		this.doInjectTest(ServicingType.SERVLET_INSTANCE_NO_DEPENDENCIES, "no-dependencies", "Servlet none");
	}

	/**
	 * Ensure inject {@link Filter} {@link Dependency}.
	 */
	public void testFilterDependency() throws Exception {
		this.doInjectTest(ServicingType.FILTER, "dependency", "Filter Dependency");
	}

	/**
	 * Ensure inject duplicate {@link Filter} {@link Dependency}.
	 */
	public void testDuplicateFilterDependency() throws Exception {
		this.doInjectTest(ServicingType.FILTER, "duplicate", "Filter Duplicate Dependency");
	}

	/**
	 * Ensure inject qualified {@link Filter} {@link Dependency}.
	 */
	public void testQualifiedFilterDependency() throws Exception {
		this.doInjectTest(ServicingType.FILTER, "qualified", "Filter Qualified Dependency");
	}

	/**
	 * Ensure inject {@link Filter} {@link Inject}.
	 */
	public void testFilterInject() throws Exception {
		this.doInjectTest(ServicingType.FILTER, "inject", "Filter Dependency");
	}

	/**
	 * Ensure inject qualified {@link Filter} {@link Inject}.
	 */
	public void testQualifiedFilterInject() throws Exception {
		this.doInjectTest(ServicingType.FILTER, "qualified-inject", "Filter Qualified Dependency");
	}

	/**
	 * Undertakes test to ensure can inject dependency.
	 * 
	 * @param servicingType  {@link ServicingType}.
	 * @param parameter      Query parameter to identify dependency.
	 * @param expectedEntity Expected entity.
	 */
	private void doInjectTest(ServicingType servicingType, String parameter, String expectedEntity) throws Exception {
		CompileWoof woof = new CompileWoof();
		woof.woof((context) -> new ServletWoofExtensionService().extend(context));
		woof.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			office.addSupplier("DEPENDENCY", new SetupSupplierSource(servicingType));
			Singleton.load(office, "DEPENDENCY", new ServletDependency("Dependency"));
			OfficeManagedObject qualified = Singleton.load(office, "QUALIFIED_DEPENDENCY",
					new ServletDependency("Qualified Dependency"));
			qualified.addTypeQualification("QUALIFIED", ServletDependency.class.getName());
			qualified.addTypeQualification(QualifiedInject.class.getName(), ServletDependency.class.getName());
		});
		try (MockWoofServer server = woof.open()) {
			MockWoofResponse response = server.send(MockWoofServer.mockRequest("/dependency?test=" + parameter));
			response.assertResponse(200, expectedEntity);
		}
	}

	/**
	 * Type of servicing.
	 */
	public static enum ServicingType {
		FILTER, SERVLET, SERVLET_INSTANCE, SERVLET_INSTANCE_NO_DEPENDENCIES
	}

	/**
	 * Setup {@link DependencyHttpServlet}.
	 */
	private static class SetupSupplierSource extends AbstractSupplierSource {

		/**
		 * {@link ServicingType}.
		 */
		private final ServicingType servicingType;

		/**
		 * Instantiate.
		 * 
		 * @param isLoadFilter Indicates to load the {@link DependencyHttpFilter}.
		 */
		private SetupSupplierSource(ServicingType servicingType) {
			this.servicingType = servicingType;
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

			// Register based on servicing type
			final String FILTER_NAME = "FILTER";
			final String SERVLET_NAME = "SERVLET";
			switch (this.servicingType) {
			case FILTER:
				// Register the filter
				servletManager.addFilter(FILTER_NAME, DependencyHttpFilter.class, null);
				FilterMap filterMap = new FilterMap();
				filterMap.setFilterName(FILTER_NAME);
				filterMap.addURLPattern("/dependency");
				servletManager.getContext().addFilterMap(filterMap);
				break;

			case SERVLET:
				// Register the servlet
				servletManager.addServlet(SERVLET_NAME, DependencyHttpServlet.class, null);
				servletManager.getContext().addServletMappingDecoded("/dependency", SERVLET_NAME);
				break;

			case SERVLET_INSTANCE:
			case SERVLET_INSTANCE_NO_DEPENDENCIES:
				// Register the servlet instance
				boolean isInjectDependencies = this.servicingType.equals(ServicingType.SERVLET_INSTANCE);
				servletManager.addServlet(SERVLET_NAME, new DependencyHttpServlet(), isInjectDependencies, null);
				servletManager.getContext().addServletMappingDecoded("/dependency", SERVLET_NAME);
				break;

			default:
				fail("Unknown servicing type " + this.servicingType.name());
				break;
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

		private @Dependency ServletDependency dependency;

		private @Dependency ServletDependency duplicate;

		private @Qualified("QUALIFIED") @Dependency ServletDependency qualified;

		private @Inject ServletDependency inject;

		private @QualifiedInject @Inject ServletDependency qualifiedInject;

		/*
		 * =================== HttpServlet =====================
		 */

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			String message = null;
			String parameter = req.getParameter("test");
			switch (parameter) {
			case "dependency":
				message = this.dependency.getMessage();
				break;
			case "duplicate":
				message = "Duplicate " + this.duplicate.getMessage();
				break;
			case "qualified":
				message = this.qualified.getMessage();
				break;
			case "inject":
				message = this.inject.getMessage();
				break;
			case "qualified-inject":
				message = this.qualifiedInject.getMessage();
				break;
			case "no-dependencies":
				if (this.dependency != null) {
					message = "dependency available";
				} else if (this.duplicate != null) {
					message = "duplicate available";
				} else if (this.qualified != null) {
					message = "qualified available";
				} else if (this.inject != null) {
					message = "inject available";
				} else if (this.qualifiedInject != null) {
					message = "qualifiedInject available";
				} else {
					message = "none";
				}
				break;
			default:
				throw new ServletException("Unknown parameter " + parameter);
			}
			resp.getWriter().write("Servlet " + message);
		}
	}

	/**
	 * {@link HttpFilter} to use {@link ServletManager} obtained dependency.
	 */
	public static class DependencyHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		private @Dependency ServletDependency dependency;

		private @Dependency ServletDependency duplicate;

		private @Qualified("QUALIFIED") @Dependency ServletDependency qualified;

		private @Inject ServletDependency inject;

		private @QualifiedInject @Inject ServletDependency qualifiedInject;

		/*
		 * =================== HttpFilter =====================
		 */

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			String message = null;
			String parameter = request.getParameter("test");
			switch (parameter) {
			case "dependency":
				message = this.dependency.getMessage();
				break;
			case "duplicate":
				message = "Duplicate " + this.duplicate.getMessage();
				break;
			case "qualified":
				message = this.qualified.getMessage();
				break;
			case "inject":
				message = this.inject.getMessage();
				break;
			case "qualified-inject":
				message = this.qualifiedInject.getMessage();
				break;
			default:
				throw new ServletException("Unknown parameter " + parameter);
			}
			response.getWriter().write("Filter " + message);
		}
	}

	/**
	 * {@link Dependency} to be injected via {@link ServletManager}.
	 */
	public static class ServletDependency {

		private final String message;

		public ServletDependency(String message) {
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}
	}

}
