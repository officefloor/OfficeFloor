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
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Extension to load {@link SecurityScheme} based on {@link HttpSecurity}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OpenApiSecurityExtension {

	/**
	 * Extends the security.
	 * 
	 * @param context {@link OpenApiSecurityExtensionContext}.
	 * @throws Exception If fails to extend.
	 */
	void extend(OpenApiSecurityExtensionContext context) throws Exception;

}
