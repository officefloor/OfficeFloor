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

import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * JWT key to encode access token.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAccessKey extends JwtAuthorityKey {

	/**
	 * Obtains the private {@link Key}.
	 * 
	 * @return Private {@link Key} to sign the JWT.
	 */
	Key getPrivateKey();

	/**
	 * <p>
	 * Obtains the public {@link Key}.
	 * <p>
	 * While not used for encoding, is kept together to enable
	 * {@link JwtAuthorityRepository} to associate public/private {@link Key}
	 * instances for {@link JwtAccessKey} to {@link JwtValidateKey} relationships.
	 * 
	 * @return Public {@link Key} to validate the JWT.
	 */
	Key getPublicKey();

}
