/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.procedure;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureMethodContext;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link ProcedureSource} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockProcedureSource implements ProcedureSource, ProcedureSourceServiceFactory {

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
	 * Allow plug in to load {@link Method}.
	 */
	@FunctionalInterface
	public static interface LoadMethod {

		/**
		 * Loads the {@link Method}.
		 * 
		 * @param context {@link ProcedureMethodContext}.
		 * @return {@link Method}.
		 * @throws Exception Possible failure.
		 */
		Method loadMethod(ProcedureMethodContext context) throws Exception;
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
	 * ================== ProcedureSourceServiceFactory =================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ========================== ProcedureSource ======================
	 */

	@Override
	public String getSourceName() {
		return "Mock";
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {

		// Determine if mocking
		if (listProcedures != null) {
			listProcedures.listProcedures(context);
		}
	}

	@Override
	public Method loadMethod(ProcedureMethodContext context) throws Exception {

		// Determine if mocking
		if (loadMethod != null) {
			return loadMethod.loadMethod(context);
		}

		// As here, no mocking
		return null;
	}

}
