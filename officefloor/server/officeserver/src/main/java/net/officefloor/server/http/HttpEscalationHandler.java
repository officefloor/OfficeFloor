package net.officefloor.server.http;

import java.io.IOException;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Enables sending an appropriate response for an {@link Escalation}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpEscalationHandler {

	/**
	 * Handles the {@link Escalation}.
	 * 
	 * @param context
	 *            {@link HttpEscalationContext}.
	 * @return <code>true</code> if handled {@link Escalation} into the
	 *         {@link HttpResponse}. <code>false</code> if not able to handle
	 *         the particular {@link Escalation}.
	 * @throws IOException
	 *             If fails to write the {@link Escalation}.
	 */
	boolean handle(HttpEscalationContext context) throws IOException;

}