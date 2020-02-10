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