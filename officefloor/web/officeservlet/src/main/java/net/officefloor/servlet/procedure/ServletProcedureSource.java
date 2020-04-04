package net.officefloor.servlet.procedure;

import java.util.concurrent.Executor;

import javax.servlet.Servlet;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * {@link Servlet} {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletProcedureSource implements ManagedFunctionProcedureSource, ProcedureSourceServiceFactory {

	/**
	 * {@link ServletProcedureSource} source name.
	 */
	public static final String SOURCE_NAME = Servlet.class.getSimpleName();

	/*
	 * ===================== ProcedureSourceServiceFactory ========================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ============================ ProcedureSource ===============================
	 */

	@Override
	public String getSourceName() {
		return SOURCE_NAME;
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {

		// Determine if servlet
		Class<?> clazz = context.getSourceContext().loadOptionalClass(context.getResource());
		if ((clazz != null) && (Servlet.class.isAssignableFrom(clazz))) {

			// Servlet so list the procedure
			context.addProcedure(clazz.getSimpleName());
		}
	}

	@Override
	public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {
		SourceContext sourceContext = context.getSourceContext();

		// Obtain the Servlet class
		@SuppressWarnings("unchecked")
		Class<? extends Servlet> servletClass = (Class<? extends Servlet>) sourceContext
				.loadClass(context.getResource());

		// Obtain the Servlet Manager
		ServletManager servletManager = ServletSupplierSource.getServletManager();

		// Add the Servlet
		String servletName = sourceContext.getName();
		ServletServicer servletServicer = servletManager.addServlet(servletName, servletClass);

		// Provide managed function
		ManagedFunctionTypeBuilder<DependencyKeys, None> servlet = context
				.setManagedFunction(new ServletProcedure(servletServicer), DependencyKeys.class, None.class);
		servlet.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);

		// Must depend on servlet servicer for thread locals to be available
		servlet.addObject(ServletServicer.class).setKey(DependencyKeys.SERVLET_SERVICER);
	}

	/**
	 * Dependency keys.
	 */
	private static enum DependencyKeys {
		SERVER_HTTP_CONNECTION, SERVLET_SERVICER
	}

	/**
	 * {@link Servlet} {@link Procedure}.
	 */
	private class ServletProcedure extends StaticManagedFunction<DependencyKeys, None> {

		/**
		 * {@link ServletServicer} for the {@link Servlet}.
		 */
		private final ServletServicer servletServicer;

		/**
		 * Instantiate.
		 * 
		 * @param servletServicer {@link ServletServicer}.
		 */
		private ServletProcedure(ServletServicer servletServicer) {
			this.servletServicer = servletServicer;
		}

		/*
		 * ======================= ManagedFunction =================================
		 */

		@Override
		public void execute(ManagedFunctionContext<DependencyKeys, None> context) throws Throwable {

			// Obtain dependencies
			ServerHttpConnection connection = (ServerHttpConnection) context
					.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);

			// Service
			AsynchronousFlow asynchronousFlow = context.createAsynchronousFlow();
			Executor executor = context.getExecutor();
			this.servletServicer.service(connection, asynchronousFlow, executor);
		}
	}

}