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
