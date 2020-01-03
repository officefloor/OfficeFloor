package net.officefloor.plugin.managedfunction.method;

/**
 * Manufactures the {@link MethodParameterFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodParameterManufacturer {

	/**
	 * <p>
	 * Creates the {@link MethodParameterFactory} for the particular
	 * parameter.
	 * <p>
	 * Should the {@link MethodParameterManufacturer} not handled the parameter, it should
	 * return <code>null</code>. This is because the first
	 * {@link MethodParameterManufacturer} providing a
	 * {@link MethodParameterFactory} will be used.
	 * 
	 * @param context {@link MethodParameterManufacturerContext}.
	 * @return {@link MethodParameterFactory} or <code>null</code> if not
	 *         able to handle parameter.
	 * @throws Exception If fails to create the
	 *                   {@link MethodParameterFactory}.
	 */
	MethodParameterFactory createParameterFactory(MethodParameterManufacturerContext context) throws Exception;

}