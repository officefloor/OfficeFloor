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

import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

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
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.build.HttpInputExplorerContext;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.openapi.build.OpenApiEmployer;
import net.officefloor.web.openapi.operation.DefaultOpenApiOperationBuilder;
import net.officefloor.web.openapi.operation.OpenApiOperationBuilder;
import net.officefloor.web.openapi.operation.OpenApiOperationExtension;
import net.officefloor.web.openapi.operation.OpenApiOperationExtensionServiceFactory;
import net.officefloor.web.openapi.operation.OpenApiOperationFunctionContext;
import net.officefloor.web.openapi.section.OpenApiSectionSource;
import net.officefloor.web.openapi.security.OpenApiSecurityExtension;
import net.officefloor.web.openapi.security.OpenApiSecurityExtensionContext;
import net.officefloor.web.openapi.security.OpenApiSecurityExtensionServiceFactory;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.resource.build.HttpResourcesBuilder;
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
	 * {@link Property} name to disable OpenAPI. This is useful for different
	 * environments to be configured differently.
	 */
	public static final String PROPERTY_DISABLE_OPEN_API = "openapi.disable";

	/**
	 * {@link Property} name for the OpenAPI JSON path.
	 */
	public static final String PROPERTY_JSON_PATH = "openapi.json.path";

	/**
	 * Default JSON path.
	 */
	public static final String DEFAULT_JSON_PATH = "/openapi.json";

	/**
	 * {@link Property} name for the OpenAPI YAML path.
	 */
	public static final String PROPERTY_YAML_PATH = "openapi.yaml.path";

	/**
	 * Default YAML path.
	 */
	public static final String DEFAULT_YAML_PATH = "/openapi.yaml";

	/**
	 * {@link Property} name to disable Swagger UI. This is useful to only expose
	 * the OpenAPI specifications, as Swagger UI likely hosted elsewhere.
	 */
	public static final String PROPERTY_DISABLE_SWAGGER = "swagger.ui.disable";

	/**
	 * {@link Property} name for the Swagger UI path.
	 */
	public static final String PROPERTY_SWAGGER_PATH = "swagger.ui.path";

	/**
	 * Default swagger path.
	 */
	public static final String DEFAULT_SWAGGER_PATH = "/swagger";

	/*
	 * ================== WoofExtensionService ====================
	 */

	@Override
	public WoofExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extend(WoofContext context) throws Exception {

		// Obtain the office context
		OfficeExtensionContext officeContext = context.getOfficeExtensionContext();

		// Determine if provide OpenAPI
		boolean isDisableOpenApi = Boolean
				.parseBoolean(officeContext.getProperty(PROPERTY_DISABLE_OPEN_API, Boolean.FALSE.toString()));
		if (isDisableOpenApi) {
			return; // don't extend, as disabled
		}

		// Load the paths
		final String openApiJsonPath = officeContext.getProperty(PROPERTY_JSON_PATH, DEFAULT_JSON_PATH);
		final String openApiYamlPath = officeContext.getProperty(PROPERTY_YAML_PATH, DEFAULT_YAML_PATH);
		final String swaggerPath = officeContext.getProperty(PROPERTY_SWAGGER_PATH, DEFAULT_SWAGGER_PATH);
		final Set<String> SELF_REGISTERED_PATHS = new HashSet<>(Arrays.asList(openApiJsonPath, openApiYamlPath));

		// Obtain the architects
		OfficeArchitect office = context.getOfficeArchitect();
		WebArchitect web = context.getWebArchitect();
		HttpSecurityArchitect security = context.getHttpSecurityArchitect();
		OpenApiArchitect openApiArchitect = OpenApiEmployer.employOpenApiArchitect(office, web, security, context.getOfficeExtensionContext());

		// Create the root
		OpenAPI openApi = new OpenAPI();
		Paths paths = new Paths();
		openApi.setPaths(paths);

		// Build the Open API
		openApiArchitect.buildOpenApi(openApi, SELF_REGISTERED_PATHS);

		// Serve up the Open API
		OfficeSection service = office.addOfficeSection("OPEN_API", new OpenApiSectionSource(openApi), null);

		// Transform path for configuration
		Function<String, String> transformPath = (path) -> path.startsWith("/") ? path : "/" + path;

		// Serve the JSON
		office.link(web.getHttpInput(false, transformPath.apply(openApiJsonPath)).getInput(),
				service.getOfficeSectionInput(OpenApiSectionSource.JSON));

		// Serve the YAML
		office.link(web.getHttpInput(false, transformPath.apply(openApiYamlPath)).getInput(),
				service.getOfficeSectionInput(OpenApiSectionSource.YAML));

		// Determine if disable Swagger UI
		boolean isDisableSwagger = Boolean
				.parseBoolean(officeContext.getProperty(PROPERTY_DISABLE_SWAGGER, Boolean.FALSE.toString()));
		if (!isDisableSwagger) {

			// Load the version of Swagger UI
			String swaggerVersion;
			try (InputStream swaggerVersionInput = officeContext
					.getResource("META-INF/maven/org.webjars.npm/swagger-ui-dist/pom.properties")) {
				Properties properties = new Properties();
				properties.load(swaggerVersionInput);
				swaggerVersion = properties.getProperty("version");
			}

			// Provide Swagger UI
			HttpResourceArchitect resource = context.getHttpResourceArchitect();
			HttpResourcesBuilder swaggerResourcesBuilder = resource
					.addHttpResources("classpath:META-INF/resources/webjars/swagger-ui-dist/" + swaggerVersion);
			swaggerResourcesBuilder.setContextPath(swaggerPath);
			swaggerResourcesBuilder.addResourceTransformer((transform) -> {
				// Determine if swagger-initializer.js
				if (transform.getPath().equals("/swagger-initializer.js")) {

					// Obtain the raw content
					Path rawIndexHtml = transform.getResource();
					StringWriter rawContent = new StringWriter();
					try (Reader reader = Files.newBufferedReader(rawIndexHtml)) {
						for (int character = reader.read(); character != -1; character = reader.read()) {
							rawContent.write(character);
						}
					}

					// Provide appropriate URL to OpenApi
					String applicationContent = rawContent.toString()
							.replace("https://petstore.swagger.io/v2/swagger.json", openApiJsonPath);

					// Write the content
					Path applicationIndexHtml = transform.createFile();
					try (Writer writer = Files.newBufferedWriter(applicationIndexHtml)) {
						writer.write(applicationContent);
					}
					transform.setTransformedResource(applicationIndexHtml);
				}
			});
		}
	}

}
