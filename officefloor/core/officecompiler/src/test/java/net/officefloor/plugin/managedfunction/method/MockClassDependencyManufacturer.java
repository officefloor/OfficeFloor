/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
