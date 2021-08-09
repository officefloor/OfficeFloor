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
