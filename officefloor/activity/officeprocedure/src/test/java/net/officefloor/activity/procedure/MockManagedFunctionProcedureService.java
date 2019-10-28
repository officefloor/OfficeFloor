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
package net.officefloor.activity.procedure;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureServiceFactory;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link ManagedFunctionProcedureService} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockManagedFunctionProcedureService implements ManagedFunctionProcedureService, ProcedureServiceFactory {

	/**
	 * Allow plug in to load {@link ManagedFunction}.
	 */
	@FunctionalInterface
	public static interface LoadManagedFunction {

		/**
		 * Loads the {@link Method}.
		 * 
		 * @param context {@link ProcedureManagedFunctionContext}.
		 * @throws Exception Possible failure.
		 */
		void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception;
	}

	/**
	 * Allow loading {@link ManagedFunction}.
	 */
	private static LoadManagedFunction loadManagedFunction = null;

	/**
	 * Logic.
	 */
	@FunctionalInterface
	public static interface Logic<R, T extends Throwable> {

		/**
		 * Runs the logic.
		 * 
		 * @return Result.
		 * @throws T Possible failure.
		 */
		R run() throws T;
	}

	/**
	 * Runs the mock {@link Logic}.
	 * 
	 * @param <R>                   Result type.
	 * @param <T>                   Possible failure type.
	 * @param managedFunctionLoader Loads the {@link Method}.
	 * @param logic                 {@link Logic}.
	 * @return Result.
	 * @throws T Possible failure.
	 */
	public static <R, T extends Throwable> R run(LoadManagedFunction managedFunctionLoader, Logic<R, T> logic)
			throws T {
		try {
			// Setup to run
			loadManagedFunction = managedFunctionLoader;

			// Run the test logic
			return logic.run();

		} finally {
			// Reset
			loadManagedFunction = null;
		}
	}

	/*
	 * ===================== ProcedureServiceFactory ====================
	 */

	@Override
	public ProcedureService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ========================== ProcedureService ======================
	 */

	@Override
	public String getServiceName() {
		return "MockManagedFunction";
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {
		// no procedures
	}

	@Override
	public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {

		// Determine if mocking
		if (loadManagedFunction != null) {
			loadManagedFunction.loadManagedFunction(context);
		}
	}

}