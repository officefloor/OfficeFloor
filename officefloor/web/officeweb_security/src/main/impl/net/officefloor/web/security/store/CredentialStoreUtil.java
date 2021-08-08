/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.security.store;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.scheme.HttpAccessControlImpl;

/**
 * Utility functions for working with a {@link CredentialStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class CredentialStoreUtil {

	/**
	 * Creates the {@link MessageDigest} for the {@link CredentialStore}
	 * algorithm.
	 * 
	 * @param algorithm
	 *            {@link CredentialStore} algorithm.
	 * @return {@link MessageDigest} for the algorithm or <code>null</code>
	 *         indicate credentials are in plain text.
	 * @throws HttpException
	 *             If fails to obtain {@link MessageDigest} for the algorithm.
	 */
	public static MessageDigest createDigest(String algorithm) throws HttpException {

		// Determine if have algorithm
		if (algorithm != null) {
			String trimmedAlgorithm = algorithm.trim();
			if ((trimmedAlgorithm.length() > 0) && (!CredentialStore.NO_ALGORITHM.equals(trimmedAlgorithm))) {
				// Have algorithm, so return appropriate digest
				try {
					return MessageDigest.getInstance(trimmedAlgorithm);
				} catch (NoSuchAlgorithmException ex) {
					throw new HttpException(ex);
				}
			}
		}

		// No algorithm, so return no digest (null)
		return null;
	}

	/**
	 * Convenience method to authenticate the user from the
	 * {@link CredentialStore}.
	 * 
	 * @param userId
	 *            Identifier for the user.
	 * @param realm
	 *            Security realm.
	 * @param credentials
	 *            Credentials.
	 * @param scheme
	 *            Authentication scheme.
	 * @param store
	 *            {@link CredentialStore}.
	 * @return {@link HttpAccessControl} or <code>null</code> if not authorised.
	 * @throws HttpException
	 *             If fails to communicate with {@link CredentialStore}.
	 */
	public static HttpAccessControl authenticate(String userId, String realm, byte[] credentials, String scheme,
			CredentialStore store) throws HttpException {

		// Attempt to obtain entry from store
		CredentialEntry entry = store.retrieveCredentialEntry(userId, realm);
		if (entry == null) {
			return null; // unknown user
		}

		// Obtain the required credentials for the connection
		byte[] requiredCredentials = entry.retrieveCredentials();

		// Translate password as per algorithm
		String algorithm = store.getAlgorithm();
		MessageDigest digest = createDigest(algorithm);
		if (digest != null) {
			// Translate credentials as per algorithm
			digest.update(credentials);
			credentials = digest.digest();
		}

		// Ensure match for authentication
		if (requiredCredentials.length != credentials.length) {
			return null; // not authenticated
		} else {
			for (int i = 0; i < requiredCredentials.length; i++) {
				if (requiredCredentials[i] != credentials[i]) {
					return null; // not authenticated
				}
			}
		}

		// Authenticated, so obtain roles and create the HTTP Security
		Set<String> roles = entry.retrieveRoles();
		HttpAccessControl security = new HttpAccessControlImpl(scheme, userId, roles);

		// Return the HTTP Security
		return security;
	}

	/**
	 * All access via static methods.
	 */
	private CredentialStoreUtil() {
	}

}
