package net.officefloor.web.session.spi;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;

/**
 * Newly created {@link HttpSession} requiring a session Id.
 *
 * @author Daniel Sagenschneider
 */
public interface FreshHttpSession {

	/**
	 * <p>
	 * Obtains the {@link ServerHttpConnection} requiring a new
	 * {@link HttpSession}.
	 * <p>
	 * Typically this should not be required to generate session Ids.
	 *
	 * @return {@link ServerHttpConnection} requiring a new {@link HttpSession}.
	 */
	ServerHttpConnection getConnection();

	/**
	 * Specifies the Id generated for the {@link HttpSession}.
	 *
	 * @param sessionId
	 *            Id generated for the {@link HttpSession}.
	 */
	void setSessionId(String sessionId);

	/**
	 * Flags failure in generating the {@link HttpSession} Id.
	 *
	 * @param failure
	 *            Failure in generating the {@link HttpSession} Id.
	 */
	void failedToGenerateSessionId(Throwable failure);

}