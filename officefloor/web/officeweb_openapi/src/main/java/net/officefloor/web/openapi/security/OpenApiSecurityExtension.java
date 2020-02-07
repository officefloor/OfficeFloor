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