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
package net.officefloor.activity.impl.procedure;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureMethodContext;
import net.officefloor.activity.procedure.spi.ProcedureService;

/**
 * {@link ProcedureService} for {@link Class}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassProcedureService implements ProcedureService {

	/**
	 * Service name.
	 */
	public static final String SERVICE_NAME = "Class";

	/*
	 * ======================== ProcedureService ===========================
	 */

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public void listProcedures(ProcedureListContext context) {

		// Load the class
		Class<?> clazz = context.getSourceContext().loadOptionalClass(context.getResource());
		if (clazz == null) {
			return; // no procedures
		}

		// Provide all public methods
		ProcedureEmployer.listMethods(clazz, null, (method) -> context.addProcedure(method.getName()));
	}

	@Override
	public Method loadMethod(ProcedureMethodContext context) throws Exception {

		// Obtain the class
		Class<?> clazz = context.getSourceContext().loadClass(context.getResource());

		// Find the method
		String methodName = context.getProcedureName();
		for (Method method : clazz.getMethods()) {
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