package net.officefloor.plugin.managedfunction.method;

import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil.MethodResult;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link MethodReturnManufacturer} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockReturnManufacturer
		implements MethodReturnManufacturer<Object, Object>, MethodReturnManufacturerServiceFactory {

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
	 * {@link MethodReturnManufacturer} to use.
	 */
	@SuppressWarnings("rawtypes")
	private static MethodReturnManufacturer manufacturer = null;

	/**
	 * Translated return {@link Class}.
	 */
	@SuppressWarnings("rawtypes")
	private static Class translatedClass = null;

	/**
	 * Runs the {@link Logic} using the {@link MethodReturnManufacturer}.
	 * 
	 * @param returnManufacturer {@link MethodReturnManufacturer}.
	 * @param logic              {@link Logic}.
	 * @return {@link MethodResult}.
	 * @throws Exception If {@link Logic} fails.
	 */
	public static <R, T> MethodResult run(Class<R> matchReturnClass, Class<T> translatedReturnClass,
			MethodReturnManufacturer<R, T> returnManufacturer, Logic logic) throws Exception {
		try {
			translatedClass = translatedReturnClass;
			manufacturer = returnManufacturer;

			// Undertake logic with manufacturer
			return logic.run();

		} finally {
			translatedClass = null;
			manufacturer = null;
		}
	}

	/*
	 * ================ MethodReturnManufacturerServiceFactory =================
	 */

	@Override
	public MethodReturnManufacturer<?, ?> createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== MethodReturnManufacturer ==========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public MethodReturnTranslator<Object, Object> createReturnTranslator(
			MethodReturnManufacturerContext<Object> context) throws Exception {

		// Determine if configured
		if (manufacturer == null) {
			return null;
		}

		// Create the return translator
		MethodReturnTranslator<Object, Object> translator = manufacturer.createReturnTranslator(context);
		if (translator != null) {
			context.setTranslatedReturnClass(translatedClass);
		}

		// Return the translator
		return translator;
	}

}