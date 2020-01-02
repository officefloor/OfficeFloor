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
package net.officefloor.scala;

import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureMethodContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Scala {@link ProcedureSourceServiceFactory}.
 */
public class ScalaProcedureSourceServiceFactory implements ProcedureSourceServiceFactory {

	/**
	 * Scala source name.
	 */
	public static final String SOURCE_NAME = "Scala";

	/**
	 * Obtains the <code>MODULE</code> from object {@link Class}.
	 *
	 * @param resource Resource.
	 * @param context  {@link ServiceContext}.
	 * @return <code>MODULE</code> or <code>null</code>.
	 * @throws Exception If fails to obtain <code>MODULE</code>.
	 */
	public static Object getModule(String resource, ServiceContext context) throws Exception {

		// Obtain the possible object class
		Class<?> objectClass = context.loadOptionalClass(resource);
		if (objectClass == null) {
			return null; // not class
		}

		// Determine if Scala object (to obtain function)
		final String MODULE$_FIELD_NAME = "MODULE$";
		for (Field field : objectClass.getFields()) {
			if (MODULE$_FIELD_NAME.equals(field.getName())) {

				// Found module for functions
				return field.get(null);
			}
		}

		// No module
		return null;
	}

	/*
	 * =================== ProcedureSourceServiceFactory ================
	 */

	@Override
	public ProcedureSource createService(ServiceContext serviceContext) throws Throwable {
		return new ScalaProcedureSource(serviceContext);
	}

	/**
	 * Scala {@link ProcedureSource}.
	 */
	private static class ScalaProcedureSource implements ProcedureSource {

		/**
		 * {@link ServiceContext}.
		 */
		private final ServiceContext context;

		/**
		 * Instantiate.
		 *
		 * @param context {@link ServiceContext}.
		 */
		private ScalaProcedureSource(ServiceContext context) {
			this.context = context;
		}

		/*
		 * ================ ProcedureSource =======================
		 */

		@Override
		public String getSourceName() {
			return SOURCE_NAME;
		}

		@Override
		public void listProcedures(ProcedureListContext procedureListContext) throws Exception {

			// Obtain the module
			Object module = getModule(procedureListContext.getResource(), this.context);
			if (module == null) {
				return; // not Scala module
			}

			// Load procedures from module (exclude Scala helper methods)
			ProcedureEmployer.listMethods(module.getClass(), (method) -> method.getName().startsWith("$"),
					(method) -> procedureListContext.addProcedure(method.getName()));
		}

		@Override
		public Method loadMethod(ProcedureMethodContext procedureMethodContext) throws Exception {

			// Obtain the module
			Object module = getModule(procedureMethodContext.getResource(), this.context);
			if (module == null) {
				return null; // not module
			}

			// Should invoke from module
			procedureMethodContext.setMethodObjectInstanceFactory((context) -> module);

			// Obtain the function
			String procedureName = procedureMethodContext.getProcedureName();
			for (Method method : module.getClass().getMethods()) {
				if (procedureName.equals(method.getName())) {
					return method; // found function
				}
			}

			// As here, no method
			return null;
		}
	}
}
