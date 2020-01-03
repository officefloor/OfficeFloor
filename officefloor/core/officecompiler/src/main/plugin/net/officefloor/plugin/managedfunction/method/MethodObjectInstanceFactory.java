package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;

/**
 * Factory to create the {@link Object} instance to invoke the {@link Method}
 * on.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodObjectInstanceFactory {

	/**
	 * Creates the {@link Object} instance to invoke the {@link Method} on.
	 * 
	 * @param context {@link ManagedFunctionContext}.
	 * @return {@link Object} instance to invoke the {@link Method} on.
	 * @throws Exception If fails to create the instance.
	 */
	Object createInstance(ManagedFunctionContext<Indexed, Indexed> context) throws Exception;

}