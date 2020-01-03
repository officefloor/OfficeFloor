package net.officefloor.web.spi.security;

import java.io.Serializable;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Listens for change in access control (or {@link Escalation} in failing to
 * authenticate).
 * 
 * @author Daniel Sagenschneider
 */
public interface AccessControlListener<AC extends Serializable> {

	/**
	 * Notified of a change to access control.
	 * 
	 * @param accessControl
	 *            Access control. May be <code>null</code> if
	 *            <ul>
	 *            <li>logging out</li>
	 *            <li>failure in authenticating</li>
	 *            </ul>
	 * @param escalation
	 *            Possible {@link Escalation}. Will be <code>null</code> if
	 *            successfully obtain access control or logout.
	 */
	void accessControlChange(AC accessControl, Throwable escalation);

}