/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.security.store;

import java.nio.charset.Charset;
import java.util.Set;

import net.officefloor.plugin.socket.server.http.security.scheme.AuthenticationException;
import net.officefloor.plugin.socket.server.http.security.scheme.HttpSecuritySource;

/**
 * <p>
 * Store containing the credentials.
 * <p>
 * This is a standard interface to allow various credential stores to be
 * utilised as a dependency for {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CredentialStore {

	/**
	 * Default algorithm for Digest security.
	 */
	public static final String DEFAULT_ALGORITHM = "MD5";

	/**
	 * Obtains the algorithm used to encrypt Digest credentials.
	 * 
	 * @return Algorithm.
	 */
	String getAlgorithm();

	/**
	 * <p>
	 * Retrieves the credentials from the store.
	 * <p>
	 * The actual credentials are particular to the authentication scheme:
	 * <ol>
	 * <li>Basic: clear text password ({@link Charset} being US_ASCII)</li>
	 * <li>Digest: encrypted &quot;username:realm:password&quot; as per the
	 * algorithm. For example, if the algorithm were MD5 then the following
	 * command would produce the appropriate value ({@link Charset} being
	 * US_ASCII):
	 * <code>echo -n &quot;username:realm:password&quot; | md5sum</code></li>
	 * <li>Negotiate: as necessary for authentication</li>
	 * </ol>
	 * 
	 * @param userId
	 *            User identifier.
	 * @param realm
	 *            Realm. May be <code>null</code> (especially in the case for
	 *            Basic authentication).
	 * @return Value as per above description.
	 * @throws AuthenticationException
	 *             If fails to retrieve the credentials.
	 */
	byte[] retrieveCredentials(String userId, String realm)
			throws AuthenticationException;

	/**
	 * Retrieves the roles for the user from the store.
	 * 
	 * @param userId
	 *            User identifier.
	 * @param realm
	 *            Realm. May be <code>null</code> (especially in the case for
	 *            Basic authentication).
	 * @return {@link Set} of roles for the user.
	 * @throws AuthenticationException
	 *             If fails to retrieve the roles.
	 */
	Set<String> retrieveRoles(String userId, String realm)
			throws AuthenticationException;

}