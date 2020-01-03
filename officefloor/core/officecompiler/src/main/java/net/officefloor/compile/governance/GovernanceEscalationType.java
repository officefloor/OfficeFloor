package net.officefloor.compile.governance;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.GovernanceActivity;

/**
 * <code>Type definition</code> of a possible {@link EscalationFlow} by the
 * {@link GovernanceActivity}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceEscalationType {

	/**
	 * Obtains the name for the {@link GovernanceEscalationType}.
	 * 
	 * @return Name for the {@link GovernanceEscalationType}.
	 */
	String getEscalationName();

	/**
	 * Obtains the type of {@link EscalationFlow} by the
	 * {@link GovernanceActivity}.
	 * 
	 * @param <E>
	 *            {@link Escalation} type.
	 * @return Type of {@link EscalationFlow} by the {@link GovernanceActivity}.
	 */
	<E extends Throwable> Class<E> getEscalationType();

}