/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.jwt.authority;

/**
 * Refresh token.
 * 
 * @author Daniel Sagenschneider
 */
public class RefreshToken {

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
	public RefreshToken(String token, long expireTime) {
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