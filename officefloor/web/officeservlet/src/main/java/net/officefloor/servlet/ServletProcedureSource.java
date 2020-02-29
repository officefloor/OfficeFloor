package net.officefloor.servlet;

import javax.servlet.Servlet;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.ServerHttpConnection;

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
		// TODO implement ProcedureSource.listProcedures
		throw new UnsupportedOperationException("TODO implement ProcedureSource.listProcedures");
	}

	@Override
	public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {

		// Obtain the Servlet class
		@SuppressWarnings("unchecked")
		Class<? extends Servlet> servletClass = (Class<? extends Servlet>) context.getSourceContext()
				.loadClass(context.getResource());

		// Provide managed function
		ManagedFunctionTypeBuilder<DependencyKeys, None> servlet = context
				.setManagedFunction(new ServletProcedure(servletClass), DependencyKeys.class, None.class);
		servlet.addObject(ServletManager.class).setKey(DependencyKeys.SERVLET_MANAGER);
		servlet.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);
	}

	/**
	 * Dependency keys.
	 */
	private static enum DependencyKeys {
		SERVLET_MANAGER, SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link Servlet} {@link Procedure}.
	 */
	private static class ServletProcedure extends StaticManagedFunction<DependencyKeys, None> {

		/**
		 * {@link Servlet} {@link Class}.
		 */
		private final Class<? extends Servlet> servletClass;

		/**
		 * {@link ServletServicer} for the {@link Servlet}.
		 */
		private volatile ServletServicer servletServicer;

		/**
		 * Instantiate.
		 * 
		 * @param servletClass {@link Servlet} {@link Class}.
		 */
		private ServletProcedure(Class<? extends Servlet> servletClass) {
			this.servletClass = servletClass;
		}

		/*
		 * ======================= ManagedFunction =================================
		 */

		@Override
		public void execute(ManagedFunctionContext<DependencyKeys, None> context) throws Throwable {

			// Obtain dependencies
			ServletManager servletManager = (ServletManager) context.getObject(DependencyKeys.SERVLET_MANAGER);
			ServerHttpConnection connection = (ServerHttpConnection) context
					.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);

			// Obtain the servicer
			ServletServicer servicer = this.servletServicer;
			if (servicer == null) {

				// Ensure only single instance registered
				synchronized (servletManager) {

					// Check not created by another thread
					servicer = this.servletServicer;
					if (servicer == null) {

						// Add the servlet
						String servletName = context.getLogger().getName();
						servicer = servletManager.addServlet(servletName, this.servletClass);

						// Register for re-use
						this.servletServicer = servicer;
					}
				}
			}

			// Service
			servicer.service(connection);
		}
	}

}