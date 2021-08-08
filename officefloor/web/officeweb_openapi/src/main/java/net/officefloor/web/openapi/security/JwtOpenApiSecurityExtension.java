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
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.openapi.operation.OpenApiOperationBuilder;
import net.officefloor.web.openapi.operation.OpenApiOperationContext;
import net.officefloor.web.openapi.operation.OpenApiOperationExtension;
import net.officefloor.web.openapi.operation.OpenApiOperationFunctionContext;
import net.officefloor.web.security.build.HttpSecurityExplorerContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * {@link OpenApiSecurityExtension} for <code>basic</code> authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtOpenApiSecurityExtension implements OpenApiSecurityExtension, OpenApiSecurityExtensionServiceFactory {

	/**
	 * {@link Class} of the <code>JWT</code> {@link HttpSecuritySource}.
	 */
	public static final String JWT_HTTP_SECURITY_SOURCE_CLASS_NAME = "net.officefloor.web.jwt.JwtHttpSecuritySource";

	/*
	 * ======================== OpenApiSecurityExtension =======================
	 */

	@Override
	public OpenApiSecurityExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extend(OpenApiSecurityExtensionContext context) throws Exception {

		// Obtain the security
		HttpSecurityExplorerContext security = context.getHttpSecurity();

		// Determine if JWT security
		if (security.getHttpSecuritySource().getClass().getName().equals(JWT_HTTP_SECURITY_SOURCE_CLASS_NAME)) {

			// Register Security Scheme
			SecurityScheme scheme = new SecurityScheme();
			String securityName = security.getHttpSecurityName();
			scheme.setType(Type.HTTP);
			scheme.setScheme("bearer");
			context.addSecurityScheme(securityName, scheme);

			// Obtain the claims type (should be only supporting object)
			Class<?> claimsType = security.getHttpSecurityType().getSupportingManagedObjectTypes()[0].getObjectType();

			// Register builder for JWT claims
			context.addOperationExtension(new JwtOperation(securityName, claimsType));
		}
	}

	/**
	 * {@link OpenApiOperationExtension} for JWT claims.
	 */
	private static class JwtOperation implements OpenApiOperationExtension, OpenApiOperationBuilder {

		/**
		 * Name of security.
		 */
		private final String securityName;

		/**
		 * JWT claims type.
		 */
		private final Class<?> claimsType;

		/**
		 * Instantiate.
		 * 
		 * @param securityName Name of security.
		 * @param claimsType   JWT claims type.
		 */
		public JwtOperation(String securityName, Class<?> claimsType) {
			this.securityName = securityName;
			this.claimsType = claimsType;
		}

		/*
		 * ================== OpenApiOperationExtension ==================
		 */

		@Override
		public OpenApiOperationBuilder createBuilder(OpenApiOperationContext context) throws Exception {
			return this;
		}

		/*
		 * =================== OpenApiOperationBuilder ===================
		 */

		@Override
		public void buildInManagedFunction(OpenApiOperationFunctionContext context) throws Exception {

			// Determine if injecting JWT claims
			for (ManagedFunctionObjectType<?> objectType : context.getManagedFunction().getManagedFunctionType()
					.getObjectTypes()) {

				// Determine if JWT claims type
				if (this.claimsType.isAssignableFrom(objectType.getObjectType())) {

					// JWT claims, so register security requirement
					context.getOrAddSecurityRequirement(this.securityName);
				}
			}
		}

		@Override
		public void buildComplete(OpenApiOperationContext context) throws Exception {
			// nothing to complete
		}
	}

}
