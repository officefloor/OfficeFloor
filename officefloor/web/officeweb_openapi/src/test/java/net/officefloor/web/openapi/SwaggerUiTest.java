/*-
 * #%L
 * OpenAPI
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.openapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can send Swagger UI resources for viewing Open API information.
 * 
 * @author Daniel Sagenschneider
 */
public class SwaggerUiTest {

	/**
	 * Ensure able to obtain <code>index.html</code>.
	 */
	@Test
	public void swagger() throws Exception {
		this.doIndexTest(OpenApiWoofExtensionService.DEFAULT_SWAGGER_PATH);
	}

	/**
	 * Ensure <code>index.html</code>.
	 */
	@Test
	public void indexHtml() throws Exception {
		this.doIndexTest(OpenApiWoofExtensionService.DEFAULT_SWAGGER_PATH + "/index.html");
	}

	/**
	 * Ensure intercept <code>swagger-initializer.js</code>.
	 * 
	 * @throws Exception
	 */
	@Test
	public void swaggerInitializerJs() throws Exception {
		this.doSwaggerInitializerTest(OpenApiWoofExtensionService.DEFAULT_SWAGGER_PATH + "/swagger-initializer.js");
	}

	/**
	 * Ensure able to obtain backing files.
	 */
	@Test
	public void remainingFiles() throws Exception {
		for (String path : new String[] { "absolute-path.js", "index.js", "oauth2-redirect.html",
				"swagger-ui-bundle.js", "swagger-ui-standalone-preset.js", "swagger-ui.css", "swagger-ui.js" }) {
			this.doSwaggerTest(OpenApiWoofExtensionService.DEFAULT_SWAGGER_PATH + "/" + path, null);
		}
	}

	/**
	 * Ensure no swagger UI.
	 */
	@Test
	public void noSwaggerUI() throws Exception {
		this.doSwaggerTest(OpenApiWoofExtensionService.DEFAULT_SWAGGER_PATH, (server, response) -> {
			assertEquals(404, response.getStatus().getStatusCode(), "Swagger UI should not be availble");

			// Ensure OpenAPI specifications still available
			for (String path : new String[] { OpenApiWoofExtensionService.DEFAULT_JSON_PATH,
					OpenApiWoofExtensionService.DEFAULT_YAML_PATH }) {
				response = server.send(MockWoofServer.mockRequest(path));
				assertEquals(200, response.getStatus().getStatusCode(), "Should find OpenAPI");
			}
		}, OpenApiWoofExtensionService.PROPERTY_DISABLE_SWAGGER, "true");
	}

	/**
	 * Ensure no OpenAPI.
	 */
	@Test
	public void noOpenApi() throws Exception {
		this.doSwaggerTest("/swagger", (server, response) -> {
			assertEquals(404, response.getStatus().getStatusCode(), "Swagger UI should not be availble");

			// Ensure OpenAPI specifications still available
			for (String path : new String[] { OpenApiWoofExtensionService.DEFAULT_JSON_PATH,
					OpenApiWoofExtensionService.DEFAULT_YAML_PATH }) {
				response = server.send(MockWoofServer.mockRequest(path));
				assertEquals(404, response.getStatus().getStatusCode(), "Should not find OpenAPI");
			}
		}, OpenApiWoofExtensionService.PROPERTY_DISABLE_OPEN_API, "true");
	}

	/**
	 * Ensure can configure path to JSON resource.
	 */
	@Test
	public void specificJsonPath() throws Exception {
		final String otherJsonPath = "/other";
		this.doOpenApiTest(otherJsonPath, "application/json", OpenApiWoofExtensionService.PROPERTY_JSON_PATH,
				otherJsonPath);
	}

	/**
	 * Ensure can configure path to YAML resource.
	 */
	@Test
	public void specificYamlPath() throws Exception {
		final String otherYamlPath = "other.yaml";
		this.doOpenApiTest("/" + otherYamlPath, "application/yaml", OpenApiWoofExtensionService.PROPERTY_YAML_PATH,
				otherYamlPath);
	}

	/**
	 * Ensure can configure path to Swagger resources.
	 */
	@Test
	public void specificSwaggerPath() throws Exception {
		final String otherSwaggerPath = "other/something";
		this.doSwaggerTest("/" + otherSwaggerPath, null, OpenApiWoofExtensionService.PROPERTY_SWAGGER_PATH,
				otherSwaggerPath);
	}

	/**
	 * Undertakes test for <code>index.html</code>.
	 * 
	 * @param path                   Path.
	 * @param propertyNameValuePairs {@link Property} name/value pairs for
	 *                               configuration
	 *                               {@link OpenApiWoofExtensionService}.
	 */
	private void doIndexTest(String path, String... propertyNameValuePairs) throws Exception {
		this.doSwaggerTest(path, (server, response) -> {
			assertEquals(200, response.getStatus().getStatusCode(), "Should be successful (path: " + path + ")");
			assertEquals("text/html", response.getHeader("content-type").getValue(), "Incorrect Content-Type");
			String entity = response.getEntity(null);
			assertTrue(entity.contains("<title>Swagger UI</title>"), "Incorrect index.html");
			assertTrue(entity.contains("./swagger-initializer.js"), "Should contain initialize URL");
		}, propertyNameValuePairs);
	}

	/**
	 * Undertakes test for <code>swagger-initializer.js</code>.
	 * 
	 * @param path                   Path.
	 * @param propertyNameValuePairs {@link Property} name/value pairs for
	 *                               configuration
	 *                               {@link OpenApiWoofExtensionService}.
	 */
	private void doSwaggerInitializerTest(String path, String... propertyNameValuePairs) throws Exception {
		this.doSwaggerTest(path, (server, response) -> {
			assertEquals(200, response.getStatus().getStatusCode(), "Should be successful (path: " + path + ")");
			assertEquals("application/javascript", response.getHeader("content-type").getValue(),
					"Incorrect Content-Type");
			String entity = response.getEntity(null);

			System.out.println(entity);

			assertFalse(entity.contains("petstore"), "Should not contain petstore url\n\n" + entity);
			assertTrue(entity.contains("/openapi.json"), "Should contain initialize URL");
		}, propertyNameValuePairs);
	}

	/**
	 * Undertakes test for OpenAPI JSON/YAML responses.
	 * 
	 * @param path                   Path.
	 * @param contentType            Expected <code>Content-Type</code>.
	 * @param propertyNameValuePairs {@link Property} name/value pairs for
	 *                               configuration
	 *                               {@link OpenApiWoofExtensionService}.
	 */
	private void doOpenApiTest(String path, String contentType, String... propertyNameValuePairs) throws Exception {
		this.doSwaggerTest(path, (server, response) -> {
			assertEquals(200, response.getStatus().getStatusCode(), "Should be successful (path: " + path + ")");
			assertEquals(contentType, response.getHeader("content-type").getValue(), "Incorrect Content-Type");
		}, propertyNameValuePairs);
	}

	/**
	 * Undertakes swagger request.
	 * 
	 * @param path                   Path for swagger file.
	 * @param validator              Validates the swagger file.
	 * @param propertyNameValuePairs {@link Property} name/value pairs for
	 *                               configuration
	 *                               {@link OpenApiWoofExtensionService}.
	 */
	private void doSwaggerTest(String path, BiConsumer<MockWoofServer, MockWoofResponse> validator,
			String... propertyNameValuePairs) throws Exception {
		CompileWoof compiler = new CompileWoof(true);
		compiler.officeFloor((context) -> {
			DeployedOffice office = context.getDeployedOffice();
			for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
				office.addProperty(propertyNameValuePairs[i], propertyNameValuePairs[i + 1]);
			}
		});
		compiler.web((context) -> {
			context.link(false, "/path", NoOperationSection.class);
		});
		try (MockWoofServer server = compiler.open()) {
			MockWoofResponse response = server.send(MockWoofServer.mockRequest(path));
			if (validator == null) {
				assertEquals(200, response.getStatus().getStatusCode(), "Should be successful (path: " + path + ")");
			} else {
				validator.accept(server, response);
			}
		}
	}

	public static class NoOperationSection {
		public void service() {
			// no operation
		}
	}

}
