/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
