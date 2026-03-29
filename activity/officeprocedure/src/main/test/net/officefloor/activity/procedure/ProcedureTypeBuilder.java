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

package net.officefloor.activity.procedure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.variable.Var;

/**
 * Builder of expected {@link ProcedureType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureTypeBuilder {

	/**
	 * Adds a {@link ProcedureObjectType}.
	 * 
	 * @param objectName    Name of {@link Object}.
	 * @param objectType    {@link Object} type.
	 * @param typeQualifier Type qualifier. May be <code>null</code>.
	 */
	void addObjectType(String objectName, Class<?> objectType, String typeQualifier);

	/**
	 * Convenience method to add a {@link ProcedureVariableType} defaulting the
	 * name.
	 * 
	 * @param variableType Type of {@link Var}.
	 */
	void addVariableType(String variableType);

	/**
	 * Adds a {@link ProcedureVariableType}.
	 * 
	 * @param variableName Name of {@link Var}.
	 * @param variableType Type of {@link Var}.
	 */
	void addVariableType(String variableName, String variableType);

	/**
	 * Adds a {@link ProcedureVariableType}.
	 * 
	 * @param variableName Name of {@link Var}.
	 * @param variableType Type of {@link Var}.
	 */
	void addVariableType(String variableName, Class<?> variableType);

	/**
	 * Adds a {@link ProcedureFlowType}.
	 * 
	 * @param flowName     Name of {@link Flow}.
	 * @param argumentType Possible argument type. May be <code>null</code> for no
	 *                     argument.
	 */
	void addFlowType(String flowName, Class<?> argumentType);

	/**
	 * Convenience method to add {@link ProcedureEscalationType} defaulting the
	 * name.
	 * 
	 * @param escalationType Escalation type.
	 */
	void addEscalationType(Class<? extends Throwable> escalationType);

	/**
	 * Adds a {@link ProcedureEscalationType}.
	 * 
	 * @param escalationName Name of {@link EscalationFlow}.
	 * @param escalationType {@link Escalation} type.
	 */
	void addEscalationType(String escalationName, Class<? extends Throwable> escalationType);

	/**
	 * Specifies the next argument type.
	 * 
	 * @param nextArgumentType Next argument type.
	 */
	void setNextArgumentType(Class<?> nextArgumentType);

	/**
	 * Builds the {@link ProcedureType}.
	 * 
	 * @return {@link ProcedureType}
	 */
	ProcedureType build();

}
