package net.officefloor.spring.webflux.procedure;

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
import net.officefloor.web.state.HttpRequestState;

/**
 * Web Flux {@link Controller} {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebFluxProcedureSource implements ManagedFunctionProcedureSource, ProcedureSourceServiceFactory {

	/**
	 * {@link SpringWebFluxProcedureSource} source name.
	 */
	public static final String SOURCE_NAME = "SpringFlux" + Controller.class.getSimpleName();

	/*
	 * ====================== ProcedureSourceServiceFactory ======================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== ManagedFunctionProcedureSource =====================
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

		// Create the procedure
		SpringWebFluxProcedure procedure = new SpringWebFluxProcedure(controllerClass, methodName);

		// Determine if register the procedure
		if (!sourceContext.isLoadingType()) {
			SpringWebFluxProcedureRegistry.registerSpringControllerProcedure(procedure);
		}

		// Provide managed function
		ManagedFunctionTypeBuilder<SpringWebFluxProcedure.DependencyKeys, None> servlet = context
				.setManagedFunction(procedure, SpringWebFluxProcedure.DependencyKeys.class, None.class);
		servlet.addObject(ServerHttpConnection.class)
				.setKey(SpringWebFluxProcedure.DependencyKeys.SERVER_HTTP_CONNECTION);
		servlet.addObject(HttpRequestState.class).setKey(SpringWebFluxProcedure.DependencyKeys.HTTP_REQUEST_STATE);
	}

}