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

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureEscalationType;
import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.activity.procedure.ProcedureObjectType;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.ProcedureVariableType;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * {@link ProcedureType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureTypeImpl implements ProcedureType {

	/**
	 * Name of {@link Procedure}.
	 */
	private final String procedureName;

	/**
	 * {@link Parameter} type for {@link Procedure}.
	 */
	private final Class<?> parameterType;

	/**
	 * {@link ProcedureObjectType} instances.
	 */
	private final ProcedureObjectType[] objectTypes;

	/**
	 * {@link ProcedureVariableType} instances.
	 */
	private final ProcedureVariableType[] variableTypes;

	/**
	 * {@link ProcedureFlowType} instances.
	 */
	private final ProcedureFlowType[] flowTypes;

	/**
	 * {@link ProcedureEscalationType} instances.
	 */
	private final ProcedureEscalationType[] escalationTypes;

	/**
	 * Next argument type.
	 */
	private final Class<?> nextArgumentType;

	/**
	 * Instantiate.
	 * 
	 * @param procedureName    Name of {@link Procedure}.
	 * @param parameterType    {@link Parameter} type for {@link Procedure}.
	 * @param objectTypes      {@link ProcedureObjectType} instances.
	 * @param variableTypes    {@link ProcedureVariableType} instances.
	 * @param flowTypes        {@link ProcedureFlowType} instances.
	 * @param escalationTypes  {@link ProcedureEscalationType} instances.
	 * @param nextArgumentType Next argument type.
	 */
	public ProcedureTypeImpl(String procedureName, Class<?> parameterType, ProcedureObjectType[] objectTypes,
			ProcedureVariableType[] variableTypes, ProcedureFlowType[] flowTypes,
			ProcedureEscalationType[] escalationTypes, Class<?> nextArgumentType) {
		this.procedureName = procedureName;
		this.parameterType = parameterType;
		this.objectTypes = objectTypes;
		this.variableTypes = variableTypes;
		this.flowTypes = flowTypes;
		this.escalationTypes = escalationTypes;
		this.nextArgumentType = nextArgumentType;
	}

	/*
	 * ================ ProcedureType =====================
	 */

	@Override
	public String getProcedureName() {
		return this.procedureName;
	}

	@Override
	public Class<?> getParameterType() {
		return this.parameterType;
	}

	@Override
	public ProcedureObjectType[] getObjectTypes() {
		return this.objectTypes;
	}

	@Override
	public ProcedureVariableType[] getVariableTypes() {
		return this.variableTypes;
	}

	@Override
	public ProcedureFlowType[] getFlowTypes() {
		return this.flowTypes;
	}

	@Override
	public ProcedureEscalationType[] getEscalationTypes() {
		return this.escalationTypes;
	}

	@Override
	public Class<?> getNextArgumentType() {
		return this.nextArgumentType;
	}

}