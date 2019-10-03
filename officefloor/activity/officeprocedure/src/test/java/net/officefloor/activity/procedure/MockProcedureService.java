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

import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureServiceContext;
import net.officefloor.activity.procedure.spi.ProcedureServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link ProcedureService} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockProcedureService implements ProcedureService, ProcedureServiceFactory {

	/**
	 * Allow plug in means to list {@link Procedure} instances.
	 */
	@FunctionalInterface
	public static interface ListProcedures {

		/**
		 * Lists the {@link Procedure} instances.
		 * 
		 * @param clazz {@link Class}.
		 * @return {@link Procedure} listing.
		 * @throws Exception Possible failure.
		 */
		String[] listProcedures(Class<?> clazz) throws Exception;
	}

	/**
	 * Allow listing mock {@link Procedure} instances.
	 */
	private static ListProcedures listProcedures = null;

	/**
	 * Allow plug in to load {@link Method}.
	 */
	@FunctionalInterface
	public static interface LoadMethod {

		/**
		 * Loads the {@link Method}.
		 * 
		 * @param context {@link ProcedureServiceContext}.
		 * @return {@link Method}.
		 * @throws Exception Possible failure.
		 */
		Method loadMethod(ProcedureServiceContext context) throws Exception;
	}

	/**
	 * Allow loading {@link Method}.
	 */
	private static LoadMethod loadMethod = null;

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
	 * @param <R>              Result type.
	 * @param <T>              Possible failure type.
	 * @param procedureListing Mocks listing the {@link Procedure} instances.
	 * @param methodLoader     Loads the {@link Method}.
	 * @param logic            {@link Logic}.
	 * @return Result.
	 * @throws T Possible failure.
	 */
	public static <R, T extends Throwable> R run(ListProcedures procedureListing, LoadMethod methodLoader,
			Logic<R, T> logic) throws T {
		try {
			// Setup to run
			listProcedures = procedureListing;
			loadMethod = methodLoader;

			// Run the test logic
			return logic.run();

		} finally {
			// Reset
			listProcedures = null;
			loadMethod = null;
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
		return "Mock";
	}

	@Override
	public String[] listProcedures(Class<?> clazz) throws Exception {

		// Determine if mocking
		if (listProcedures != null) {
			return listProcedures.listProcedures(clazz);
		}

		// As here, no mocking
		return null;
	}

	@Override
	public Method loadMethod(ProcedureServiceContext context) throws Exception {

		// Determine if mocking
		if (loadMethod != null) {
			return loadMethod.loadMethod(context);
		}

		// As here, no mocking
		return null;
	}

}