package net.officefloor.web.session.spi;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import net.officefloor.web.session.HttpSession;

/**
 * Operation to obtain details of retrieving a {@link HttpSession} from the
 * {@link HttpSessionStore}.
 *
 * @author Daniel Sagenschneider
 */
public interface RetrieveHttpSessionOperation {

	/**
	 * Obtains the session Id of the {@link HttpSession} to retrieve.
	 *
	 * @return Session Id of the {@link HttpSession} to retrieve.
	 */
	String getSessionId();

	/**
	 * Flags that the {@link HttpSession} was successfully retrieved from the
	 * {@link HttpSessionStore}.
	 *
	 * @param creationTime
	 *            Time the {@link HttpSession} was created in the
	 *            {@link HttpSessionStore}.
	 * @param expireTime
	 *            Time to expire the {@link HttpSession} should it be idle.
	 * @param attributes
	 *            Attributes for the retrieved {@link HttpSession}.
	 */
	void sessionRetrieved(Instant creationTime, Instant expireTime, Map<String, Serializable> attributes);

	/**
	 * <p>
	 * Flags that the {@link HttpSession} is not available in the
	 * {@link HttpSessionStore}.
	 * <p>
	 * Typically this is due to the {@link HttpSession} timing out and being
	 * invalidated.
	 */
	void sessionNotAvailable();

	/**
	 * Flags that failed to retrieve the {@link HttpSession} from the
	 * {@link HttpSessionStore}.
	 *
	 * @param cause
	 *            Cause of the failure.
	 */
	void failedToRetreiveSession(Throwable cause);

}