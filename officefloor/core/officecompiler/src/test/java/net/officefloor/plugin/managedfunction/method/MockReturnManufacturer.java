/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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