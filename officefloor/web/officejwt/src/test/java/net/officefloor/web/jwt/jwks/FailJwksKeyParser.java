/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt.jwks;

import java.security.Key;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Keys;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link JwksKeyParser} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class FailJwksKeyParser implements JwksKeyParserServiceFactory, JwksKeyParser {

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
		if (!"FAIL".equalsIgnoreCase(context.getKty())) {
			return null;
		}

		// Failed to parse
		throw new IOException("FAIL");
	}

}
