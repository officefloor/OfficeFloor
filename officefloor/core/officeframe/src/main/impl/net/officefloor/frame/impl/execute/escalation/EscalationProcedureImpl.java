package net.officefloor.frame.impl.execute.escalation;

import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;

/**
 * Implementation of the {@link EscalationProcedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationProcedureImpl implements EscalationProcedure {

	/**
	 * {@link EscalationFlow} instances in order for this procedure.
	 */
	private final EscalationFlow[] escalations;

	/**
	 * Initiate with {@link EscalationFlow} details.
	 * 
	 * @param escalations
	 *            {@link EscalationFlow} instances in order to be taken for this
	 *            procedure.
	 */
	public EscalationProcedureImpl(EscalationFlow... escalations) {
		this.escalations = escalations;
	}

	/*
	 * ============= EscalationProcedure ==================================
	 */

	@Override
	public EscalationFlow getEscalation(Throwable cause) {

		// Find the first matching escalation
		for (EscalationFlow escalation : this.escalations) {
			if (escalation.getTypeOfCause().isInstance(cause)) {
				// Use first matching
				return escalation;
			}
		}

		// Not found
		return null;
	}

}