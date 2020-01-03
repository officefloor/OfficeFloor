package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Reference to a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionReference {

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the type of argument to be passed to the referenced
	 * {@link ManagedFunction}.
	 * 
	 * @return Type of argument to be passed to the referenced
	 *         {@link ManagedFunction}.
	 */
	Class<?> getArgumentType();

}