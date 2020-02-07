package net.officefloor.web.openapi.security;

import io.swagger.v3.oas.models.security.SecurityScheme;
import net.officefloor.web.security.build.HttpSecurityExplorerContext;

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

}