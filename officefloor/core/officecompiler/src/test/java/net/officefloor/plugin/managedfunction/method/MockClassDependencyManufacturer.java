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
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerServiceFactory;

/**
 * Mock {@link ClassDependencyManufacturer} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockClassDependencyManufacturer
		implements ClassDependencyManufacturer, ClassDependencyManufacturerServiceFactory {

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
	 * {@link ClassDependencyManufacturer} to use.
	 */
	private static ClassDependencyManufacturer manufacturer = null;

	/**
	 * Runs the {@link Logic} using the {@link ClassDependencyManufacturer}.
	 * 
	 * @param dependencyManufacturer {@link ClassDependencyManufacturer}.
	 * @param logic                  {@link Logic}.
	 * @return {@link MethodResult}.
	 * @throws Exception If {@link Logic} fails.
	 */
	public static MethodResult run(ClassDependencyManufacturer dependencyManufacturer, Logic logic) throws Exception {
		try {
			manufacturer = dependencyManufacturer;

			// Undertake logic with manufacturer
			return logic.run();

		} finally {
			manufacturer = null;
		}
	}

	/*
	 * ================ ClassDependencyManufacturerServiceFactory =================
	 */

	@Override
	public ClassDependencyManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== ClassDependencyManufacturer ==========================
	 */

	@Override
	public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

		// Determine if configured
		if (manufacturer == null) {
			return null;
		}

		// Create the parameter factory
		return manufacturer.createParameterFactory(context);
	}

}
