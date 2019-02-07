package net.officefloor.app.subscription.rule;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.json.webtoken.JsonWebSignature.Header;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.util.Clock;

import net.officefloor.app.subscription.authenticate.GoogleIdTokenVerifierManagedObjectSource;

/**
 * {@link Rule} for mocking the {@link GoogleIdTokenVerifier}.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleIdTokenRule implements TestRule {

	/**
	 * Mock {@link KeyPair}.
	 */
	private static KeyPair pair = null;

	/**
	 * Obtains the mock {@link KeyPair}.
	 * 
	 * @return Mock {@link KeyPair}.
	 * @throws Exception If fails to create the mock {@link KeyPair}.
	 */
	private static KeyPair getMockKeyPair() throws Exception {
		if (pair == null) {
			// Avoid heavy computation by reusing key pair
			pair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		}
		return pair;
	}

	/**
	 * {@link MockVerifier}.
	 */
	private volatile GoogleIdTokenVerifier mockVerifier = null;

	/**
	 * Generates a mock {@link GoogleIdToken} string.
	 * 
	 * @param googleId       Google identifier.
	 * @param email          Email address.
	 * @param nameValuePairs Name/value pairs.
	 * @return {@link GoogleIdToken} string.
	 * @throws Exception If fails to generate the mock {@link GoogleIdToken} string.
	 */
	public String getMockIdToken(String googleId, String email, String... nameValuePairs) throws Exception {

		// Ensure mock verifier available
		if (this.mockVerifier == null) {
			throw new IllegalStateException("Can only obtain mock id token with context of rule");
		}

		// Obtain the private key
		PrivateKey privateKey = getMockKeyPair().getPrivate();

		// Generate the id token
		Header header = new JsonWebSignature.Header().setAlgorithm("RS256");
		Payload payload = new GoogleIdToken.Payload().setSubject(googleId).setEmail(email)
				.setIssuedAtTimeSeconds(mockVerifier.getClock().currentTimeMillis()).setExpirationTimeSeconds(10L)
				.setIssuer(mockVerifier.getIssuer());
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String name = nameValuePairs[i];
			String value = nameValuePairs[i + 1];
			payload = payload.set(name, value);
		}
		String token = GoogleIdToken.signUsingRsaSha256(privateKey, JacksonFactory.getDefaultInstance(), header,
				payload);

		// Return the id token
		return token;
	}

	/**
	 * Obtains the {@link GoogleIdTokenVerifier}.
	 * 
	 * @return {@link GoogleIdTokenVerifier}.
	 */
	public GoogleIdTokenVerifier getGoogleIdTokenVerifier() {

		// Ensure mock verifier available
		if (this.mockVerifier == null) {
			throw new IllegalStateException(
					"Can only obtain " + GoogleIdTokenVerifier.class.getSimpleName() + " with context of rule");
		}

		// Return the verifier
		return this.mockVerifier;
	}

	/*
	 * =================== TestRule =========================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {

				// Create the public key to verify token
				KeyPair pair = getMockKeyPair();
				PublicKey publicKey = pair.getPublic();

				// Key manager
				GooglePublicKeysManager manager = new GooglePublicKeysManager(new MockHttpTransport(),
						JacksonFactory.getDefaultInstance()) {
					@Override
					public GooglePublicKeysManager refresh() throws GeneralSecurityException, IOException {
						try {
							Field publicKeys = GooglePublicKeysManager.class.getDeclaredField("publicKeys");
							publicKeys.setAccessible(true);
							List<PublicKey> keys = new ArrayList<>();
							keys.add(publicKey);
							publicKeys.set(this, keys);
						} catch (Exception ex) {
							throw new GeneralSecurityException(ex);
						}
						return this;
					}
				};

				// Build the mock verifier
				Clock clock = () -> 300;
				try {
					// Hook in mock verifier
					GoogleIdTokenRule.this.mockVerifier = new GoogleIdTokenVerifier.Builder(manager).setClock(clock)
							.build();
					GoogleIdTokenVerifierManagedObjectSource.setGoogleIdTokenVerifierFactory(
							(audienceId) -> GoogleIdTokenRule.this.getGoogleIdTokenVerifier());

					// Undertake the test
					base.evaluate();

				} finally {
					// Ensure clear mock verifier
					GoogleIdTokenVerifierManagedObjectSource.setGoogleIdTokenVerifierFactory(null);
					GoogleIdTokenRule.this.mockVerifier = null;
				}
			}
		};
	}

}