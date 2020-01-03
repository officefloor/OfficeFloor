package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.ProcedureEscalationType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * {@link ProcedureEscalationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureEscalationTypeImpl implements ProcedureEscalationType {

	/**
	 * Name of {@link EscalationFlow}.
	 */
	private final String escalationName;

	/**
	 * Type of {@link Escalation}.
	 */
	private final Class<? extends Throwable> escalationType;

	/**
	 * Instantiate.
	 * 
	 * @param escalationName Name of {@link EscalationFlow}.
	 * @param escalationType Type of {@link Escalation}.
	 */
	public ProcedureEscalationTypeImpl(String escalationName, Class<? extends Throwable> escalationType) {
		this.escalationName = escalationName;
		this.escalationType = escalationType;
	}

	/*
	 * =============== ProcedureEscalationType ===================
	 */

	@Override
	public String getEscalationName() {
		return this.escalationName;
	}

	@Override
	public Class<? extends Throwable> getEscalationType() {
		return this.escalationType;
	}
}