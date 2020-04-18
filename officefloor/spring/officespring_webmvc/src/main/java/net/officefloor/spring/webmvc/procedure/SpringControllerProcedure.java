package net.officefloor.spring.webmvc.procedure;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Executor;

import org.springframework.stereotype.Controller;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.ServletServicer;

/**
 * Spring {@link Controller} {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringControllerProcedure extends StaticManagedFunction<SpringControllerProcedure.DependencyKeys, None> {

	/**
	 * Dependency keys.
	 */
	public static enum DependencyKeys {
		SERVER_HTTP_CONNECTION, SERVLET_SERVICER
	}

	/**
	 * {@link ServletServicer} for the {@link ProcedureProxyServlet}.
	 */
	private final ServletServicer servletServicer;

	/**
	 * {@link Controller} {@link Class}.
	 */
	protected final Class<?> controllerClass;

	/**
	 * Name of {@link Controller} {@link Method}.
	 */
	protected final String controllerMethodName;

	/**
	 * Attributes for {@link ServletServicer}. Provided by
	 * {@link SpringControllerProcedureRegistry}.
	 */
	protected Map<String, Object> attributes;

	/**
	 * Instantiate.
	 * 
	 * @param servletServicer      {@link ServletServicer} for the
	 *                             {@link ProcedureProxyServlet}.
	 * @param controllerClass      {@link Controller} {@link Class}.
	 * @param controllerMethodName Name of {@link Controller} {@link Method}.
	 */
	public SpringControllerProcedure(ServletServicer servletServicer, Class<?> controllerClass,
			String controllerMethodName) {
		this.servletServicer = servletServicer;
		this.controllerClass = controllerClass;
		this.controllerMethodName = controllerMethodName;
	}

	/*
	 * ======================= ManagedFucntion ============================
	 */

	@Override
	public void execute(ManagedFunctionContext<DependencyKeys, None> context) throws Throwable {

		// Obtain dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);

		// Service
		AsynchronousFlow asynchronousFlow = context.createAsynchronousFlow();
		Executor executor = context.getExecutor();
		this.servletServicer.service(connection, asynchronousFlow, executor, this.attributes);
	}

}