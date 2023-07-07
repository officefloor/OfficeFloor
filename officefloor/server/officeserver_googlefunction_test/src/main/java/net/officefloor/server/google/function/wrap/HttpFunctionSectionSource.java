package net.officefloor.server.google.function.wrap;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Section to execute the {@link HttpFunction}.
 */
public class HttpFunctionSectionSource extends AbstractSectionSource {

	/**
	 * {@link DeployedOfficeInput} section name.
	 */
	public static final String SECTION_NAME = "_googgle_http_function_";

	/**
	 * {@link SectionInput} name.
	 */
	public static final String INPUT_NAME = "service";

	/**
	 * Type qualifier for the {@link ServerHttpConnection}.
	 */
	public static final String CONNECTION_TYPE_QUALIFIER = "_google_http_function_connection_";

	/**
	 * {@link HttpFunction} {@link Class}.
	 */
	private final Class<?> httpFunctionClass;

	/**
	 * Instantiate.
	 * 
	 * @param httpFunctionClass {@link HttpFunction} {@link Class}.
	 */
	public HttpFunctionSectionSource(Class<?> httpFunctionClass) {
		this.httpFunctionClass = httpFunctionClass;
	}

	/*
	 * =================== SectionSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Provide servicing by HTTP Function
		SectionFunction httpFunction = designer
				.addSectionFunctionNamespace("HTTP_FUNCTION", new HttpFunctionManagedFunctionSource())
				.addSectionFunction("httpFunction", HttpFunctionManagedFunctionSource.HTTP_FUNCTION_NAME);
		designer.link(designer.addSectionInput(INPUT_NAME, null), httpFunction);
		SectionObject serverHttpConnection = designer.addSectionObject(DependencyKey.SERVER_HTTP_CONNECTION.name(),
				ServerHttpConnection.class.getName());
		serverHttpConnection.setTypeQualifier(CONNECTION_TYPE_QUALIFIER);
		designer.link(httpFunction.getFunctionObject(DependencyKey.SERVER_HTTP_CONNECTION.name()),
				serverHttpConnection);
	}

	/**
	 * Keys for dependencies.
	 */
	public static enum DependencyKey {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link ManagedFunctionSource} to execute the {@link HttpFunction}.
	 */
	private class HttpFunctionManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * Name of the {@link HttpFunction} {@link ManagedFunction}.
		 */
		private static final String HTTP_FUNCTION_NAME = "function";

		/*
		 * ================= ManagedFunctionSource ================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder builder, ManagedFunctionSourceContext context)
				throws Exception {

			// Add function to execute the HTTP Function
			ManagedFunctionTypeBuilder<DependencyKey, None> httpFunctionBuilder = builder
					.addManagedFunctionType(HTTP_FUNCTION_NAME, DependencyKey.class, None.class);
			httpFunctionBuilder.addObject(ServerHttpConnection.class).setTypeQualifier(CONNECTION_TYPE_QUALIFIER)
					.setKey(DependencyKey.SERVER_HTTP_CONNECTION);
			httpFunctionBuilder.setFunctionFactory(() -> (functionContext) -> {

				// Obtain the connection
				ServerHttpConnection connection = (ServerHttpConnection) functionContext
						.getObject(DependencyKey.SERVER_HTTP_CONNECTION);

				// Translate the HTTP request and response
				HttpRequest request = new HttpFunctionHttpRequest(connection.getRequest());
				HttpFunctionHttpResponse response = new HttpFunctionHttpResponse(connection.getResponse());

				// Undertake the HTTP Function
				HttpFunction httpFunction = (HttpFunction) HttpFunctionSectionSource.this.httpFunctionClass
						.getConstructor().newInstance();
				httpFunction.service(request, response);

				// Send the response
				response.send();
			});
		}
	}

}