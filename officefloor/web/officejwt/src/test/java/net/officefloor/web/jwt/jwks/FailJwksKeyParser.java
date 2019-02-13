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