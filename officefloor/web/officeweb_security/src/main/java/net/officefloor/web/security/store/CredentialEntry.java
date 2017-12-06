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

import java.nio.charset.Charset;
import java.util.Set;

import net.officefloor.server.http.HttpException;

/**
 * Entry within the {@link CredentialStore}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CredentialEntry {

	/**
	 * <p>
	 * Retrieves the credentials.
	 * <p>
	 * The actual credentials are particular to the authentication scheme:
	 * <ol>
	 * <li><code>Basic</code>: clear text password ({@link Charset} being
	 * US_ASCII)</li>
	 * <li><code>Digest</code>: encrypted &quot;username:realm:password&quot; as
	 * per the algorithm. For example, if the algorithm were MD5 then the
	 * following command would produce the appropriate value ({@link Charset}
	 * being US_ASCII):
	 * <code>echo -n &quot;username:realm:password&quot; | md5sum</code></li>
	 * <li><code>Negotiate</code>: as necessary for authentication</li>
	 * </ol>
	 * 
	 * @return Value as per above description.
	 * @throws HttpException
	 *             If fails to retrieve the credentials.
	 */
	byte[] retrieveCredentials() throws HttpException;

	/**
	 * Retrieves the roles.
	 * 
	 * @return {@link Set} of roles.
	 * @throws HttpException
	 *             If fails to retrieve the roles.
	 */
	Set<String> retrieveRoles() throws HttpException;

}