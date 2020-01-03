package net.officefloor.activity.procedure;

import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Flow} possibly instigated by a
 * {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureFlowType {

	/**
	 * Obtains the name for the {@link ProcedureFlowType}.
	 * 
	 * @return Name for the {@link ProcedureFlowType}.
	 */
	String getFlowName();

	/**
	 * Obtains the type of the argument passed by the {@link Procedure} to the
	 * {@link Flow}.
	 * 
	 * @return Type of argument passed to {@link Flow}. May be <code>null</code> to
	 *         indicate no argument.
	 */
	Class<?> getArgumentType();

}