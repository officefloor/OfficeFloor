/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.ProcedureVariableType;
import net.officefloor.plugin.variable.Var;

/**
 * {@link ProcedureVariableType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureVariableTypeImpl implements ProcedureVariableType {

	/**
	 * Name of {@link Var}.
	 */
	private final String variableName;

	/**
	 * Type of {@link Var}.
	 */
	private final String variableType;

	/**
	 * Instantiate.
	 * 
	 * @param variableName Name of {@link Var}.
	 * @param variableType Type of {@link Var}.
	 */
	public ProcedureVariableTypeImpl(String variableName, String variableType) {
		this.variableName = variableName;
		this.variableType = variableType;
	}

	/*
	 * ================== ProcedureVariableType ==================
	 */

	@Override
	public String getVariableName() {
		return this.variableName;
	}

	@Override
	public String getVariableType() {
		return this.variableType;
	}

}
