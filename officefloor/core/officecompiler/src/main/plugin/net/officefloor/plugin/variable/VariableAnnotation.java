/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.variable;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;

/**
 * Annotation for {@link Var}.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableAnnotation {

	/**
	 * Extracts the possible variable name.
	 * 
	 * @param objectType {@link ManagedFunctionObjectType}.
	 * @return Variable name or <code>null</code> if not a variable.
	 */
	public static String extractPossibleVariableName(ManagedFunctionObjectType<?> objectType) {

		// Extract variable name
		VariableAnnotation annotation = objectType.getAnnotation(VariableAnnotation.class);
		if (annotation != null) {
			return annotation.getVariableName();
		}

		// As here, not variable
		return null;
	}

	/**
	 * Name for the {@link Var}.
	 */
	private final String name;

	/**
	 * Type for the {@link Var}.
	 */
	private final String type;

	/**
	 * Instantiate.
	 * 
	 * @param name Name for the {@link Var}.
	 * @param type Type for the {@link Var}.
	 */
	public VariableAnnotation(String name, String type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Obtains the name of the {@link Var}.
	 * 
	 * @return Name of the {@link Var}.
	 */
	public String getVariableName() {
		return this.name;
	}

	/**
	 * Obtains the type of the {@link Var}.
	 * 
	 * @return Type of the {@link Var}.
	 */
	public String getVariableType() {
		return this.type;
	}

}
