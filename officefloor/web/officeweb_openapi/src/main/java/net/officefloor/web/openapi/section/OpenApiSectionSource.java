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

package net.officefloor.web.openapi.section;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
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

/**
 * Configures the Open API servicing.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class OpenApiSectionSource extends AbstractSectionSource {

	/**
	 * Name to link the JSON servicing.
	 */
	public static final String JSON = "JSON";

	/**
	 * Name to link the YAML servicing.
	 */
	public static final String YAML = "YAML";

	/**
	 * Dependency keys.
	 */
	private static enum Dependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link OpenAPI}.
	 */
	private final OpenAPI openApi;

	/**
	 * Initiate.
	 * 
	 * @param openApi {@link OpenAPI}.
	 */
	public OpenApiSectionSource(OpenAPI openApi) {
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
		designer.link(jsonServicer.getFunctionObject(Dependencies.SERVER_HTTP_CONNECTION.name()), serverHttpConnection);
		designer.link(designer.addSectionInput(JSON, null), jsonServicer);

		// Configure the YAML servicing
		SectionFunction yamlServicer = namespace.addSectionFunction(YAML, YAML);
		designer.link(yamlServicer.getFunctionObject(Dependencies.SERVER_HTTP_CONNECTION.name()), serverHttpConnection);
		designer.link(designer.addSectionInput(YAML, null), yamlServicer);
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
					.addManagedFunctionType(JSON, Dependencies.class, None.class)
					.setFunctionFactory(() -> (managedFunctionContext) -> {

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

					});
			json.addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);

			// Configure the YAML servicing
			ManagedFunctionTypeBuilder<Dependencies, None> yaml = functionNamespaceTypeBuilder
					.addManagedFunctionType(YAML, Dependencies.class, None.class)
					.setFunctionFactory(() -> (managedFunctionContext) -> {

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
						response.setContentType("application/yaml", null);
						response.getEntityWriter().write(yamlContent);

					});
			yaml.addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);
		}
	}

}
