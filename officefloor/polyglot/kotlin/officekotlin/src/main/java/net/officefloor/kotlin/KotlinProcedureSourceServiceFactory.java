/*-
 * #%L
 * Kotlin
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

package net.officefloor.kotlin;

import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureMethodContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Kotlin {@link ProcedureSourceServiceFactory}.
 *
 * @author Daniel Sagenschneider
 */
public class KotlinProcedureSourceServiceFactory implements ProcedureSourceServiceFactory {

	/**
	 * Source name.
	 */
	public static final String SOURCE_NAME = "Kotlin";

	/**
	 * Obtains the Kotlin functions {@link Class}.
	 *
	 * @param resource Resource.
	 * @param context  {@link ServiceContext}.
	 * @return Kotlin functions {@link Class} or <code>null</code>.
	 */
	public static Class<?> getKotlinFunctions(String resource, ServiceContext context) {

		// Ensure kotlin functions
		if (!resource.endsWith("Kt")) {
			return null; // not top level functions
		}

		// Return the possible functions class
		return context.loadOptionalClass(resource);
	}

	/*
	 * ====================== ProcedureSourceServiceFactory ======================
	 */

	@Override
	public ProcedureSource createService(ServiceContext serviceContext) throws Throwable {
		return new KotlinProcedureSource(serviceContext);
	}

	/**
	 * Kotlin {@link ProcedureSource}.
	 */
	private static class KotlinProcedureSource implements ProcedureSource {

		/**
		 * {@link ServiceContext}.
		 */
		private final ServiceContext serviceContext;

		/**
		 * Instantiate.
		 *
		 * @param serviceContext {@link ServiceContext}.
		 */
		private KotlinProcedureSource(ServiceContext serviceContext) {
			this.serviceContext = serviceContext;
		}

		/*
		 * ==================== ProcedureSource ============================
		 */

		@Override
		public String getSourceName() {
			return SOURCE_NAME;
		}

		@Override
		public void listProcedures(ProcedureListContext procedureListContext) throws Exception {

			// Obtain the Kotlin functions
			Class<?> functionsClass = getKotlinFunctions(procedureListContext.getResource(), this.serviceContext);
			if (functionsClass == null) {
				return; // not Kotlin functions
			}

			// Load the procedures (Kotlin static methods)
			ProcedureEmployer.listMethods(functionsClass, (method) -> !Modifier.isStatic(method.getModifiers()),
					(method) -> procedureListContext.addProcedure(method.getName()));
		}

		@Override
		public Method loadMethod(ProcedureMethodContext procedureMethodContext) throws Exception {

			// Obtain the Kotlin functions
			Class<?> functionClass = getKotlinFunctions(procedureMethodContext.getResource(), this.serviceContext);
			if (functionClass == null) {
				return null; // no Kotlin functions
			}

			// Should always be static
			procedureMethodContext.setMethodObjectInstanceFactory(null);

			// Obtain the function
			String procedureName = procedureMethodContext.getProcedureName();
			for (Method method : functionClass.getMethods()) {
				if (procedureName.equals(method.getName())) {
					return method; // found function
				}
			}

			// As here, no method
			return null;
		}
	}
}
