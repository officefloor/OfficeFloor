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

package net.officefloor.web.openapi.security;

import io.swagger.v3.oas.models.security.SecurityScheme;
import net.officefloor.web.openapi.operation.OpenApiOperationExtension;
import net.officefloor.web.security.build.HttpSecurityExplorerContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Context for the {@link OpenApiSecurityExtensionContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OpenApiSecurityExtensionContext {

	/**
	 * Obtains the {@link HttpSecurityExplorerContext}.
	 * 
	 * @return {@link HttpSecurityExplorerContext}.
	 */
	HttpSecurityExplorerContext getHttpSecurity();

	/**
	 * Registers a {@link SecurityScheme}.
	 * 
	 * @param securityName Name of security.
	 * @param scheme       {@link SecurityScheme}.
	 */
	void addSecurityScheme(String securityName, SecurityScheme scheme);

	/**
	 * <p>
	 * Adds an {@link OpenApiOperationExtension}.
	 * <p>
	 * This allows for custom description specific to {@link HttpSecuritySource}.
	 * 
	 * @param extension {@link OpenApiOperationExtension}.
	 */
	void addOperationExtension(OpenApiOperationExtension extension);

}
