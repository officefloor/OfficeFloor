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
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
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
		this.doChainServletDependencyTest(false);
	}

	/**
	 * Ensure dependency available to {@link Filter} when chaining
	 * {@link ServletManager}.
	 */
	public void testChainFilterDependency() throws Exception {
		this.doChainServletDependencyTest(true);
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
	 * @param isWithFilter Indicates if test {@link Filter}.
	 */
	private void doChainServletDependencyTest(boolean isWithFilter) throws Exception {
		CompileWoof woof = new CompileWoof();
		woof.woof((context) -> new ServletWoofExtensionService().extend(context));
		woof.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			office.addSupplier("DEPENDENCY", new SetupSupplierSource(isWithFilter));
			Singleton.load(office, new ServletDependency());
		});
		try (MockWoofServer server = woof.open()) {
			MockWoofResponse response = server.send(MockWoofServer.mockRequest("/dependency"));
			response.assertResponse(200, (isWithFilter ? "Filter" : "Servlet") + " dependency via ServletManager");
		}
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

		/*
		 * =================== HttpServlet =====================
		 */

		@Override
		public void init() throws ServletException {

			// Obtain the servlet manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();

			// Obtain the dependency
			this.dependency = servletManager.getDependency(null, ServletDependency.class);
		}

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write("Servlet dependency " + this.dependency.getMessage());
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

		/*
		 * =================== HttpFilter =====================
		 */

		@Override
		public void init() throws ServletException {

			// Obtain the servlet manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();

			// Obtain the dependency
			this.dependency = servletManager.getDependency(null, ServletDependency.class);
		}

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			response.getWriter().write("Filter dependency " + this.dependency.getMessage());
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