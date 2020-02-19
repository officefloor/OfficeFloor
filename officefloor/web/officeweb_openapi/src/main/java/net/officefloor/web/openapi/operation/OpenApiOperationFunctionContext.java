package net.officefloor.web.openapi.operation;

import net.officefloor.compile.spi.office.ExecutionManagedFunction;

/**
 * Context for the {@link OpenApiOperationBuilder} building the
 * {@link ExecutionManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OpenApiOperationFunctionContext extends OpenApiOperationContext {

	/**
	 * Obtains the {@link ExecutionManagedFunction}.
	 * 
	 * @return {@link ExecutionManagedFunction}.
	 */
	ExecutionManagedFunction getManagedFunction();

}