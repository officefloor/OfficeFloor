package net.officefloor.web.openapi.operation;

import io.swagger.v3.oas.models.Operation;

/**
 * Extension to configure the {@link Operation}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OpenApiOperationExtension {

	/**
	 * Creates the {@link OpenApiOperationBuilder} for the {@link Operation}.
	 * 
	 * @param context {@link OpenApiOperationContext}.
	 * @return {@link OpenApiOperationBuilder} or <code>null</code> if not handle
	 *         {@link Operation}.
	 * @throws Exception If fails to create the {@link OpenApiOperationBuilder}.
	 */
	OpenApiOperationBuilder createBuilder(OpenApiOperationContext context) throws Exception;

}