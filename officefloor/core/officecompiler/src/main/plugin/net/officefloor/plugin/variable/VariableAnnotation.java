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
	 * Instantiate.
	 * 
	 * @param name Name for the {@link Var}.
	 */
	public VariableAnnotation(String name) {
		this.name = name;
	}

	/**
	 * Obtains the name of the {@link Var}.
	 * 
	 * @return Name of the {@link Var}.
	 */
	public String getVariableName() {
		return name;
	}

}