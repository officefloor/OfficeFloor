/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.web.http.security.store;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.scheme.HttpSecurityImpl;

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
	 * @throws IOException
	 *             If fails to obtain {@link MessageDigest} for the algorithm.
	 */
	public static MessageDigest createDigest(String algorithm)
			throws IOException {

		// Determine if have algorithm
		if (algorithm != null) {
			String trimmedAlgorithm = algorithm.trim();
			if ((trimmedAlgorithm.length() > 0)
					&& (!CredentialStore.NO_ALGORITHM.equals(trimmedAlgorithm))) {
				// Have algorithm, so return appropriate digest
				try {
					return MessageDigest.getInstance(trimmedAlgorithm);
				} catch (NoSuchAlgorithmException ex) {
					throw new IOException(ex);
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
	 * @return {@link HttpSecurity} or <code>null</code> if not authorised.
	 * @throws IOException
	 *             If fails to communicate with {@link CredentialStore}.
	 */
	public static HttpSecurity authenticate(String userId, String realm,
			byte[] credentials, String scheme, CredentialStore store)
			throws IOException {

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
		HttpSecurity security = new HttpSecurityImpl(scheme, userId, roles);

		// Return the HTTP Security
		return security;
	}

	/**
	 * All access via static methods.
	 */
	private CredentialStoreUtil() {
	}

}