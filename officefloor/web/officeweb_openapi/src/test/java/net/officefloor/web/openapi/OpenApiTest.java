package net.officefloor.web.openapi;

import java.io.Serializable;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpCookieParameter;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
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
	public void testAllMethods() {
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
	 * Ensure can provide {@link PathParameter}.
	 */
	public void testPathParameter() {
		this.doOpenApiTest((context) -> context.link(false, "/path/{parameter}", PathParameterService.class));
	}

	public static class PathParameterService {
		public void service(@HttpPathParameter("parameter") String parameter) {
			// no operation
		}
	}

	/**
	 * Ensure can provide {@link QueryParameter}.
	 */
	public void testQueryParameter() {
		this.doOpenApiTest((context) -> context.link(false, "/path", QueryParameterService.class));
	}

	public static class QueryParameterService {
		public void service(@HttpQueryParameter("parameter") String parameter) {
			// no operation
		}
	}

	/**
	 * Ensure can provide {@link HeaderParameter}.
	 */
	public void testHeaderParameter() {
		this.doOpenApiTest((context) -> context.link(false, "/path", HeaderParameterService.class));
	}

	public static class HeaderParameterService {
		public void service(@HttpHeaderParameter("parameter") String parameter) {
			// no operation
		}
	}

	/**
	 * Ensure can provide {@link CookieParameter}.
	 */
	public void testCookieParameter() {
		this.doOpenApiTest((context) -> context.link(false, "/path", CookieParameterService.class));
	}

	public static class CookieParameterService {
		public void service(@HttpCookieParameter("parameter") String parameter) {
			// no operation
		}
	}

	/**
	 * Ensure can describe {@link HttpParameters} object.
	 */
	public void testHttpParameters() {
		this.doOpenApiTest((context) -> context.link(false, "/path/{one}", ParametersService.class));
	}

	@HttpParameters
	public static class Parameters implements Serializable {
		private static final long serialVersionUID = 1L;

		public void setOne(String one) {
			// no operation
		}

		public void setTwo(String two) {
			// no operation
		}
	}

	public static class ParametersService {
		public void service(Parameters parameters) {
			// no operation
		}
	}

	/**
	 * Ensure can provide {@link RequestBody}.
	 */
	public void testRequestBody() {
		this.doOpenApiTest((context) -> context.link(false, "/path", RequestBodyService.class));
	}

	@HttpObject
	public static class Request {
		public String getMessage() {
			return "MOCK";
		}
	}

	public static class RequestBodyService {
		public void service(Request request) {
			// no operation
		}
	}

	/**
	 * Undertakes the OpenAPI test.
	 * 
	 * @param extension {@link CompileWebExtension}.
	 */
	private void doOpenApiTest(CompileWebExtension extension) {
		try {
			CompileWoof compiler = new CompileWoof();
			compiler.web(extension);
			try (MockWoofServer server = compiler.open()) {

				// Obtain the expected specification
				String testName = this.getName();
				String expectedFileName = testName.substring("test".length()) + ".json";
				String expectedContent = this.getFileContents(this.findFile(this.getClass(), expectedFileName));

				// Translate to YAML and JSON (round trip for better comparison)
				OpenAPI expectedApi = Json.mapper().readValue(expectedContent, OpenAPI.class);

				// Ensure correct JSON
				MockWoofResponse response = server.send(MockHttpServer.mockRequest("/openapi.json"));
				assertEquals("Should find OpenAPI JSON", 200, response.getStatus().getStatusCode());
				String expectedJson = Json.pretty(expectedApi);
				String actualJson = response.getEntity(null);
				this.printMessage("EXPECTED:\n" + expectedJson);
				this.printMessage("JSON:\n" + actualJson);
				assertEquals("Incorrect JSON", expectedJson, actualJson);

				// Ensure correct YAML
				response = server.send(MockHttpServer.mockRequest("/openapi.yaml"));
				assertEquals("Should find OpenAPI YAML", 200, response.getStatus().getStatusCode());
				String expectedYaml = Yaml.pretty(expectedApi);
				String actualYaml = response.getEntity(null);
				this.printMessage("YAML:\n" + actualYaml);
				assertEquals("Incorrect YAML", expectedYaml, actualYaml);
			}
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}