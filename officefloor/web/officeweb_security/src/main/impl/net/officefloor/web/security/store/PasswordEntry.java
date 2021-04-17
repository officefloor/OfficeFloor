/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
