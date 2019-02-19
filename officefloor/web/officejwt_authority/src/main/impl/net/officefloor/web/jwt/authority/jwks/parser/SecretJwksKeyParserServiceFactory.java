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
