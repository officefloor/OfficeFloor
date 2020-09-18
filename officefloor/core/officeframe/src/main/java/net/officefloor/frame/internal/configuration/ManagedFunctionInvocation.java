package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Provides reference details to invoke a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionInvocation extends ManagedFunctionReference {

	/**
	 * Obtains the argument to invoke the {@link ManagedFunction}.
	 * 
	 * @return Argument to invoke the {@link ManagedFunction}.
	 */
	Object getArgument();

	/*
	 * ====================== ManagedFunctionReference ==================
	 */

	@Override
	default Class<?> getArgumentType() {

		// Provide argument type from the argument
		Object argument = this.getArgument();
		return argument != null ? argument.getClass() : null;
	}

}