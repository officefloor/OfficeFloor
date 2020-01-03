package net.officefloor.web.session.spi;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import net.officefloor.web.session.HttpSession;

/**
 * Operation to obtain details of creating a new {@link HttpSession} within the
 * {@link HttpSessionStore}.
 *
 * @author Daniel Sagenschneider
 */
public interface CreateHttpSessionOperation {

	/**
	 * Obtains the session Id of the new {@link HttpSession}.
	 *
	 * @return Session Id of the new {@link HttpSession}.
	 */
	String getSessionId();

	/**
	 * Flags that the {@link HttpSession} was successfully created within the
	 * {@link HttpSessionStore}.
	 *
	 * @param creationTime
	 *            Time the {@link HttpSession} was created within the
	 *            {@link HttpSessionStore}.
	 * @param expireTime
	 *            Time to expire the {@link HttpSession} should it be idle.
	 * @param attributes
	 *            {@link Map} to contain the {@link HttpSession} attributes.
	 */
	void sessionCreated(Instant creationTime, Instant expireTime, Map<String, Serializable> attributes);

	/**
	 * Flags that the session Id is the same as another {@link HttpSession}
	 * within the {@link HttpSessionStore}.
	 */
	void sessionIdCollision();

	/**
	 * Flags that failed to create the {@link HttpSession} within the
	 * {@link HttpSessionStore}.
	 *
	 * @param cause
	 *            Cause of the failure.
	 */
	void failedToCreateSession(Throwable cause);

}