package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.manage.Office;

/**
 * Handles an {@link Escalation} from an {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface EscalationHandler {

	/**
	 * Handles an {@link Escalation} from an {@link Office}.
	 * 
	 * @param escalation
	 *            {@link Escalation}.
	 * @throws Throwable
	 *             Should failure in handling {@link Escalation}.
	 */
	void handleEscalation(Throwable escalation) throws Throwable;

}