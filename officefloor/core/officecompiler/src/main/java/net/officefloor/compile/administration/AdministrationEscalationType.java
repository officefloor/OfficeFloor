package net.officefloor.compile.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * <code>Type definition</code> of a possible {@link EscalationFlow} by the
 * {@link Administration}.
 *
 * @author Daniel Sagenschneider
 */
public interface AdministrationEscalationType {

	/**
	 * Obtains the name for the {@link AdministrationEscalationType}.
	 * 
	 * @return Name for the {@link AdministrationEscalationType}.
	 */
	String getEscalationName();

	/**
	 * Obtains the type of {@link EscalationFlow} by the {@link Administration}.
	 * 
	 * @param <E>
	 *            {@link Escalation} type.
	 * @return Type of {@link EscalationFlow} by the {@link Administration}.
	 */
	<E extends Throwable> Class<E> getEscalationType();

}