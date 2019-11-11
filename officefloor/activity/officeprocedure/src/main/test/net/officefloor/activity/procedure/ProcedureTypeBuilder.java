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
	 * Adds a {@link ProcedureFlowType}.
	 * 
	 * @param flowName     Name of {@link Flow}.
	 * @param argumentType Possible argument type. May be <code>null</code> for no
	 *                     argument.
	 */
	void addFlowType(String flowName, Class<?> argumentType);

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

}