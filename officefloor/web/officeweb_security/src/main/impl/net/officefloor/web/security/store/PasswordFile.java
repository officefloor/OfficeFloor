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
package net.officefloor.web.security.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Contents of the password file.
 * 
 * @author Daniel Sagenschneider
 */
public class PasswordFile {

	/**
	 * Algorithm encrypting the password entry credentials.
	 */
	private final String algorithm;

	/**
	 * {@link PasswordEntry} instances by user Id (in lower case to provide case
	 * insensitive matching).
	 */
	private final Map<String, PasswordEntry> entries = new HashMap<String, PasswordEntry>();

	/**
	 * Initiate.
	 * 
	 * @param algorithm
	 *            Algorithm encrypting the password entry credentials.
	 */
	public PasswordFile(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * Obtains the algorithm encrypting the credentials.
	 * 
	 * @return Algorithm encrypting the credentials.
	 */
	public String getAlgorithm() {
		return this.algorithm;
	}

	/**
	 * Obtains the {@link PasswordEntry} for the user Id.
	 * 
	 * @param userId
	 *            User Id.
	 * @return {@link PasswordEntry} for the user Id or <code>null</code> if
	 *         user Id not exist within this {@link PasswordFile}.
	 */
	public PasswordEntry getEntry(String userId) {
		return this.entries.get(userId.toLowerCase());
	}

	/**
	 * Adds a {@link PasswordEntry} to this {@link PasswordFile}.
	 * 
	 * @param userId
	 *            User Id.
	 * @param credentials
	 *            Credentials of user.
	 * @param roles
	 *            Roles.
	 */
	public void addEntry(String userId, byte[] credentials, Set<String> roles) {
		this.entries.put(userId.toLowerCase(), new PasswordEntry(userId,
				credentials, roles));
	}

}