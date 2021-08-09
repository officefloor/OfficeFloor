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

import java.util.function.BiConsumer;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can send Swagger UI resources for viewing Open API information.
 * 
 * @author Daniel Sagenschneider
 */
public class SwaggerUiTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to obtain <code>index.html</code>.
	 */
	public void testSwagger() throws Exception {
		this.doIndexTest(OpenApiWoofExtensionService.DEFAULT_SWAGGER_PATH);
	}

	/**
	 * Ensure intercept <code>index.html</code>.
	 */
	public void testInterceptIndexHtml() throws Exception {
		this.doIndexTest(OpenApiWoofExtensionService.DEFAULT_SWAGGER_PATH + "/index.html");
	}

	/**
	 * Ensure able to obtain backing files.
	 */
	public void testRemainingFiles() throws Exception {
		for (String path : new String[] { "absolute-path.js", "index.js", "oauth2-redirect.html",
				"swagger-ui-bundle.js", "swagger-ui-standalone-preset.js", "swagger-ui.css", "swagger-ui.js" }) {
			this.doSwaggerTest(OpenApiWoofExtensionService.DEFAULT_SWAGGER_PATH + "/" + path, null);
		}
	}

	/**
	 * Ensure no swagger UI.
	 */
	public void testNoSwaggerUI() throws Exception {
		this.doSwaggerTest(OpenApiWoofExtensionService.DEFAULT_SWAGGER_PATH, (server, response) -> {
			assertEquals("Swagger UI should not be availble", 404, response.getStatus().getStatusCode());

			// Ensure OpenAPI specifications still available
			for (String path : new String[] { OpenApiWoofExtensionService.DEFAULT_JSON_PATH,
					OpenApiWoofExtensionService.DEFAULT_YAML_PATH }) {
				response = server.send(MockWoofServer.mockRequest(path));
				assertEquals("Should find OpenAPI", 200, response.getStatus().getStatusCode());
			}
		}, OpenApiWoofExtensionService.PROPERTY_DISABLE_SWAGGER, "true");
	}

	/**
	 * Ensure no OpenAPI.
	 */
	public void testNoOpenApi() throws Exception {
		this.doSwaggerTest("/swagger", (server, response) -> {
			assertEquals("Swagger UI should not be availble", 404, response.getStatus().getStatusCode());

			// Ensure OpenAPI specifications still available
			for (String path : new String[] { OpenApiWoofExtensionService.DEFAULT_JSON_PATH,
					OpenApiWoofExtensionService.DEFAULT_YAML_PATH }) {
				response = server.send(MockWoofServer.mockRequest(path));
				assertEquals("Should not find OpenAPI", 404, response.getStatus().getStatusCode());
			}
		}, OpenApiWoofExtensionService.PROPERTY_DISABLE_OPEN_API, "true");
	}

	/**
	 * Ensure can configure path to JSON resource.
	 */
	public void testSpecificJsonPath() throws Exception {
		final String otherJsonPath = "/other";
		this.doOpenApiTest(otherJsonPath, "application/json", OpenApiWoofExtensionService.PROPERTY_JSON_PATH,
				otherJsonPath);
	}

	/**
	 * Ensure can configure path to YAML resource.
	 */
	public void testSpecificYamlPath() throws Exception {
		final String otherYamlPath = "other.yaml";
		this.doOpenApiTest("/" + otherYamlPath, "application/yaml", OpenApiWoofExtensionService.PROPERTY_YAML_PATH,
				otherYamlPath);
	}

	/**
	 * Ensure can configure path to Swagger resources.
	 */
	public void testSpecificSwaggerPath() throws Exception {
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
			assertEquals("Should be successful (path: " + path + ")", 200, response.getStatus().getStatusCode());
			assertEquals("Incorrect Content-Type", "text/html", response.getHeader("content-type").getValue());
			String entity = response.getEntity(null);
			assertTrue("Incorrect index.html", entity.contains("<title>Swagger UI</title>"));
			assertFalse("Should not contain petstore URL", entity.contains("petstore"));
			assertTrue("Should contain hosted OpenApi URL", entity.contains("\"/openapi.json\""));
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
			assertEquals("Should be successful (path: " + path + ")", 200, response.getStatus().getStatusCode());
			assertEquals("Incorrect Content-Type", contentType, response.getHeader("content-type").getValue());
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
				assertEquals("Should be successful (path: " + path + ")", 200, response.getStatus().getStatusCode());
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
