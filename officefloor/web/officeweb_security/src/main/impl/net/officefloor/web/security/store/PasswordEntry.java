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

import java.util.Set;

/**
 * {@link PasswordFile} {@link CredentialEntry}.
 * 
 * @author Daniel Sagenschneider
 */
public class PasswordEntry implements CredentialEntry {

	/**
	 * User Id.
	 */
	private final String userId;

	/**
	 * Credentials.
	 */
	private final byte[] credentials;

	/**
	 * Roles.
	 */
	private final Set<String> roles;

	/**
	 * Initiate.
	 * 
	 * @param userId
	 *            User Id.
	 * @param credentials
	 *            Credentials.
	 * @param roles
	 *            Roles.
	 */
	public PasswordEntry(String userId, byte[] credentials, Set<String> roles) {
		this.userId = userId;
		this.credentials = credentials;
		this.roles = roles;
	}

	/**
	 * Obtains the User Id.
	 * 
	 * @return User Id.
	 */
	public String getUserId() {
		return this.userId;
	}

	/*
	 * ======================= CredentialEntry ======================
	 */

	@Override
	public byte[] retrieveCredentials() {
		return this.credentials;
	}

	@Override
	public Set<String> retrieveRoles() {
		return this.roles;
	}

}
