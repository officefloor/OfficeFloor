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
