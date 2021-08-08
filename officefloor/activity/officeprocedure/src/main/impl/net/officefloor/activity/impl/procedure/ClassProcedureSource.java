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

package net.officefloor.activity.impl.procedure;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureMethodContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;

/**
 * {@link ProcedureSource} for {@link Class}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassProcedureSource implements ProcedureSource {

	/**
	 * Source name.
	 */
	public static final String SOURCE_NAME = "Class";

	/*
	 * ======================== ProcedureSource ===========================
	 */

	@Override
	public String getSourceName() {
		return SOURCE_NAME;
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
		Class<?> clazz = context.getResource();

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
