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
