package net.officefloor.web.jwt.jwks.parser;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.jwt.jwks.JwksKeyParser;
import net.officefloor.web.jwt.jwks.JwksKeyParserContext;
import net.officefloor.web.jwt.jwks.JwksKeyParserServiceFactory;

/**
 * {@link RSAPublicKey} {@link JwksKeyParserServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class RsaPublicJwksKeyParserServiceFactory implements JwksKeyParserServiceFactory, JwksKeyParser {

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
		if (privateExponent != null) {
			return null; // private key
		}

		// Obtain the public modulus and exponent
		BigInteger modulus = context.getBase64BigInteger("n");
		BigInteger exponent = context.getBase64BigInteger("e");

		// Create new RSA public key
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
		Key key = KeyFactory.getInstance(keyType).generatePublic(keySpec);

		// Return the key
		return key;
	}

}
