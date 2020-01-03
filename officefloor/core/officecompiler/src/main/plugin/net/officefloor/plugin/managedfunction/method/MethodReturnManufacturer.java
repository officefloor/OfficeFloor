package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Method;

/**
 * Manufactures the {@link MethodReturnTranslator}.
 * 
 * @param <R> {@link MethodFunction} return type.
 * @param <T> Translated type.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodReturnManufacturer<R, T> {

	/**
	 * <p>
	 * Creates the {@link MethodReturnTranslator} for the particular {@link Method}
	 * return.
	 * <p>
	 * Should the {@link MethodReturnManufacturer} not handle the return value, it
	 * should return <code>null</code>. This is because the first
	 * {@link MethodReturnManufacturer} providing a {@link MethodReturnTranslator}
	 * will be used.
	 * 
	 * @param context {@link MethodReturnManufacturerContext}.
	 * @return {@link MethodReturnTranslator} or <code>null</code> if not able to
	 *         handle return value.
	 * @throws Exception If fails to create the {@link MethodReturnTranslator}.
	 */
	MethodReturnTranslator<R, T> createReturnTranslator(MethodReturnManufacturerContext<T> context) throws Exception;

}