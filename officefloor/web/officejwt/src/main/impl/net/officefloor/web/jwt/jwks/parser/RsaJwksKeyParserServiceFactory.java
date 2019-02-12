package net.officefloor.web.jwt.jwks.parser;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;

import com.fasterxml.jackson.databind.JsonNode;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.jwt.jwks.JwksKeyParser;
import net.officefloor.web.jwt.jwks.JwksKeyParserServiceFactory;

/**
 * RSA {@link JwksKeyParserServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class RsaJwksKeyParserServiceFactory implements JwksKeyParserServiceFactory, JwksKeyParser {

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
	public Key parseKey(JsonNode keyNode) throws Exception {

		// Ensure correct key type
		String kty = JwksKeyParser.getString(keyNode, KTY, null);
		if (!"RSA".equalsIgnoreCase(kty)) {
			return null;
		}

		// Obtain the modulus and exponent
		BigInteger modulus = JwksKeyParser.getBase64BigInteger(keyNode, "n", null);
		BigInteger exponent = JwksKeyParser.getBase64BigInteger(keyNode, "e", null);

		// Create new RSA public key
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
		Key key = KeyFactory.getInstance(kty).generatePublic(keySpec);

		// Return the key
		return key;
	}

}
