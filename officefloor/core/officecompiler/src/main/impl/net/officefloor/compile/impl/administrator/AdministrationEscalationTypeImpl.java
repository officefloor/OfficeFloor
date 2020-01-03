package net.officefloor.compile.impl.administrator;

import net.officefloor.compile.administration.AdministrationEscalationType;
import net.officefloor.frame.api.escalate.Escalation;

/**
 * {@link AdministrationEscalationType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationEscalationTypeImpl implements AdministrationEscalationType {

	/**
	 * {@link Escalation} name.
	 */
	private final String escalationName;

	/**
	 * {@link Escalation} type.
	 */
	private final Class<? extends Throwable> escalationType;

	/**
	 * Instantiate.
	 * 
	 * @param escalationName
	 *            {@link Escalation} name.
	 * @param escalationType
	 *            {@link Escalation} type.
	 */
	public AdministrationEscalationTypeImpl(String escalationName, Class<? extends Throwable> escalationType) {
		this.escalationName = escalationName;
		this.escalationType = escalationType;
	}

	/*
	 * ================= AdministrationEscalationType ===============
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
