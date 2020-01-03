package net.officefloor.identity.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

/**
 * Factory {@link FunctionalInterface} to create the
 * {@link GoogleIdTokenVerifier}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface GoogleIdTokenVerifierFactory {

	/**
	 * Creates the {@link GoogleIdTokenVerifier}.
	 *
	 * @return {@link GoogleIdTokenVerifier}.
	 * @throws Exception If fails to create the {@link GoogleIdTokenVerifier}.
	 */
	GoogleIdTokenVerifier create() throws Exception;

}