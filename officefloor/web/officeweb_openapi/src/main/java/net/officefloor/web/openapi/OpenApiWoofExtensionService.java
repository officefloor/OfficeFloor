package net.officefloor.web.openapi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpCookieParameter;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.value.load.ValueLoaderFactory;
import net.officefloor.web.value.load.ValueLoaderSource;
import net.officefloor.web.value.load.ValueName;
import net.officefloor.woof.WoofContext;
import net.officefloor.woof.WoofExtensionService;

/**
 * {@link WoofExtensionService} to configure OpenAPI specification.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenApiWoofExtensionService implements WoofExtensionService {

	/**
	 * Name to link the JSON servicing.
	 */
	private static final String JSON = "JSON";

	/**
	 * Name to link the YAML servicing.
	 */
	private static final String YAML = "YAML";

	/*
	 * ================== WoofExtensionService ====================
	 */

	@Override
	public void extend(WoofContext context) throws Exception {

		final String JSON_PATH = "/openapi.json";
		final String YAML_PATH = "/openapi.yaml";
		final Set<String> SELF_REGISTERED_PATHS = new HashSet<>(Arrays.asList(JSON_PATH, YAML_PATH));

		// Obtain the web architect
		WebArchitect web = context.getWebArchitect();

		// Provide configuration of Open API
		OpenAPI openApi = new OpenAPI();
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
			}

			// Determine the parameters
			Map<String, Parameter> parameters = new HashMap<>();
			ExecutionManagedFunction managedFunction = explore.getInitialManagedFunction();
			ManagedFunctionType<?, ?> type = managedFunction.getManagedFunctionType();
			for (ManagedFunctionObjectType<?> objectType : type.getObjectTypes()) {

				// Include possible path parameter
				HttpPathParameter pathParam = objectType.getAnnotation(HttpPathParameter.class);
				if (pathParam != null) {
					String paramName = pathParam.value();
					this.addParameter(paramName, () -> new PathParameter().name(paramName), objectType, operation,
							parameters);
				}

				// Include possible query parameter
				HttpQueryParameter queryParam = objectType.getAnnotation(HttpQueryParameter.class);
				if (queryParam != null) {
					String paramName = queryParam.value();
					this.addParameter(paramName, () -> new QueryParameter().name(paramName), objectType, operation,
							parameters);
				}

				// Include possible header parameter
				HttpHeaderParameter headerParam = objectType.getAnnotation(HttpHeaderParameter.class);
				if (headerParam != null) {
					String paramName = headerParam.value();
					this.addParameter(paramName, () -> new HeaderParameter().name(paramName), objectType, operation,
							parameters);
				}

				// Include possible cookie parameter
				HttpCookieParameter cookieParam = objectType.getAnnotation(HttpCookieParameter.class);
				if (cookieParam != null) {
					String paramName = cookieParam.value();
					this.addParameter(paramName, () -> new CookieParameter().name(paramName), objectType, operation,
							parameters);
				}

				// Include possible HTTP Parameters
				if (objectType.getAnnotation(HttpParameters.class) != null) {

					// Obtain the HTTP parameter names
					Class<?> httpParametersType = objectType.getObjectType();
					ValueLoaderSource loaderSource = new ValueLoaderSource(httpParametersType, false, new HashMap<>(),
							null);
					ValueLoaderFactory<?> loaderFactory = loaderSource.sourceValueLoaderFactory(httpParametersType);
					ValueName[] names = loaderFactory.getValueNames();

					// Load names in sorted order
					Arrays.sort(names, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()));
					for (ValueName name : names) {
						String paramName = name.getName();
						HttpValueLocation location = name.getLocation();
						if (location == null) {
							// Attempt to default location
							location = (explore.getRoutePath().toLowerCase()
									.contains("{" + paramName.toLowerCase() + "}")) ? HttpValueLocation.PATH
											: HttpValueLocation.QUERY;
						}
						switch (location) {
						case PATH:
							this.addParameter(paramName, () -> new PathParameter().name(paramName), objectType,
									operation, parameters);
							break;
						case ENTITY:
						case QUERY:
							this.addParameter(paramName, () -> new QueryParameter().name(paramName), objectType,
									operation, parameters);
							break;
						case HEADER:
							this.addParameter(paramName, () -> new HeaderParameter().name(paramName), objectType,
									operation, parameters);
							break;
						case COOKIE:
							this.addParameter(paramName, () -> new CookieParameter().name(paramName), objectType,
									operation, parameters);
							break;
						default:
							throw new IllegalStateException(
									"Unknown  " + HttpValueLocation.class.getSimpleName() + " " + location);
						}
					}
				}

				// Obtain the HTTP Object Parsers
				HttpObjectParserFactory[] objectParserFactories = explore.getHttpObjectParserFactories();

				// Include possible HTTP Object
				if (objectType.getAnnotation(HttpObject.class) != null) {

					// Obtain the request body schema
					Class<?> httpObjectType = objectType.getObjectType();
					ResolvedSchema resolvedSchema = ModelConverters.getInstance()
							.readAllAsResolvedSchema(httpObjectType);

					// Add the request body
					Content content = new Content();

					// Load the handled content types
					for (HttpObjectParserFactory objectParserFactory : objectParserFactories) {

						// Determine if can handle object type
						boolean isHandleHttpObject;
						try {
							isHandleHttpObject = objectParserFactory.createHttpObjectParser(httpObjectType) != null;
						} catch (Exception ex) {
							isHandleHttpObject = false;
						}

						// Able to handle input of type
						if (isHandleHttpObject) {
							content.addMediaType(objectParserFactory.getContentType(),
									new MediaType().schema(resolvedSchema.schema));
							operation.requestBody(new RequestBody().content(content));
						}
					}
				}

				// Include possible Object Responder
				Class<?> objectClass = objectType.getObjectType();
				if (ObjectResponse.class.equals(objectClass)) {

					// Obtain the response type
					ObjectResponseAnnotation responseAnnotation = objectType
							.getAnnotation(ObjectResponseAnnotation.class);
					Class<?> responseType = responseAnnotation != null ? responseAnnotation.getResponseType()
							: Object.class;

					// Obtain the response schema
					ResolvedSchema resolvedSchema = ModelConverters.getInstance().readAllAsResolvedSchema(responseType);

					// Include the response
					ApiResponses responses = new ApiResponses();
					operation.setResponses(responses);
					Content content = new Content();
					content.put("application/json", new MediaType().schema(resolvedSchema.schema));
					responses.addApiResponse("200", new ApiResponse().content(content));
				}
			}
		});

		// Serve up the Open API
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection service = office.addOfficeSection("OPEN_API", new OpenApiSection(openApi), null);

		// Serve the JSON
		office.link(web.getHttpInput(false, JSON_PATH).getInput(), service.getOfficeSectionInput(JSON));

		// Serve the YAML
		office.link(web.getHttpInput(false, YAML_PATH).getInput(), service.getOfficeSectionInput(YAML));
	}

	/**
	 * Add {@link Parameter}.
	 * 
	 * @param name       Name.
	 * @param factory    Factory to create {@link Parameter}.
	 * @param objectType {@link ManagedFunctionObjectType} for the
	 *                   {@link Parameter}.
	 * @param operation  {@link Operation}.
	 * @param parameters Existing {@link Parameter} instances by name.
	 */
	private void addParameter(String name, Supplier<Parameter> factory, ManagedFunctionObjectType<?> objectType,
			Operation operation, Map<String, Parameter> parameters) {
		Parameter parameter = parameters.get(name);
		if (parameter == null) {

			// Create the parameter
			parameter = factory.get();

			// Always String
			parameter.schema(new Schema<String>().type("string"));

			// Add in OpenAPI annotated information
			io.swagger.v3.oas.annotations.Parameter documentation = objectType
					.getAnnotation(io.swagger.v3.oas.annotations.Parameter.class);
			if (documentation != null) {
				parameter.setDescription(documentation.description());
				if (documentation.required()) {
					parameter.setRequired(true);
				}
				parameter.setExample(documentation.example());
			}

			// Include the parameter
			parameters.put(name, parameter);
			operation.addParametersItem(parameter);
		}
	}

	/**
	 * Dependency keys.
	 */
	private static enum Dependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Configures the Open API servicing.
	 */
	@PrivateSource
	private static class OpenApiSection extends AbstractSectionSource {

		/**
		 * {@link OpenAPI}.
		 */
		private final OpenAPI openApi;

		/**
		 * Initiate.
		 * 
		 * @param openApi {@link OpenAPI}.
		 */
		private OpenApiSection(OpenAPI openApi) {
			this.openApi = openApi;
		}

		/*
		 * ================= SectionSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

			// Configure the functions
			SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("SERVICE",
					new OpenApiFunctions(this.openApi));

			// Provide server HTTP connection
			SectionObject serverHttpConnection = designer.addSectionObject(ServerHttpConnection.class.getSimpleName(),
					ServerHttpConnection.class.getName());

			// Configure the JSON servicing
			SectionFunction jsonServicer = namespace.addSectionFunction(JSON, JSON);
			designer.link(jsonServicer.getFunctionObject(Dependencies.SERVER_HTTP_CONNECTION.name()),
					serverHttpConnection);
			designer.link(designer.addSectionInput(JSON, null), jsonServicer);

			// Configure the YAML servicing
			SectionFunction yamlServicer = namespace.addSectionFunction(YAML, YAML);
			designer.link(yamlServicer.getFunctionObject(Dependencies.SERVER_HTTP_CONNECTION.name()),
					serverHttpConnection);
			designer.link(designer.addSectionInput(YAML, null), yamlServicer);
		}
	}

	/**
	 * Configures the Open API servicing.
	 */
	@PrivateSource
	private static class OpenApiFunctions extends AbstractManagedFunctionSource {

		/**
		 * {@link OpenAPI}.
		 */
		private final OpenAPI openApi;

		/**
		 * JSON content.
		 */
		private volatile String json = null;

		/**
		 * YAML content.
		 */
		private volatile String yaml = null;

		/**
		 * Initiate.
		 * 
		 * @param openApi {@link OpenAPI}.
		 */
		private OpenApiFunctions(OpenAPI openApi) {
			this.openApi = openApi;
		}

		/*
		 * ============== ManagedFunctionSource ================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Configure the JSON servicing
			ManagedFunctionTypeBuilder<Dependencies, None> json = functionNamespaceTypeBuilder
					.addManagedFunctionType(JSON, () -> (managedFunctionContext) -> {

						// Obtain the JSON
						String jsonContent = this.json;
						if (jsonContent == null) {
							jsonContent = Json.pretty(this.openApi);
							this.json = jsonContent; // cache as should not change
						}

						// Send the JSON content
						ServerHttpConnection connection = (ServerHttpConnection) managedFunctionContext
								.getObject(Dependencies.SERVER_HTTP_CONNECTION);
						HttpResponse response = connection.getResponse();
						response.setContentType("application/json", null);
						response.getEntityWriter().write(jsonContent);

					}, Dependencies.class, None.class);
			json.addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);

			// Configure the YAML servicing
			ManagedFunctionTypeBuilder<Dependencies, None> yaml = functionNamespaceTypeBuilder
					.addManagedFunctionType(YAML, () -> (managedFunctionContext) -> {

						// Obtain the YAML
						String yamlContent = this.yaml;
						if (yamlContent == null) {
							yamlContent = Yaml.pretty(this.openApi);
							this.yaml = yamlContent; // cache as should not change
						}

						// Send the YAML content
						ServerHttpConnection connection = (ServerHttpConnection) managedFunctionContext
								.getObject(Dependencies.SERVER_HTTP_CONNECTION);
						HttpResponse response = connection.getResponse();
						response.setContentType("application/json", null);
						response.getEntityWriter().write(yamlContent);

					}, Dependencies.class, None.class);
			yaml.addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);
		}
	}

}