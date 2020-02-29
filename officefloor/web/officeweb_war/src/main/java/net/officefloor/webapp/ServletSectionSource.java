package net.officefloor.webapp;

import javax.servlet.Servlet;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.ServletServicer;

/**
 * {@link SectionSource} servicing {@link ServerHttpConnection} via
 * {@link Servlet}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletSectionSource extends AbstractSectionSource {

	/**
	 * {@link SectionInput} name for servicing the {@link ServerHttpConnection}.
	 */
	public static final String INPUT = "serviceByServlet";

	/**
	 * {@link SectionOutput} for passing on to next in chain for servicing
	 * {@link ServerHttpConnection}.
	 */
	public static final String OUTPUT = "notServiced";

	/**
	 * Name of service {@link ManagedFunction}.
	 */
	private static final String FUNCTION = "service";

	/*
	 * ========================= SectionSource ==============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Configure in servicing
		SectionFunction service = designer.addSectionFunctionNamespace(FUNCTION, new ServletManagedFunctionSource())
				.addSectionFunction(FUNCTION, FUNCTION);

		// Link for use
		designer.link(designer.addSectionInput(INPUT, null), service);
		designer.link(service.getFunctionFlow(FlowKeys.NOT_FOUND.name()),
				designer.addSectionOutput(OUTPUT, null, false), false);

		// Provide dependencies
		designer.link(service.getFunctionObject(DependencyKeys.SERVLET_SERVICER.name()),
				designer.addSectionObject(ServletServicer.class.getSimpleName(), ServletServicer.class.getName()));
		designer.link(service.getFunctionObject(DependencyKeys.SERVER_HTTP_CONNECTION.name()), designer
				.addSectionObject(ServerHttpConnection.class.getSimpleName(), ServerHttpConnection.class.getName()));
	}

	/**
	 * Dependency keys.
	 */
	private static enum DependencyKeys {
		SERVLET_SERVICER, SERVER_HTTP_CONNECTION
	}

	/**
	 * Flow keys.
	 */
	private static enum FlowKeys {
		NOT_FOUND
	}

	/**
	 * {@link ManagedFunctionSource} for {@link ServletServicerFunction}.
	 */
	private static class ServletManagedFunctionSource extends AbstractManagedFunctionSource {

		/*
		 * ===================== ManagedFunctionSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Provide service function
			ManagedFunctionTypeBuilder<DependencyKeys, FlowKeys> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(FUNCTION, new ServletServicerFunction(), DependencyKeys.class,
							FlowKeys.class);
			function.addObject(ServletServicer.class).setKey(DependencyKeys.SERVLET_SERVICER);
			function.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);
			function.addFlow().setKey(FlowKeys.NOT_FOUND);
		}
	}

	/**
	 * {@link ServletServicer} {@link ManagedFunction}.
	 */
	private static class ServletServicerFunction extends StaticManagedFunction<DependencyKeys, FlowKeys> {

		/*
		 * ======================== ManagedFunction =========================
		 */

		@Override
		public void execute(ManagedFunctionContext<DependencyKeys, FlowKeys> context) throws Throwable {

			// Obtain dependencies
			ServletServicer servicer = (ServletServicer) context.getObject(DependencyKeys.SERVLET_SERVICER);
			ServerHttpConnection connection = (ServerHttpConnection) context
					.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);

			// Undertake servicing
			servicer.service(connection);

			// Determine if not serviced
			HttpResponse response = connection.getResponse();
			if (HttpStatus.NOT_FOUND.equals(response.getStatus())) {

				// Reset and attempt further handling in chain
				response.reset();
				context.doFlow(FlowKeys.NOT_FOUND, null, null);
			}
		}
	}

}