package net.officefloor.activity.procedure;

import net.officefloor.plugin.variable.Var;

/**
 * <code>Type definition</code> of {@link Var} required by the
 * {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureVariableType {

	/**
	 * Obtains the name for the {@link Var}.
	 * 
	 * @return Name for the {@link Var}.
	 */
	String getVariableName();

	/**
	 * Obtains the type of the {@link Var}.
	 * 
	 * @return Type of the {@link Var}.
	 */
	String getVariableType();

}