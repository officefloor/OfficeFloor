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
package net.officefloor.web.security.scheme;

import net.officefloor.web.spi.security.HttpCredentials;
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