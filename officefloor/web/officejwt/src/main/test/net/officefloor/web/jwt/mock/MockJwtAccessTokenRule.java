package net.officefloor.web.jwt.mock;

import java.security.KeyPair;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * <p>
 * {@link TestRule} to mock JWT access tokens for the
 * {@link JwtHttpSecuritySource}.
 * <p>
 * This allows generating access tokens for testing the application.
 * 
 * @author Daniel Sagenschneider
 */
public class MockJwtAccessTokenRule implements TestRule {

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@link KeyPair} for signing and validating the JWT.
	 */
	private final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);

	/**
	 * {@link JwtValidateKey} that does not expire (well within the context of
	 * testing).
	 */
	private final JwtValidateKey validateKey = new JwtValidateKey(this.keyPair.getPublic());

	/**
	 * Creates the access token.
	 * 
	 * @param claims Claims for the access token.
	 * @return Access token.
	 */
	public String createAccessToken(Object claims) {

		// Obtain the claims
		String payload;
		try {
			payload = mapper.writeValueAsString(claims);
		} catch (Exception ex) {
			throw new AssertionError("Failed to serialise claims to payload", ex);
		}

		// Create the access token
		String accessToken = Jwts.builder().signWith(this.keyPair.getPrivate()).setPayload(payload).compact();

		// Return the access token
		return accessToken;
	}

	/**
	 * Obtains the active {@link JwtValidateKey} instances.
	 * 
	 * @return Active {@link JwtValidateKey} instances.
	 */
	public JwtValidateKey[] getActiveJwtValidateKeys() {
		return new JwtValidateKey[] { this.validateKey };
	}

	/*
	 * =================== TestRule =====================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				JwtHttpSecuritySource.runWithKeys(
						() -> new JwtValidateKey[] { MockJwtAccessTokenRule.this.validateKey }, () -> base.evaluate());
			}
		};
	}

}