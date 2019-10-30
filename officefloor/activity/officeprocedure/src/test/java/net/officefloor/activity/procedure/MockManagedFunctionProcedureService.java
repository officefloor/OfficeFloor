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
	 * Service name.
	 */
	public static final String SERVICE_NAME = "MockManagedFunction";

	/**
	 * Allow plug in means to list {@link Procedure} instances.
	 */
	@FunctionalInterface
	public static interface ListProcedures {

		/**
		 * Lists the {@link Procedure} instances.
		 * 
		 * @param context {@link ProcedureListContext}.
		 * @throws Exception Possible failure.
		 */
		void listProcedures(ProcedureListContext context) throws Exception;
	}

	/**
	 * Allow listing mock {@link Procedure} instances.
	 */
	private static ListProcedures listProcedures = null;

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
	 * @param procedureListing      Mocks listing the {@link Procedure} instances.
	 * @param managedFunctionLoader Loads the {@link Method}.
	 * @param logic                 {@link Logic}.
	 * @return Result.
	 * @throws T Possible failure.
	 */
	public static <R, T extends Throwable> R run(ListProcedures procedureListing,
			LoadManagedFunction managedFunctionLoader, Logic<R, T> logic) throws T {
		try {
			// Setup to run
			listProcedures = procedureListing;
			loadManagedFunction = managedFunctionLoader;

			// Run the test logic
			return logic.run();

		} finally {
			// Reset
			listProcedures = null;
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
		return SERVICE_NAME;
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {
		
		// Determine if mocking
		if (listProcedures != null) {
			listProcedures.listProcedures(context);
		}
	}

	@Override
	public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {

		// Determine if mocking
		if (loadManagedFunction != null) {
			loadManagedFunction.loadManagedFunction(context);
		}
	}

}