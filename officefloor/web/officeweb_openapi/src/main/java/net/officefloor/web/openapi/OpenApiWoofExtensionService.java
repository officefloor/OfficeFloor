package net.officefloor.web.openapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.build.HttpInputExplorerContext;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.openapi.operation.DefaultOpenApiOperationBuilder;
import net.officefloor.web.openapi.operation.OpenApiOperationBuilder;
import net.officefloor.web.openapi.operation.OpenApiOperationExtension;
import net.officefloor.web.openapi.operation.OpenApiOperationExtensionServiceFactory;
import net.officefloor.web.openapi.operation.OpenApiOperationFunctionContext;
import net.officefloor.web.openapi.section.OpenApiSectionSource;
import net.officefloor.web.openapi.security.OpenApiSecurityExtension;
import net.officefloor.web.openapi.security.OpenApiSecurityExtensionContext;
import net.officefloor.web.openapi.security.OpenApiSecurityExtensionServiceFactory;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityExplorerContext;
import net.officefloor.woof.WoofContext;
import net.officefloor.woof.WoofExtensionService;
import net.officefloor.woof.WoofExtensionServiceFactory;

/**
 * {@link WoofExtensionService} to configure OpenAPI specification.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenApiWoofExtensionService implements WoofExtensionService, WoofExtensionServiceFactory {

	/**
	 * Obtains or adds the {@link Components} from the {@link OpenAPI}
	 * 
	 * @param openApi {@link OpenAPI}.
	 * @return {@link Components}.
	 */
	private static Components getComponents(OpenAPI openApi) {
		Components components = openApi.getComponents();
		if (components == null) {
			components = new Components();
			openApi.setComponents(components);
		}
		return components;
	}

	/*
	 * ================== WoofExtensionService ====================
	 */

	@Override
	public WoofExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extend(WoofContext context) throws Exception {

		final String JSON_PATH = "/openapi.json";
		final String YAML_PATH = "/openapi.yaml";
		final Set<String> SELF_REGISTERED_PATHS = new HashSet<>(Arrays.asList(JSON_PATH, YAML_PATH));

		// Obtain the web architect
		WebArchitect web = context.getWebArchitect();

		// Create the root
		OpenAPI openApi = new OpenAPI();

		// Load the security extensions
		List<OpenApiSecurityExtension> securityExtensions = new ArrayList<>();
		for (OpenApiSecurityExtension extension : context.getOfficeExtensionContext()
				.loadOptionalServices(OpenApiSecurityExtensionServiceFactory.class)) {
			securityExtensions.add(extension);
		}

		// Load the operation extensions
		List<OpenApiOperationExtension> operationExtensions = new ArrayList<>();
		for (OpenApiOperationExtension extension : context.getOfficeExtensionContext()
				.loadOptionalServices(OpenApiOperationExtensionServiceFactory.class)) {
			operationExtensions.add(extension);
		}

		// Explore HTTP security loading security schemes
		HttpSecurityArchitect security = context.getHttpSecurityArchitect();
		Set<String> allHttpSecurityNames = new HashSet<>();
		security.addHttpSecurityExplorer((explore) -> {
			for (OpenApiSecurityExtension extension : securityExtensions) {
				extension.extend(new OpenApiSecurityExtensionContext() {

					@Override
					public HttpSecurityExplorerContext getHttpSecurity() {
						return explore;
					}

					@Override
					public void addSecurityScheme(String securityName, SecurityScheme scheme) {
						getComponents(openApi).addSecuritySchemes(securityName, scheme);
						allHttpSecurityNames.add(securityName);
					}

					@Override
					public void addOperationExtension(OpenApiOperationExtension extension) {
						operationExtensions.add(extension);
					}
				});
			}
		});

		// Listing of operation builders
		List<OperationBuilder> operationBuilders = new ArrayList<>();

		// Provide the paths
		Paths paths = new Paths();
		openApi.setPaths(paths);
		web.addHttpInputExplorer((explore) -> {

			// Ignore self registered paths
			if (SELF_REGISTERED_PATHS.contains(explore.getRoutePath())) {
				return;
			}

			// Lazy create the path
			String applicationPath = explore.getApplicationPath();
			PathItem path = paths.get(applicationPath);
			if (path == null) {
				path = new PathItem();
				paths.put(applicationPath, path);
			}

			// Add the operation
			Operation operation = new Operation();
			switch (explore.getHttpMethod().getName()) {
			case "GET":
				path.setGet(operation);
				break;
			case "POST":
				path.setPost(operation);
				break;
			case "PUT":
				path.setPut(operation);
				break;
			case "DELETE":
				path.setDelete(operation);
				break;
			case "HEAD":
				path.setHead(operation);
				break;
			case "OPTIONS":
				path.setOptions(operation);
				break;
			case "PATCH":
				path.setPatch(operation);
				break;
			case "TRACE":
				path.setTrace(operation);
				break;
			default:
				// Ignore method
				return;
			}

			// Obtain all the HTTP security names
			String[] httpSecurityNames = allHttpSecurityNames.toArray(new String[allHttpSecurityNames.size()]);
			Arrays.sort(httpSecurityNames, String.CASE_INSENSITIVE_ORDER);

			// Create the builder context
			OpenApiOperationContextImpl builderContext = new OpenApiOperationContextImpl(explore, openApi, path,
					operation, httpSecurityNames);

			// Create the default operation builder
			DefaultOpenApiOperationBuilder defaultOperationBuilder = new DefaultOpenApiOperationBuilder(builderContext);

			// Create the builders (default always first)
			List<OpenApiOperationBuilder> builders = new ArrayList<>();
			builders.add(defaultOperationBuilder);
			for (OpenApiOperationExtension extension : operationExtensions) {
				OpenApiOperationBuilder builder = extension.createBuilder(builderContext);
				if (builder != null) {
					builders.add(builder);
				}
			}

			// Create and register the operation builder
			OperationBuilder operationBuilder = new OperationBuilder(defaultOperationBuilder, builderContext,
					builders.toArray(new OpenApiOperationBuilder[builders.size()]));
			operationBuilders.add(operationBuilder);

			// Recursive explore graph, loading path specification
			ExecutionManagedFunction managedFunction = explore.getInitialManagedFunction();
			this.recursiveLoadManagedFunction(managedFunction, operationBuilder, new HashSet<>());
		});

		// Provide additional exception handling information
		OfficeArchitect office = context.getOfficeArchitect();
		office.addOfficeEscalationExplorer((explore) -> {

			// Obtain the escalation class
			String escalationType = explore.getOfficeEscalationType();
			Class<?> escalationClass = context.getOfficeExtensionContext().loadClass(escalationType);

			// Determine if operation builder requires escalation
			for (OperationBuilder operationBuilder : operationBuilders) {

				// Determine if load escalation
				boolean isLoadEscalation = false;
				if ((RuntimeException.class.isAssignableFrom(escalationClass))
						|| (Error.class.isAssignableFrom(escalationClass))) {
					isLoadEscalation = true; // unchecked, so always include
				} else {
					// Determine if unhandled escalation
					INCLUDED: for (Class<?> unhandledEscalationType : operationBuilder.defaultOperationBuilder
							.getUnhandledEsclationTypes()) {
						if (escalationClass.isAssignableFrom(unhandledEscalationType)) {
							isLoadEscalation = true;
							break INCLUDED;
						}
					}
				}

				// Load escalation (if include)
				if (isLoadEscalation) {
					ExecutionManagedFunction managedFunction = explore.getInitialManagedFunction();
					this.recursiveLoadManagedFunction(managedFunction, operationBuilder, new HashSet<>());
				}
			}
		});

		// Provide completion
		office.addOfficeCompletionExplorer(() -> {
			for (OperationBuilder operationBuilder : operationBuilders) {
				for (OpenApiOperationBuilder builder : operationBuilder.builders) {
					builder.buildComplete(operationBuilder.builderContext);
				}
			}
		});

		// Serve up the Open API
		OfficeSection service = office.addOfficeSection("OPEN_API", new OpenApiSectionSource(openApi), null);

		// Serve the JSON
		office.link(web.getHttpInput(false, JSON_PATH).getInput(),
				service.getOfficeSectionInput(OpenApiSectionSource.JSON));

		// Serve the YAML
		office.link(web.getHttpInput(false, YAML_PATH).getInput(),
				service.getOfficeSectionInput(OpenApiSectionSource.YAML));
	}

	/**
	 * Recursively loads the {@link ExecutionManagedFunction} graph.
	 * 
	 * @param managedFunction Root {@link ExecutionManagedFunction}.
	 * @param builders        {@link OpenApiOperationBuilder} instances.
	 * @param builderContext  {@link OpenApiOperationContextImpl}.
	 * @param visited         Names of the visited {@link ExecutionManagedFunction}
	 *                        instances.
	 * @throws Exception If fails to load graph.
	 */
	private void recursiveLoadManagedFunction(ExecutionManagedFunction managedFunction,
			OperationBuilder operationBuilder, Set<String> visited) throws Exception {

		// Determine if already visited
		String name = managedFunction.getManagedFunctionName();
		if (visited.contains(name)) {
			return; // already visited
		}
		visited.add(name); // now visiting

		// Load the managed function
		operationBuilder.buildInManagedObject(managedFunction);

		// Recursively explore rest of graph
		ManagedFunctionType<?, ?> type = managedFunction.getManagedFunctionType();
		for (ManagedFunctionFlowType<?> flowType : type.getFlowTypes()) {
			ExecutionManagedFunction flowManagedFunction = managedFunction.getManagedFunction(flowType);
			this.recursiveLoadManagedFunction(flowManagedFunction, operationBuilder, visited);
		}
		ExecutionManagedFunction nextManagedFunction = managedFunction.getNextManagedFunction();
		if (nextManagedFunction != null) {
			this.recursiveLoadManagedFunction(nextManagedFunction, operationBuilder, visited);
		}
		for (ManagedFunctionEscalationType escalationType : type.getEscalationTypes()) {
			ExecutionManagedFunction escalateManagedFunction = managedFunction.getManagedFunction(escalationType);
			if (escalateManagedFunction != null) {
				this.recursiveLoadManagedFunction(escalateManagedFunction, operationBuilder, visited);
			}
		}
	}

	/**
	 * Builds the {@link Operation}.
	 */
	private static class OperationBuilder {

		/**
		 * Default {@link OpenApiOperationBuilder}.
		 */
		private final DefaultOpenApiOperationBuilder defaultOperationBuilder;

		/**
		 * {@link OpenApiOperationContextImpl}.
		 */
		private final OpenApiOperationContextImpl builderContext;

		/**
		 * {@link OpenApiOperationBuilder}
		 */
		private final OpenApiOperationBuilder[] builders;

		/**
		 * Initiate.
		 * 
		 * @param defaultOperationBuilder {@link DefaultOpenApiOperationBuilder}.
		 * @param builderContext          {@link OpenApiOperationContextImpl}.
		 * @param builders                {@link OpenApiOperationBuilder} instances.
		 */
		private OperationBuilder(DefaultOpenApiOperationBuilder defaultOperationBuilder,
				OpenApiOperationContextImpl builderContext, OpenApiOperationBuilder[] builders) {
			this.defaultOperationBuilder = defaultOperationBuilder;
			this.builderContext = builderContext;
			this.builders = builders;
		}

		/**
		 * Builds in the {@link ExecutionManagedFunction}.
		 * 
		 * @param managedFunction {@link ExecutionManagedFunction}.
		 * @throws Exception If fails to build in {@link ExecutionManagedFunction}.
		 */
		private void buildInManagedObject(ExecutionManagedFunction managedFunction) throws Exception {
			builderContext.managedFunction = managedFunction;
			for (OpenApiOperationBuilder builder : builders) {
				builder.buildInManagedFunction(builderContext);
			}
		}
	}

	/**
	 * {@link OpenApiOperationFunctionContext} implementation.
	 */
	private static class OpenApiOperationContextImpl implements OpenApiOperationFunctionContext {

		/**
		 * {@link HttpInputExplorerContext}.
		 */
		private final HttpInputExplorerContext httpInput;

		/**
		 * {@link OpenAPI}.
		 */
		private final OpenAPI openApi;

		/**
		 * {@link PathItem}.
		 */
		private final PathItem path;

		/**
		 * {@link Operation}.
		 */
		private final Operation operation;

		/**
		 * All security names.
		 */
		private final String[] allSecurityNames;

		/**
		 * {@link ExecutionManagedFunction}.
		 */
		private ExecutionManagedFunction managedFunction = null;

		/**
		 * Instantiate.
		 * 
		 * @param openApi          {@link OpenAPI}.
		 * @param httpInput        {@link HttpInputExplorerContext}.
		 * @param path             {@link PathItem}.
		 * @param operation        {@link Operation}.
		 * @param allSecurityNames All security names.
		 */
		private OpenApiOperationContextImpl(HttpInputExplorerContext httpInput, OpenAPI openApi, PathItem path,
				Operation operation, String[] allSecurityNames) {
			this.httpInput = httpInput;
			this.openApi = openApi;
			this.path = path;
			this.operation = operation;
			this.allSecurityNames = allSecurityNames;
		}

		/*
		 * ============== OpenApiOperationFunctionContext =================
		 */

		@Override
		public ExecutionManagedFunction getManagedFunction() {
			return this.managedFunction;
		}

		/*
		 * ================== OpenApiOperationContext ===================
		 */

		@Override
		public HttpInputExplorerContext getHttpInput() {
			return this.httpInput;
		}

		@Override
		public PathItem getPath() {
			return this.path;
		}

		@Override
		public Operation getOperation() {
			return this.operation;
		}

		@Override
		public Parameter getParameter(String name) {

			// Ensure have parameters
			List<Parameter> parameters = this.operation.getParameters();
			if (parameters == null) {
				parameters = new ArrayList<>();
				this.operation.setParameters(parameters);
			}

			// Search for the parameter
			for (Parameter parameter : parameters) {
				if (name.equals(parameter.getName())) {
					return parameter; // found parameter
				}
			}

			// As here, parameter not exist
			return null;
		}

		@Override
		public SecurityRequirement getOrAddSecurityRequirement(String securityName) {

			// Ensure have security list
			List<SecurityRequirement> requirements = this.operation.getSecurity();
			if (requirements == null) {
				requirements = new ArrayList<>();
				this.operation.setSecurity(requirements);
			}

			// Search for requirement
			for (SecurityRequirement requirement : requirements) {
				if (requirement.containsKey(securityName)) {
					return requirement;
				}
			}

			// As here, security requirement not exist, so add and return
			SecurityRequirement requirement = new SecurityRequirement();
			requirement.put(securityName, new ArrayList<>());
			this.operation.addSecurityItem(requirement);
			return requirement;
		}

		@Override
		public Components getComponents() {
			return OpenApiWoofExtensionService.getComponents(this.openApi);
		}

		@Override
		public String[] getAllSecurityNames() {
			return this.allSecurityNames;
		}
	}

}