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

/**
 * Access token.
 * 
 * @author Daniel Sagenschneider
 */
public class AccessToken {

	/**
	 * Token.
	 */
	private final String token;

	/**
	 * Expire time in seconds.
	 */
	private final long expireTime;

	/**
	 * Instantiate.
	 * 
	 * @param token      Token.
	 * @param expireTime Expire time in seconds.
	 */
	public AccessToken(String token, long expireTime) {
		this.token = token;
		this.expireTime = expireTime;
	}

	/**
	 * Obtains the token.
	 * 
	 * @return Token.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Obtains the expire time in seconds.
	 * 
	 * @return Expire time in seconds.
	 */
	public long getExpireTime() {
		return expireTime;
	}

}
