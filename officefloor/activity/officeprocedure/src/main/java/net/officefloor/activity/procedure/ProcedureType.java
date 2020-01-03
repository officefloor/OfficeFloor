package net.officefloor.activity.procedure;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.Var;

/**
 * <code>Type definition</code> of a {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureType {

	/**
	 * Obtains the name of the {@link Procedure}.
	 * 
	 * @return Name of the {@link Procedure}.
	 */
	String getProcedureName();

	/**
	 * Obtains the type name of the {@link Parameter}.
	 * 
	 * @return Type name of the {@link Parameter}. May be <code>null</code> if no
	 *         {@link Parameter}.
	 */
	Class<?> getParameterType();

	/**
	 * Obtains the {@link ProcedureObjectType} definitions for the dependent
	 * {@link Object} instances required by the {@link Procedure}.
	 * 
	 * @return {@link ProcedureObjectType} definitions for the dependent
	 *         {@link Object} instances required by the {@link Procedure}.
	 */
	ProcedureObjectType[] getObjectTypes();

	/**
	 * Obtains the {@link ProcedureVariableType} definitions for the {@link Var}
	 * instances required by the {@link Procedure}.
	 * 
	 * @return {@link ProcedureVariableType} definitions for the {@link Var}
	 *         instances required by the {@link Procedure}.
	 */
	ProcedureVariableType[] getVariableTypes();

	/**
	 * Obtains the {@link ProcedureFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link Procedure}.
	 * 
	 * @return {@link ProcedureFlowType} definitions for the possible {@link Flow}
	 *         instances instigated by the {@link Procedure}.
	 */
	ProcedureFlowType[] getFlowTypes();

	/**
	 * Obtains the {@link ProcedureEscalationType} definitions for the possible
	 * {@link EscalationFlow} instances by the {@link Procedure}.
	 * 
	 * @return {@link ProcedureEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the {@link Procedure}.
	 */
	ProcedureEscalationType[] getEscalationTypes();

	/**
	 * Obtains the type name of {@link Object} passed to the {@link Next}
	 * {@link ManagedFunction}.
	 * 
	 * @return Type name of {@link Object} passed to the {@link Next}
	 *         {@link ManagedFunction}. May be <code>null</code> if no argument.
	 */
	Class<?> getNextArgumentType();

}