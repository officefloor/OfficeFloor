package net.officefloor.web.session.generator;

import java.util.UUID;

import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.spi.FreshHttpSession;
import net.officefloor.web.session.spi.HttpSessionIdGenerator;

/**
 * {@link HttpSessionIdGenerator} that uses {@link UUID#randomUUID()} to
 * generate a {@link HttpSession} Id.
 *
 * @author Daniel Sagenschneider
 */
public class UuidHttpSessionIdGenerator implements HttpSessionIdGenerator {

	/*
	 * ================== HttpSessionIdGenerator =======================
	 */

	@Override
	public void generateSessionId(FreshHttpSession session) {

		// Generate the random UUID to obtain the Session Id
		UUID uuid = UUID.randomUUID();
		String sessionId = uuid.toString();

		// Load the Session Id
		session.setSessionId(sessionId);
	}

}