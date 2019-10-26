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
package net.officefloor.polyglot.kotlin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureServiceContext;
import net.officefloor.activity.procedure.spi.ProcedureServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Kotlin {@link ProcedureServiceFactory}.
 *
 * @author Daniel Sagenschneider
 */
public class KotlinProcedureServiceFactory implements ProcedureServiceFactory {

	/**
	 * Service name.
	 */
	public static final String SERVICE_NAME = "Kotlin";

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
	 * ====================== ProcedureServiceFactory ======================
	 */

	@Override
	public ProcedureService createService(ServiceContext serviceContext) throws Throwable {
		return new KotlinProcedureService(serviceContext);
	}

	/**
	 * Kotlin {@link ProcedureService}.
	 */
	private static class KotlinProcedureService implements ProcedureService {

		/**
		 * {@link ServiceContext}.
		 */
		private final ServiceContext serviceContext;

		/**
		 * Instantiate.
		 *
		 * @param serviceContext {@link ServiceContext}.
		 */
		private KotlinProcedureService(ServiceContext serviceContext) {
			this.serviceContext = serviceContext;
		}

		/*
		 * ==================== ProcedureService ============================
		 */

		@Override
		public String getServiceName() {
			return SERVICE_NAME;
		}

		@Override
		public String[] listProcedures(String resource) throws Exception {

			// Obtain the Kotlin functions
			Class<?> functionsClass = getKotlinFunctions(resource, this.serviceContext);
			if (functionsClass == null) {
				return null; // not Kotlin functions
			}

			// Return the procedures (Kotlin static methods)
			return ProcedureEmployer.listProcedureNames(functionsClass,
					(method) -> !Modifier.isStatic(method.getModifiers()));
		}

		@Override
		public Method loadMethod(ProcedureServiceContext procedureServiceContext) throws Exception {

			// Obtain the Kotlin functions
			Class<?> functionClass = getKotlinFunctions(procedureServiceContext.getResource(), this.serviceContext);
			if (functionClass == null) {
				return null; // no Kotlin functions
			}

			// Should always be static
			procedureServiceContext.setMethodObjectInstanceFactory(null);

			// Obtain the function
			String procedureName = procedureServiceContext.getProcedureName();
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