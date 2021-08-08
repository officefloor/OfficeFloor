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

import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.binary.Base64;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * {@link CredentialStore} for {@link DirContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiLdapCredentialStore implements CredentialStore {

	/**
	 * Algorithm.
	 */
	private final String algorithm;

	/**
	 * Algorithm prefix of credentials to use.
	 */
	private final String credentialPrefix;

	/**
	 * {@link DirContext}.
	 */
	private final DirContext context;

	/**
	 * Base dn for searching for entries.
	 */
	private final String entrySearchBaseDn;

	/**
	 * Base dn for searching for roles.
	 */
	private final String rolesSearchBaseDn;

	/**
	 * Initiate.
	 * 
	 * @param algorithm
	 *            Algorithm.
	 * @param context
	 *            {@link DirContext}.
	 * @param entrySearchBaseDn
	 *            Base dn for searching for entries.
	 * @param rolesSearchBaseDn
	 *            Base dn for searching for roles.
	 */
	public JndiLdapCredentialStore(String algorithm, DirContext context, String entrySearchBaseDn,
			String rolesSearchBaseDn) {
		this.algorithm = (algorithm == null ? null : (algorithm.trim().length() == 0 ? null : algorithm.trim()));
		this.context = context;
		this.entrySearchBaseDn = entrySearchBaseDn;
		this.rolesSearchBaseDn = rolesSearchBaseDn;

		// Create the prefix
		if (this.algorithm == null) {
			this.credentialPrefix = "";
		} else {
			this.credentialPrefix = "{" + this.algorithm.toUpperCase() + "}";
		}
	}

	/*
	 * ================== CredentialStore ==========================
	 */

	@Override
	public String getAlgorithm() {
		return this.algorithm;
	}

	@Override
	public CredentialEntry retrieveCredentialEntry(String userId, String realm) throws HttpException {
		try {
			// Search for the credential entry
			NamingEnumeration<SearchResult> searchResults = this.context.search(this.entrySearchBaseDn,
					"(&(objectClass=inetOrgPerson)(uid=" + userId + "))", null);
			if (!searchResults.hasMore()) {
				return null; // entry not found
			}
			SearchResult result = searchResults.next();

			// Obtain the attributes
			String entryDn = result.getNameInNamespace();

			// Create and return the credential entry
			return new JndiLdapCredentialEntry(entryDn);

		} catch (NamingException ex) {
			throw new HttpException(ex);
		}
	}

	/**
	 * JNDI LDAP {@link CredentialEntry}.
	 */
	private class JndiLdapCredentialEntry implements CredentialEntry {

		/**
		 * Dn for this {@link CredentialEntry}.
		 */
		private final String entryDn;

		/**
		 * Initiate.
		 * 
		 * @param entryDn
		 *            Dn for this {@link CredentialEntry}.
		 */
		public JndiLdapCredentialEntry(String entryDn) {
			this.entryDn = entryDn;
		}

		/*
		 * =================== CredentialEntry ========================
		 */

		@Override
		public byte[] retrieveCredentials() throws HttpException {
			try {
				// Obtain the entry's userPassword attribute
				Attributes entry = JndiLdapCredentialStore.this.context.getAttributes(this.entryDn);
				Attribute userPasswordAttribute = entry.get("userPassword");

				// Iterate over 'userPassword' values to match algorithm
				NamingEnumeration<?> userPasswords = userPasswordAttribute.getAll();
				for (; userPasswords.hasMore();) {

					// Obtain the userPassword value
					byte[] userPasswordBytes = (byte[]) userPasswords.next();
					String userPasswordText = new String(userPasswordBytes, AbstractHttpSecuritySource.UTF_8);

					// Determine if credential for algorithm
					if (userPasswordText.toUpperCase().startsWith(JndiLdapCredentialStore.this.credentialPrefix)) {

						// Found credentials, so strip out prefix
						userPasswordText = userPasswordText
								.substring(JndiLdapCredentialStore.this.credentialPrefix.length());

						// Decode credentials
						byte[] credentials;
						if (JndiLdapCredentialStore.this.algorithm == null) {
							// Plain text password
							credentials = userPasswordText.getBytes(AbstractHttpSecuritySource.UTF_8);
						} else {
							// Decode credentials (assume always Base64)
							credentials = Base64.decodeBase64(userPasswordText);
						}

						// Return the credentials
						return credentials;
					}
				}

				// If here, no credentials
				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null,
						"No authentication credentials for " + this.entryDn);

			} catch (NamingException ex) {
				throw new HttpException(ex);
			}
		}

		@Override
		public Set<String> retrieveRoles() throws HttpException {
			try {

				// Search for the groups
				NamingEnumeration<SearchResult> groupResults = JndiLdapCredentialStore.this.context.search(
						JndiLdapCredentialStore.this.rolesSearchBaseDn,
						"(&(objectClass=groupOfNames)(member=" + this.entryDn + "))", null);

				// Obtain the set of roles
				Set<String> roles = new HashSet<String>();
				for (; groupResults.hasMore();) {
					SearchResult group = groupResults.next();

					// Obtain the role from the group
					String role = (String) group.getAttributes().get("ou").get();

					// Add role to listing
					roles.add(role);
				}

				// Return the roles
				return roles;

			} catch (NamingException ex) {
				throw new HttpException(ex);
			}
		}
	}

}
