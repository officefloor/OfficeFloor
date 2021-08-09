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

package net.officefloor.web.security.scheme;

import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * {@link HttpCredentials} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpCredentialsImpl implements HttpCredentials {

	/**
	 * Username.
	 */
	private final String username;

	/**
	 * Password.
	 */
	private final byte[] password;

	/**
	 * Initiate.
	 * 
	 * @param username
	 *            Username.
	 * @param password
	 *            Password.
	 */
	public HttpCredentialsImpl(String username, byte[] password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Convenience constructor for providing a {@link String} password.
	 * 
	 * @param username
	 *            Username.
	 * @param password
	 *            Password.
	 */
	public HttpCredentialsImpl(String username, String password) {
		this(username, (password == null ? null : password.getBytes(AbstractHttpSecuritySource.UTF_8)));
	}

	/*
	 * ===================== HttpCredentials =======================
	 */

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public byte[] getPassword() {
		return this.password;
	}

}
