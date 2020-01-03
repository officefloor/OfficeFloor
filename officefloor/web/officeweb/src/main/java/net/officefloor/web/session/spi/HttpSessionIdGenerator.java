package net.officefloor.web.session.spi;

import net.officefloor.web.session.HttpSession;

/**
 * <p>
 * Generates the {@link HttpSession} Id.
 * <p>
 * Typically a default {@link HttpSessionIdGenerator} is provided by the
 * {@link HttpSession} and this need not be provided. This interface however
 * enables customising the generation of the {@link HttpSession} Id.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpSessionIdGenerator {

	/**
	 * <p>
	 * Generates the session Id.
	 * <p>
	 * This method may return without the session Id being specified on the
	 * {@link FreshHttpSession}. In this case it is expected that the session Id
	 * will be populated some time in the near future.
	 *
	 * @param session
	 *            {@link FreshHttpSession} to be populated with a new session
	 *            Id.
	 */
	void generateSessionId(FreshHttpSession session);

}