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

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.web.http.security.scheme.AuthenticationException;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySource;

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
	 * Default algorithm.
	 */
	public static final String DEFAULT_ALGORITHM = "MD5";

	/**
	 * <p>
	 * Non-blank value indicating no algorithm.
	 * <p>
	 * This is useful for property configurations of {@link ManagedObjectSource}
	 * and {@link WorkSource} instances that need to indicate an algorithm (and
	 * can not provide blank values for required properties).
	 */
	public static final String NO_ALGORITHM = "-";

	/**
	 * <p>
	 * Obtains the algorithm used to encrypt credentials within this
	 * {@link CredentialStore}.
	 * <p>
	 * Should the return be <code>null</code>, blank or {@link #NO_ALGORITHM}
	 * then the password is considered to be stored in plain text. This is
	 * however only useful for the <code>BASIC</code> authentication scheme due
	 * to the nature of the other authentication schemes (such as
	 * <code>DIGEST</code>).
	 * <p>
	 * It is expected that the credentials for <code>DIGEST</code> will be
	 * stored as the algorithm applied to <code>userId:realm:password</code> (as
	 * per RFC 2617). This is necessary as the password is never supplied and
	 * therefore for <code>DIGEST</code> this MUST return an algorithm.
	 * 
	 * @return Algorithm.
	 */
	String getAlgorithm();

	/**
	 * Retrieves the {@link CredentialEntry}.
	 * 
	 * @param userId
	 *            User identifier.
	 * @param realm
	 *            Realm. May be <code>null</code> (especially in the case for
	 *            <code>Basic</code> authentication).
	 * @return {@link CredentialEntry} or <code>null</code> if no
	 *         {@link CredentialEntry} exists for parameters.
	 * @throws AuthenticationException
	 */
	CredentialEntry retrieveCredentialEntry(String userId, String realm)
			throws AuthenticationException;

}