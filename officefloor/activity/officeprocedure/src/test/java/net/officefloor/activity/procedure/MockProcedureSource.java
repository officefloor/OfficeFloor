/*-
 * #%L
 * Procedure
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
