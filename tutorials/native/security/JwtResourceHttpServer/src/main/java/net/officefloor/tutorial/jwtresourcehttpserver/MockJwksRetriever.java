package net.officefloor.tutorial.jwtresourcehttpserver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import net.officefloor.web.jwt.jwks.JwksRetriever;

/**
 * <p>
 * Mock {@link JwksRetriever}.
 * <p>
 * Typically this would be HTTPS call to JWT authority server.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class MockJwksRetriever implements JwksRetriever {

	@Override
	public InputStream retrieveJwks() throws Exception {

		// For production make HTTPS call to JWT Authority server to obtain JWKS content

		// For tutorial, returning mocked JWKS response
		RSAPublicKey key = (RSAPublicKey) Keys.keyPairFor(SignatureAlgorithm.RS256).getPublic();
		JwksKeys keys = new JwksKeys(Arrays.asList(
				new JwksKey("RSA", base64(key.getModulus()), base64(key.getPublicExponent()), 0, Long.MAX_VALUE)));
		String content = mapper.writeValueAsString(keys);
		return new ByteArrayInputStream(content.getBytes());
	}

	private static ObjectMapper mapper = new ObjectMapper();

	private static String base64(BigInteger value) {
		return Base64.getUrlEncoder().encodeToString(value.toByteArray());
	}

	@Data
	public static class JwksKeys {
		private final List<JwksKey> keys;
	}

	@Data
	public static class JwksKey {
		private final String kty;
		private final String n;
		private final String e;
		private final long nbf;
		private final long exp;
	}

}
// END SNIPPET: tutorial