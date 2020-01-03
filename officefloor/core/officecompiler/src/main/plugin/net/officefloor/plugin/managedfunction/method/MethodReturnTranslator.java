package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Method;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Translate the return value of the {@link MethodFunction}.
 * 
 * @param <R> {@link MethodFunction} return type.
 * @param <T> Translated type.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodReturnTranslator<R, T> {

	/**
	 * Translates the {@link Method} return value for next {@link ManagedFunction}
	 * argument.
	 * 
	 * @param context {@link MethodReturnTranslatorContext}.
	 * @throws Exception If fails to translate.
	 */
	void translate(MethodReturnTranslatorContext<R, T> context) throws Exception;

}