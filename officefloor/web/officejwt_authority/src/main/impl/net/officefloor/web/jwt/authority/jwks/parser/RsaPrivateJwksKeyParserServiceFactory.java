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

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.jwt.jwks.JwksKeyParser;
import net.officefloor.web.jwt.jwks.JwksKeyParserContext;
import net.officefloor.web.jwt.jwks.JwksKeyParserServiceFactory;

/**
 * {@link RSAPrivateKey} {@link JwksKeyParserServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class RsaPrivateJwksKeyParserServiceFactory implements JwksKeyParserServiceFactory, JwksKeyParser {

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
		if (!"RSA".equalsIgnoreCase(keyType)) {
			return null;
		}

		// Determine if private key
		BigInteger privateExponent = context.getBase64BigInteger("d");
		if (privateExponent == null) {
			return null; // public key
		}

		// Obtain the modulus and exponent
		BigInteger modulus = context.getBase64BigInteger("n");
		BigInteger publicExponent = context.getBase64BigInteger("e");
		BigInteger primeP = context.getBase64BigInteger("p");
		BigInteger primeQ = context.getBase64BigInteger("q");
		BigInteger primeExponentP = context.getBase64BigInteger("dp");
		BigInteger primeExponentQ = context.getBase64BigInteger("dq");
		BigInteger crtCoefficient = context.getBase64BigInteger("qi");

		// Create new RSA private key
		RSAPrivateKeySpec keySpec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ,
				primeExponentP, primeExponentQ, crtCoefficient);
		Key key = KeyFactory.getInstance(keyType).generatePrivate(keySpec);

		// Return the key
		return key;
	}

}
