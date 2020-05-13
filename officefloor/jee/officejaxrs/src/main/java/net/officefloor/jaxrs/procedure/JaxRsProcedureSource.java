package net.officefloor.jaxrs.procedure;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.servlet.ServletContainer;

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
 * JAX-RS {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsProcedureSource implements ManagedFunctionProcedureSource, ProcedureSourceServiceFactory {

	/**
	 * Source name for {@link JaxRsProcedureSource}.
	 */
	public static final String SOURCE_NAME = "JAXRS";

	/*
	 * ================== ProcedureSourceServiceFactory ===================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================= ManagedFunctionProcedureSource ==================
	 */

	@Override
	public String getSourceName() {
		return SOURCE_NAME;
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {
		// TODO Implement ProcedureSource.listProcedures
		throw new IllegalStateException("TODO implement ProcedureSource.listProcedures");
	}

	@Override
	public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {
		SourceContext sourceContext = context.getSourceContext();

		// Obtain details
		Class<?> resourceClass = sourceContext.loadClass(context.getResource());
		String resourceMethodName = context.getProcedureName();

		// Find method on resource class
		Method resourceMethod = null;
		for (Method method : resourceClass.getMethods()) {
			if (method.getName().equals(resourceMethodName)) {
				resourceMethod = method;
			}
		}
		if (resourceMethod == null) {
			throw new IllegalStateException("Can not find method " + resourceMethodName + " on JAX-RS resource class "
					+ resourceClass.getName());
		}

		// Build the JAX-RS method to service request
		Resource.Builder resourceBuilder = Resource.builder().path("/");
		resourceBuilder.addMethod().handledBy(resourceClass, resourceMethod).build();
		Resource resource = resourceBuilder.build();

		// Create Servlet for JAX-RS method
		ResourceConfig config = new ResourceConfig();
		config.registerResources(resource);
		ServletContainer container = new ServletContainer(config) {
			private static final long serialVersionUID = 1L;

			@Override
			public Value<Integer> service(URI baseUri, URI requestUri, HttpServletRequest request,
					HttpServletResponse response) throws ServletException, IOException {

				// Replace request URI with path
				URI overrideRequestUri;
				try {
					overrideRequestUri = new URI(requestUri.getScheme(), requestUri.getHost(), "/",
							requestUri.getQuery(), requestUri.getFragment());
				} catch (URISyntaxException ex) {
					throw new ServletException(ex);
				}

				// Undertake servicing
				return super.service(baseUri, overrideRequestUri, request, response);
			}
		};

		// Determine if loading type
		ServletServicer servletServicer = null;
		if (!sourceContext.isLoadingType()) {

			// Obtain the Servlet Manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();

			// Add the Servlet
			String servletName = sourceContext.getName();
			servletServicer = servletManager.addServlet(servletName, container, true, null);
		}

		// Provide managed function
		ManagedFunctionTypeBuilder<DependencyKeys, None> servlet = context
				.setManagedFunction(new JaxRsProcedure(servletServicer), DependencyKeys.class, None.class);
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
	 * JAX-RS {@link Procedure}.
	 */
	private class JaxRsProcedure extends StaticManagedFunction<DependencyKeys, None> {

		/**
		 * {@link ServletServicer} for the {@link Servlet}.
		 */
		private final ServletServicer servletServicer;

		/**
		 * Instantiate.
		 * 
		 * @param servletServicer {@link ServletServicer}.
		 */
		private JaxRsProcedure(ServletServicer servletServicer) {
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
			this.servletServicer.service(connection, executor, asynchronousFlow, null, null);
		}
	}

}