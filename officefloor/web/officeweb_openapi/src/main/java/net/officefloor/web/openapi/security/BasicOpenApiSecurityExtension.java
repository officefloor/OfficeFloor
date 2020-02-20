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
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * {@link OpenApiSecurityExtension} for <code>basic</code> authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class BasicOpenApiSecurityExtension implements OpenApiSecurityExtension, OpenApiSecurityExtensionServiceFactory {

	/**
	 * {@link Class} of the <code>basic</code> {@link HttpSecuritySource}.
	 */
	public static final String BASIC_HTTP_SECURITY_SOURCE_CLASS_NAME = "net.officefloor.web.security.scheme.BasicHttpSecuritySource";

	/*
	 * ======================== OpenApiSecurityExtension =======================
	 */

	@Override
	public OpenApiSecurityExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extend(OpenApiSecurityExtensionContext context) throws Exception {
		if (context.getHttpSecurity().getHttpSecuritySource().getClass().getName()
				.equals(BASIC_HTTP_SECURITY_SOURCE_CLASS_NAME)) {
			String securityName = context.getHttpSecurity().getHttpSecurityName();
			SecurityScheme scheme = new SecurityScheme();
			scheme.setType(Type.HTTP);
			scheme.setScheme("basic");
			context.addSecurityScheme(securityName, scheme);
		}
	}

}
