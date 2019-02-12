package net.officefloor.web.jwt.jwks;

import java.security.Key;

import com.fasterxml.jackson.databind.JsonNode;

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
	public Key parseKey(JsonNode keyNode) throws Exception {

		// Ensure correct key type
		String kty = JwksKeyParser.getString(keyNode, KTY, null);
		if (!"FAIL".equalsIgnoreCase(kty)) {
			return null;
		}

		// Failed to parse
		throw new IOException("FAIL");
	}

}