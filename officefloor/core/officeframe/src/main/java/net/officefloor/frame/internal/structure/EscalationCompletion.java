package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Handler for the completion of {@link Escalation} handling.
 * 
 * @author Daniel Sagenschneider
 */
public interface EscalationCompletion {

	/**
	 * Notifies the completion of the {@link Escalation} handling.
	 * 
	 * @return {@link FunctionState} for notifying the {@link Escalation} handling
	 *         is complete.
	 */
	FunctionState escalationComplete();

}