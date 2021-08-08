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
