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