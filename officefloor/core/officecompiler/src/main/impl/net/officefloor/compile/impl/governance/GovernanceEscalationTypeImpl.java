package net.officefloor.compile.impl.governance;

import net.officefloor.compile.governance.GovernanceEscalationType;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * {@link GovernanceEscalationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceEscalationTypeImpl implements GovernanceEscalationType {

	/**
	 * Name of {@link EscalationFlow}.
	 */
	private final String escalationName;

	/**
	 * Type of the {@link EscalationFlow}.
	 */
	private final Class<?> escalationType;

	/**
	 * Initiate.
	 * 
	 * @param escalationName
	 *            Name of {@link EscalationFlow}.
	 * @param escalationType
	 *            Type of the {@link EscalationFlow}.
	 */
	public GovernanceEscalationTypeImpl(String escalationName,
			Class<?> escalationType) {
		this.escalationName = escalationName;
		this.escalationType = escalationType;
	}

	/*
	 * =================== GovernanceEscalationType ==========================
	 */

	@Override
	public String getEscalationName() {
		return this.escalationName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Throwable> Class<E> getEscalationType() {
		return (Class<E>) this.escalationType;
	}

}