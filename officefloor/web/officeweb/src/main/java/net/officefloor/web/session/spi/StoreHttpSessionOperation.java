package net.officefloor.web.session.spi;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import net.officefloor.web.session.HttpSession;

/**
 * Operation to obtain details of storing the {@link HttpSession} within the
 * {@link HttpSessionStore}.
 *
 * @author Daniel Sagenschneider
 */
public interface StoreHttpSessionOperation {

	/**
	 * Obtains the session Id of the {@link HttpSession} to store.
	 *
	 * @return Session Id of the {@link HttpSession} to store.
	 */
	String getSessionId();

	/**
	 * Obtains the creation time for the {@link HttpSession}.
	 *
	 * @return Creation time for the {@link HttpSession}.
	 */
	Instant getCreationTime();

	/**
	 * Obtains the time to expire the {@link HttpSession} should it be idle.
	 *
	 * @return Time to expire the {@link HttpSession} should it be idle.
	 */
	Instant getExpireTime();

	/**
	 * Obtains the attributes of the {@link HttpSession}.
	 *
	 * @return Attributes of the {@link HttpSession}.
	 */
	Map<String, Serializable> getAttributes();

	/**
	 * Flags the {@link HttpSession} was stored successfully within the
	 * {@link HttpSessionStore}.
	 */
	void sessionStored();

	/**
	 * Flags failed to store the {@link HttpSession} within the
	 * {@link HttpSessionStore}.
	 *
	 * @param cause
	 *            Cause of the failure.
	 */
	void failedToStoreSession(Throwable cause);

}