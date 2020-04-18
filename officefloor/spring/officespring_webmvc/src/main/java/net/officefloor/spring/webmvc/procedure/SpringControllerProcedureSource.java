package net.officefloor.spring.webmvc.procedure;

import org.springframework.stereotype.Controller;

import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * {@link Controller} {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringControllerProcedureSource implements ManagedFunctionProcedureSource, ProcedureSourceServiceFactory {

	/**
	 * {@link SpringControllerProcedureSource} source name.
	 */
	public static final String SOURCE_NAME = "Spring" + Controller.class.getSimpleName();

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
		SourceContext sourceContext = context.getSourceContext();

		// Obtain the Controller class and method
		Class<?> controllerClass = sourceContext.loadClass(context.getResource());
		String methodName = context.getProcedureName();

		// Determine if loading type
		ServletServicer servletServicer = null;
		if (!sourceContext.isLoadingType()) {

			// Obtain the Servlet Manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();

			// Add the dispatcher Servlet
			String servletName = sourceContext.getName();
			servletServicer = servletManager.addServlet(servletName, ProcedureDispatcherServlet.class);
		}

		// Create the procedure
		SpringControllerProcedure procedure = new SpringControllerProcedure(servletServicer, controllerClass,
				methodName);

		// Determine if register the procedure
		if (!sourceContext.isLoadingType()) {
			SpringControllerProcedureRegistry.registerSpringControllerProcedure(procedure);
		}

		// Provide managed function
		ManagedFunctionTypeBuilder<SpringControllerProcedure.DependencyKeys, None> servlet = context
				.setManagedFunction(procedure, SpringControllerProcedure.DependencyKeys.class, None.class);
		servlet.addObject(ServerHttpConnection.class)
				.setKey(SpringControllerProcedure.DependencyKeys.SERVER_HTTP_CONNECTION);

		// Must depend on following for thread locals to be available
		servlet.addObject(ServletServicer.class).setKey(SpringControllerProcedure.DependencyKeys.SERVLET_SERVICER);
	}

}