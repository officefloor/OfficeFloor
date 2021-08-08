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
