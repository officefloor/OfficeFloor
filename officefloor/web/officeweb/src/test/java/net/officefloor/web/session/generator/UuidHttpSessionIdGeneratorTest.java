/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.session.generator;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
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
