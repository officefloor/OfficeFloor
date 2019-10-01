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
package net.officefloor.activity.procedure.java;

import java.util.Arrays;

import net.officefloor.activity.procedure.ProcedureService;
import net.officefloor.activity.procedure.ProcedureServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link ProcedureService} for {@link Class}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassProcedureServiceFactory implements ProcedureServiceFactory {

	/*
	 * ====================== ProcedureServiceFactory =====================
	 */

	@Override
	public ProcedureService createService(ServiceContext context) throws Throwable {
		return new ClassProcedureService(context);
	}

	/**
	 * {@link Class} {@link ProcedureService}.
	 */
	private static class ClassProcedureService implements ProcedureService {

		/**
		 * {@link ServiceContext}.
		 */
		private final ServiceContext context;

		/**
		 * Instantiate.
		 * 
		 * @param context {@link ServiceContext}.
		 */
		private ClassProcedureService(ServiceContext context) {
			this.context = context;
		}

		/*
		 * ======================== ProcedureService ===========================
		 */

		@Override
		public String getServiceName() {
			return "Class";
		}

		@Override
		public String[] listProcedures(String className) {

			// Load the class
			Class<?> clazz = context.loadClass(className);

			// Provide all public (non-object methods)
			return Arrays.stream(clazz.getMethods())
					.filter((method) -> !Object.class.equals(method.getDeclaringClass()))
					.map((method) -> method.getName()).toArray(String[]::new);
		}
	}
}