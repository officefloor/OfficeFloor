package net.officefloor.web.openapi.operation;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import net.officefloor.web.build.HttpInputExplorerContext;
import net.officefloor.web.openapi.security.OpenApiSecurityExtension;

/**
 * Context for the {@link OpenApiOperationBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OpenApiOperationContext {

	/**
	 * Obtains the {@link HttpInputExplorerContext}.
	 * 
	 * @return {@link HttpInputExplorerContext}.
	 */
	HttpInputExplorerContext getHttpInput();

	/**
	 * Obtains the {@link PathItem}.
	 * 
	 * @return {@link PathItem}.
	 */
	PathItem getPath();

	/**
	 * Obtains the {@link Operation}.
	 * 
	 * @return {@link Operation}.
	 */
	Operation getOperation();

	/**
	 * Obtains the {@link Parameter} by name.
	 * 
	 * @param name Name of the {@link Parameter}.
	 * @return {@link Parameter} by name or <code>null</code> if none.
	 */
	Parameter getParameter(String name);

	/**
	 * <p>
	 * Convenience method to lazy create the {@link SecurityRequirement}.
	 * <p>
	 * This will return the first {@link SecurityRequirement} registered for the
	 * {@link SecurityScheme}.
	 * 
	 * @param securityName Name of {@link SecurityScheme}.
	 * @return Existing {@link SecurityRequirement} for {@link SecurityScheme} or
	 *         created {@link SecurityRequirement} if not already added.
	 */
	SecurityRequirement getOrAddSecurityRequirement(String securityName);

	/**
	 * Obtains the {@link Components}.
	 * 
	 * @return {@link Components}.
	 */
	Components getComponents();

	/**
	 * Obtains all the security names registered via the
	 * {@link OpenApiSecurityExtension} instances.
	 * 
	 * @return All the security names.
	 */
	String[] getAllSecurityNames();

}