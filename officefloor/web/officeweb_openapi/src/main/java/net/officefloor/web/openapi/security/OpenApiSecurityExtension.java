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
