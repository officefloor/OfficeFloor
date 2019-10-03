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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureServiceContext;
import net.officefloor.activity.procedure.spi.ProcedureServiceFactory;
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
		return new ClassProcedureService();
	}

	/**
	 * {@link Class} {@link ProcedureService}.
	 */
	private static class ClassProcedureService implements ProcedureService {

		/*
		 * ======================== ProcedureService ===========================
		 */

		@Override
		public String getServiceName() {
			return "Class";
		}

		@Override
		public String[] listProcedures(Class<?> clazz) {

			// Provide all public (non-object methods)
			return Arrays.stream(clazz.getMethods())
					.filter((method) -> !Object.class.equals(method.getDeclaringClass()))
					.map((method) -> method.getName()).toArray(String[]::new);
		}

		@Override
		public Method loadMethod(ProcedureServiceContext context) throws Exception {

			// Find the method
			String methodName = context.getProcedureName();
			for (Method method : context.getInstanceClass().getMethods()) {
				if (method.getName().equals(methodName)) {

					// Found the method

					// Determine if static
					if (Modifier.isStatic(method.getModifiers())) {
						context.setMethodObjectInstanceFactory(null); // static
					}

					// Return the method
					return method;
				}
			}

			// Unable to find the method
			return null;
		}
	}
}