package net.officefloor.plugin.managedfunction.method;

import net.officefloor.frame.api.function.ManagedFunctionContext;

/**
 * Creates the parameter for the {@link MethodFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodParameterFactory {

	/**
	 * Creates the parameter from the {@link ManagedFunctionContext}.
	 * 
	 * @param context
	 *            {@link ManagedFunctionContext}.
	 * @return Parameter.
	 * @throws Exception
	 *             If fails to create the parameter.
	 */
	Object createParameter(ManagedFunctionContext<?, ?> context) throws Exception;

}