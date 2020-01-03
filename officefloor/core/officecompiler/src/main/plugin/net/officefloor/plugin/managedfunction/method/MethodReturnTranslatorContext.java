package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Method;

import net.officefloor.frame.api.function.ManagedFunctionContext;

/**
 * Context for the {@link MethodReturnTranslator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodReturnTranslatorContext<R, T> {

	/**
	 * Obtains the return value from the {@link Method}.
	 * 
	 * @return Return value from the {@link Method}.
	 */
	R getReturnValue();

	/**
	 * Specifies the translated return value.
	 * 
	 * @param value Translated return value.
	 */
	void setTranslatedReturnValue(T value) throws Exception;

	/**
	 * Obtains the {@link ManagedFunctionContext}.
	 * 
	 * @return {@link ManagedFunctionContext}.
	 */
	ManagedFunctionContext<?, ?> getManagedFunctionContext();

}