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