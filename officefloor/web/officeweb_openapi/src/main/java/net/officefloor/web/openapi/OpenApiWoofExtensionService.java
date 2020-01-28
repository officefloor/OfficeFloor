package net.officefloor.web.openapi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
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
import net.officefloor.web.build.WebArchitect;
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