/*-
 * #%L
 * JWT Authority
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

package net.officefloor.web.jwt.authority.repository;

import java.security.Key;

/**
 * JWT refresh key.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtRefreshKey extends JwtAuthorityKey {

	/**
	 * Obtains the init vector.
	 * 
	 * @return Init vector.
	 */
	String getInitVector();

	/**
	 * Obtains the start salt.
	 * 
	 * @return Start salt.
	 */
	String getStartSalt();

	/**
	 * Obtains the lace.
	 * 
	 * @return Lace.
	 */
	String getLace();

	/**
	 * Obtains the end salt.
	 * 
	 * @return End salt.
	 */
	String getEndSalt();

	/**
	 * Obtains the {@link Key}.
	 * 
	 * @return {@link Key} to encrypt/decrypt the refresh token.
	 */
	Key getKey();

}
