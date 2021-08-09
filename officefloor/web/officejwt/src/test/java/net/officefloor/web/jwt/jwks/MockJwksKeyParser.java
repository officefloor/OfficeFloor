/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt.jwks;

import java.security.Key;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link JwksKeyParser} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockJwksKeyParser implements JwksKeyParserServiceFactory, JwksKeyParser {

	/**
	 * Mock {@link Key}.
	 */
	public static Key mockKey = Keys.keyPairFor(SignatureAlgorithm.RS256).getPublic();

	/*
	 * ================= JwksKeyParserServiceFactory ==================
	 */

	@Override
	public JwksKeyParser createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== JwksKeyParser ===========================
	 */

	@Override
	public Key parseKey(JwksKeyParserContext context) throws Exception {

		// Ensure correct key type
		if (!"MOCK".equalsIgnoreCase(context.getKty())) {
			return null;
		}

		// Return the mock key
		return mockKey;
	}

}
