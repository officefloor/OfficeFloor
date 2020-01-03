package net.officefloor.compile.managedfunction;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * <code>Type definition</code> of a possible {@link EscalationFlow} by the
 * {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionEscalationType {

	/**
	 * Obtains the name for the {@link ManagedFunctionEscalationType}.
	 * 
	 * @return Name for the {@link ManagedFunctionEscalationType}.
	 */
	String getEscalationName();

	/**
	 * Obtains the type of {@link EscalationFlow} by the {@link ManagedFunction}.
	 * 
	 * @param <E>
	 *            {@link Escalation} type.
	 * @return Type of {@link EscalationFlow} by the {@link ManagedFunction}.
	 */
	<E extends Throwable> Class<E> getEscalationType();

}