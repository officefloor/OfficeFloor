package net.officefloor.frame.internal.structure;

/**
 * Procedure to undertake in resolving exceptional circumstances to normal
 * processing.
 * 
 * @author Daniel Sagenschneider
 */
public interface EscalationProcedure {

	/**
	 * Obtains the {@link EscalationFlow} for the cause within this
	 * {@link EscalationProcedure}.
	 * 
	 * @param cause
	 *            Cause.
	 * @return {@link EscalationFlow} for the cause.
	 */
	EscalationFlow getEscalation(Throwable cause);

}