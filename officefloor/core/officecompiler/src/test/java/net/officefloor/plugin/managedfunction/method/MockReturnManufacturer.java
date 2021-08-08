/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.plugin.managedfunction.method;

import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil.MethodResult;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.method.MethodReturnManufacturer;
import net.officefloor.plugin.clazz.method.MethodReturnManufacturerContext;
import net.officefloor.plugin.clazz.method.MethodReturnManufacturerServiceFactory;
import net.officefloor.plugin.clazz.method.MethodReturnTranslator;

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
