/*-
 * #%L
 * OpenAPI
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
