package net.officefloor.web.session.generator;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.generator.UuidHttpSessionIdGenerator;
import net.officefloor.web.session.spi.FreshHttpSession;

/**
 * Tests the {@link UuidHttpSessionIdGenerator}.
 *
 * @author Daniel Sagenschneider
 */
public class UuidHttpSessionIdGeneratorTest extends OfficeFrameTestCase {

	/**
	 * Ensures a series of generations are unique.
	 */
	public void testForUniqueness() {

		UuidHttpSessionIdGenerator generator = new UuidHttpSessionIdGenerator();
		final Set<String> uniqueSessionIds = new HashSet<String>();
		for (int i = 0; i < 10000; i++) {
			generator.generateSessionId(new FreshHttpSession() {

				@Override
				public ServerHttpConnection getConnection() {
					fail("Should not require connection");
					return null;
				}

				@Override
				public void setSessionId(String sessionId) {
					// Ensure Session Id is unique
					assertFalse("Should be unique: " + sessionId,
							uniqueSessionIds.contains(sessionId));
					uniqueSessionIds.contains(sessionId);
				}

				@Override
				public void failedToGenerateSessionId(Throwable failure) {
					fail("Should not fail generation");
				}
			});
		}
	}

}