package net.officefloor.web.session.spi;

import net.officefloor.web.session.HttpSession;

/**
 * Operation to obtain details of invalidating a {@link HttpSession} within the
 * {@link HttpSessionStore}.
 *
 * @author Daniel Sagenschneider
 */
public interface InvalidateHttpSessionOperation {

	/**
	 * Obtains the session Id of the {@link HttpSession} to invalidate.
	 *
	 * @return Session Id of the {@link HttpSession} to invalidate.
	 */
	String getSessionId();

	/**
	 * Flags the {@link HttpSession} was invalidated successfully within the
	 * {@link HttpSessionStore}.
	 */
	void sessionInvalidated();

	/**
	 * Flags failed to invalidate the {@link HttpSession} within the
	 * {@link HttpSessionStore}.
	 *
	 * @param cause
	 *            Cause of the failure.
	 */
	void failedToInvalidateSession(Throwable cause);

}