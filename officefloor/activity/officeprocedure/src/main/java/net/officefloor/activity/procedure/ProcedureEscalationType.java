package net.officefloor.activity.procedure;

import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * <code>Type definition</code> of a possible {@link EscalationFlow} by the
 * {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureEscalationType {

	/**
	 * Obtains the name for the {@link ProcedureEscalationType}.
	 * 
	 * @return Name for the {@link ProcedureEscalationType}.
	 */
	String getEscalationName();

	/**
	 * Obtains the type of {@link EscalationFlow} by the {@link Procedure}.
	 * 
	 * @return Type of {@link EscalationFlow} by the {@link Procedure}.
	 */
	Class<? extends Throwable> getEscalationType();

}