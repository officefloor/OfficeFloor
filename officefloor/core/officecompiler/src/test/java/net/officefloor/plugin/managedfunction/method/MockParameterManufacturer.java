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