/*-
 * #%L
 * Kotlin
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
			Class<?> functionClass = getKotlinFunctions(procedureMethodContext.getResource().getName(), this.serviceContext);
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
