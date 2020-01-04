/*-
 * #%L
 * JWT Authority
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

package net.officefloor.web.jwt.authority.jwks.parser;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.jwt.jwks.JwksKeyParser;
import net.officefloor.web.jwt.jwks.JwksKeyParserContext;
import net.officefloor.web.jwt.jwks.JwksKeyParserServiceFactory;

/**
 * {@link SecretKeySpec} {@link JwksKeyParserServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class SecretJwksKeyParserServiceFactory implements JwksKeyParserServiceFactory, JwksKeyParser {

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
		String keyType = context.getKty();
		if (!"oct".equalsIgnoreCase(keyType)) {
			return null;
		}

		// Obtain the key details
		String algorithm = context.getString("alg");
		byte[] encoding = context.getBase64Bytes("k");

		// Create secret key spec
		SecretKeySpec keySpec = new SecretKeySpec(encoding, algorithm);

		// Return the key
		return keySpec;
	}

}
