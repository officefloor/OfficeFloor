package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.EscalationFlow;

/**
 * Configuration for the {@link EscalationFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface EscalationConfiguration {

	/**
	 * Obtains the type of cause handled by this {@link EscalationFlow}.
	 * 
	 * @return Type of cause handled by this {@link EscalationFlow}.
	 */
	Class<? extends Throwable> getTypeOfCause();

	/**
	 * Obtains the {@link ManagedFunctionReference} for the
	 * {@link ManagedFunction} handling the {@link Escalation}.
	 * 
	 * @return {@link ManagedFunctionReference} for the {@link ManagedFunction}
	 *         handling the {@link Escalation}.
	 */
	ManagedFunctionReference getManagedFunctionReference();

}
