/*-
 * #%L
 * OfficeCompiler
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
