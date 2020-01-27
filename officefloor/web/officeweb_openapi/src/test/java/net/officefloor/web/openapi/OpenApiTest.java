package net.officefloor.web.openapi;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.compile.CompileWebExtension;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Test generating OpenAPI specification.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenApiTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to obtain swagger specification.
	 */
	public void testAllMethods() throws Exception {
		this.doOpenApiTest((context) -> {
			for (HttpMethod httpMethod : HttpMethod.values()) {
				context.link(false, httpMethod.name(), "/methods/all", NoOpService.class);
			}
		});
	}

	public static class NoOpService {
		public void service() {
			// no operation
		}
	}

	/**
	 * Undertakes the OpenAPI test.
	 * 
	 * @param extension {@link CompileWebExtension}.
	 */
	private void doOpenApiTest(CompileWebExtension extension) throws Exception {
		CompileWoof compiler = new CompileWoof();
		compiler.web(extension);
		try (MockWoofServer server = compiler.open()) {

			// Ensure can obtain swagger JSON
			MockWoofResponse response = server.send(MockHttpServer.mockRequest("/swagger.json"));
			assertEquals("Should find swagger", 200, response.getStatus().getStatusCode());

			// Obtain the expected specification
			String testName = this.getName();
			String expectedFileName = testName.substring("test".length()) + ".json";
			String expectedJson = this.getFileContents(this.findFile(this.getClass(), expectedFileName));

			// Round trip for better comparison
			OpenAPI expectedApi = Json.mapper().readValue(expectedJson, OpenAPI.class);
			expectedJson = Json.pretty(expectedApi);

			// Ensure correct result
			String spec = response.getEntity(null);
			assertEquals("Incorrect content", expectedJson, spec);
		}

	}

	public void _testResolvedModel() throws Exception {
		AnnotatedType type = new AnnotatedType(Parent.class);
		type.setResolveAsRef(true);
		ResolvedSchema schema = ModelConverters.getInstance().readAllAsResolvedSchema(type);

		OpenAPI api = new OpenAPI();
		Paths paths = new Paths();
		api.setPaths(paths);

		Operation get = new Operation();
		get.setDescription("Description");

		ApiResponses responses = new ApiResponses();
		ApiResponse response = new ApiResponse();
		response.description("RESPONSE");
		responses.addApiResponse("200", response);
		get.setResponses(responses);

		Operation post = new Operation();
		post.setDescription("POST");

		PathItem pathItem = new PathItem();
		pathItem.get(get);
		pathItem.post(post);
		paths.addPathItem("/hi", pathItem);

		Components components = new Components();
		api.setComponents(components);
		components.setSchemas(schema.referencedSchemas);
		System.out.println(Json.pretty(api));
	}

	public static class Parent {

		public String getMessage() {
			return "test";
		}

		public Child getChild() {
			return new Child();
		}
	}

	public static class Child {

		public String getAnswer() {
			return "answer";
		}
	}

}