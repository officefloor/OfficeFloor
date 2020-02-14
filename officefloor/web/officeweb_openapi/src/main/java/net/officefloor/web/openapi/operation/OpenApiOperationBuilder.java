package net.officefloor.web.openapi.operation;

import io.swagger.v3.oas.models.Operation;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;

/**
 * Builds the {@link Operation}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OpenApiOperationBuilder {

	/**
	 * <p>
	 * Loads the {@link ExecutionManagedFunction} information.
	 * <p>
	 * This will be invoked for each {@link ExecutionManagedFunction} in the
	 * execution tree.
	 * 
	 * @param context {@link OpenApiOperationFunctionContext}.
	 * @throws Exception If fails to load {@link ExecutionManagedFunction}.
	 */
	void buildInManagedFunction(OpenApiOperationFunctionContext context) throws Exception;

	/**
	 * Invoked at the end of building the {@link Operation}. This allows finalising
	 * the {@link Operation}.
	 * 
	 * @param context {@link OpenApiOperationContext}.
	 * @throws Exception If fails to complete the {@link Operation}.
	 */
	void buildComplete(OpenApiOperationContext context) throws Exception;

}