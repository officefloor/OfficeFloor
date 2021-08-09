/*-
 * #%L
 * JWT Authority
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

package net.officefloor.web.jwt.authority;

import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.jwt.authority.repository.JwtAccessKey;
import net.officefloor.web.jwt.authority.repository.JwtAuthorityRepository;
import net.officefloor.web.jwt.authority.repository.JwtRefreshKey;
import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * Authority for JWT.
 * 
 * @param <I> Identity type.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAuthority<I> {

	/**
	 * Creates the refresh token for the identity.
	 * 
	 * @param identity Identity.
	 * @return {@link RefreshToken}.
	 * @throws RefreshTokenException If fails to create the refresh token.
	 */
	RefreshToken createRefreshToken(I identity) throws RefreshTokenException;

	/**
	 * Decodes the refresh token for the identity.
	 * 
	 * @param refreshToken Refresh token.
	 * @return Identity within the refresh token.
	 * @throws RefreshTokenException If fails to decode refresh token.
	 */
	I decodeRefreshToken(String refreshToken) throws RefreshTokenException;

	/**
	 * <p>
	 * Allows manually triggering a reload of the {@link JwtRefreshKey} instances.
	 * <p>
	 * This is useful for manual intervention in the active {@link JwtRefreshKey}
	 * instances. For example, a compromised {@link JwtRefreshKey} can be removed
	 * from the {@link JwtAuthorityRepository} with this method invoked to reload
	 * the {@link JwtRefreshKey} instances (minus the deleted compromised
	 * {@link JwtRefreshKey} instance).
	 */
	void reloadRefreshKeys();

	/**
	 * Create the access token for the claims.
	 * 
	 * @param claims Claims.
	 * @return {@link AccessToken}.
	 * @throws AccessTokenException If fails to create the access token.
	 */
	AccessToken createAccessToken(Object claims) throws AccessTokenException;

	/**
	 * <p>
	 * Allows manually triggering a reload of the {@link JwtAccessKey} instances.
	 * <p>
	 * Similar to {@link #reloadRefreshKeys()}, except for {@link JwtAccessKey}
	 * instances.
	 */
	void reloadAccessKeys();

	/**
	 * <p>
	 * Obtains the current active {@link JwtValidateKey} instances.
	 * <p>
	 * This allows publishing the {@link JwtValidateKey} instances to
	 * {@link JwtHttpSecuritySource} implementations.
	 * 
	 * @return Current active {@link JwtValidateKey} instances.
	 * @throws ValidateKeysException If fails to retrieve the active
	 *                               {@link JwtValidateKey} instances.
	 */
	JwtValidateKey[] getActiveJwtValidateKeys() throws ValidateKeysException;

}
