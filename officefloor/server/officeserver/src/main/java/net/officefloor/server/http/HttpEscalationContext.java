package net.officefloor.server.http;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Context for the {@link HttpEscalationHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpEscalationContext {

	/**
	 * Obtains the {@link Escalation}.
	 * 
	 * @return {@link Escalation}.
	 */
	Throwable getEscalation();

	/**
	 * Indicates whether the stack trace should be included.
	 * 
	 * @return <code>true</code> to include the stack trace.
	 */
	boolean isIncludeStacktrace();

	/**
	 * Obtains the {@link ServerHttpConnection} to write the {@link Escalation}.
	 * 
	 * @return {@link ServerHttpConnection}.
	 */
	ServerHttpConnection getServerHttpConnection();

}