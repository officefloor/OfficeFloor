package net.officefloor.plugin.managedfunction.method;

import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil.MethodResult;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link MethodParameterManufacturer} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockParameterManufacturer
		implements MethodParameterManufacturer, MethodParameterManufacturerServiceFactory {

	/**
	 * Logic.
	 */
	@FunctionalInterface
	public static interface Logic {

		/**
		 * Runs logic.
		 * 
		 * @return {@link MethodResult}.
		 * @throws Exception Possible failure.
		 */
		MethodResult run() throws Exception;
	}

	/**
	 * {@link MethodParameterManufacturer} to use.
	 */
	private static MethodParameterManufacturer manufacturer = null;

	/**
	 * Runs the {@link Logic} using the {@link MethodParameterManufacturer}.
	 * 
	 * @param parameterManufacturer {@link MethodParameterManufacturer}.
	 * @param logic                 {@link Logic}.
	 * @return {@link MethodResult}.
	 * @throws Exception If {@link Logic} fails.
	 */
	public static MethodResult run(MethodParameterManufacturer parameterManufacturer, Logic logic) throws Exception {
		try {
			manufacturer = parameterManufacturer;

			// Undertake logic with manufacturer
			return logic.run();

		} finally {
			manufacturer = null;
		}
	}

	/*
	 * ================ MethodParameterManufacturerServiceFactory =================
	 */

	@Override
	public MethodParameterManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== MethodParameterManufacturer ==========================
	 */

	@Override
	public MethodParameterFactory createParameterFactory(MethodParameterManufacturerContext context) throws Exception {

		// Determine if configured
		if (manufacturer == null) {
			return null;
		}

		// Create the parameter factory
		return manufacturer.createParameterFactory(context);
	}

}