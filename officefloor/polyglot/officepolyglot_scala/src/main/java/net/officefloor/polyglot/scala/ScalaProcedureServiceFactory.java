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
package net.officefloor.polyglot.scala;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureServiceContext;
import net.officefloor.activity.procedure.spi.ProcedureServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Scala {@link ProcedureServiceFactory}.
 */
public class ScalaProcedureServiceFactory implements ProcedureServiceFactory {

	/**
	 * Scala service name.
	 */
	public static final String SERVICE_NAME = "Scala";

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
	 * =================== ProcedureServiceFactory ================
	 */

	@Override
	public ProcedureService createService(ServiceContext serviceContext) throws Throwable {
		return new ScalaProcedureService(serviceContext);
	}

	/**
	 * Scala {@link ProcedureService}.
	 */
	private static class ScalaProcedureService implements ProcedureService {

		/**
		 * {@link ServiceContext}.
		 */
		private final ServiceContext context;

		/**
		 * Instantiate.
		 *
		 * @param context {@link ServiceContext}.
		 */
		private ScalaProcedureService(ServiceContext context) {
			this.context = context;
		}

		/*
		 * ================ ProcedureService =======================
		 */

		@Override
		public String[] listProcedures(String resource) throws Exception {

			// Obtain the module
			Object module = getModule(resource, this.context);
			if (module == null) {
				return null; // not Scala module
			}

			// Load procedures from module (exclude Scala helper methods)
			return ProcedureEmployer.listProcedureNames(module.getClass(),
					(method) -> method.getName().startsWith("$"));
		}

		@Override
		public String getServiceName() {
			return SERVICE_NAME;
		}

		@Override
		public Method loadMethod(ProcedureServiceContext procedureServiceContext) throws Exception {

			// Obtain the module
			Object module = getModule(procedureServiceContext.getResource(), this.context);
			if (module == null) {
				return null; // not module
			}

			// Should invoke from module
			procedureServiceContext.setMethodObjectInstanceFactory((context) -> module);

			// Obtain the function
			String procedureName = procedureServiceContext.getProcedureName();
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
