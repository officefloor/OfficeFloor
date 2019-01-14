package net.officefloor.web.jwt.spi.decode;

import java.io.Serializable;
import java.security.Key;

/**
 * JWT decode key.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtDecodeKey implements Serializable {

	/**
	 * Start time.
	 */
	private final long startTime;

	/**
	 * Expire time.
	 */
	private final long expireTime;

	/**
	 * {@link Key}.
	 */
	private final Key key;

	/**
	 * Instantiate.
	 * 
	 * @param startTime  Milliseconds since Epoch for when this {@link JwtDecodeKey}
	 *                   becomes active.
	 * @param expireTime Milliseconds since Epoch for expiry of this
	 *                   {@link JwtDecodeKey}.
	 * @param key        {@link Key} to validate the JWT.
	 */
	public JwtDecodeKey(long startTime, long expireTime, Key key) {
		this.startTime = startTime;
		this.expireTime = expireTime;
		this.key = key;
	}

	/**
	 * Instantiate to become active immediately and expire within the specified
	 * time.
	 * 
	 * @param millisecondsToExpire Milliseconds from now to expire.
	 * @param key                  {@link Key}.
	 */
	public JwtDecodeKey(long millisecondsToExpire, Key key) {
		this.startTime = System.currentTimeMillis();
		this.expireTime = this.startTime + millisecondsToExpire;
		this.key = key;
	}

	/**
	 * <p>
	 * Instantiates to (effectively) never expire.
	 * <p>
	 * <strong>This should only be used for testing.</strong> Within production
	 * environments, {@link Key} instances should be rotated a semi-regular basis to
	 * reduce impact of compromised keys.
	 * 
	 * @param key {@link Key}.
	 */
	public JwtDecodeKey(Key key) {
		this.startTime = 0;
		this.expireTime = Long.MAX_VALUE;
		this.key = key;
	}

	/**
	 * Obtains the milliseconds since Epoch for when this {@link JwtDecodeKey}
	 * becomes active.
	 * 
	 * @return Milliseconds since Epoch for when this {@link JwtDecodeKey} becomes
	 *         active.
	 */
	public long startTime() {
		return this.startTime;
	}

	/**
	 * Obtains the milliseconds since Epoch for expiry of this {@link JwtDecodeKey}.
	 * 
	 * @return Milliseconds since Epoch for expiry of this {@link JwtDecodeKey}.
	 */
	public long expireTime() {
		return this.expireTime;
	}

	/**
	 * Obtains the {@link Key}.
	 * 
	 * @return {@link Key} to validate the JWT.
	 */
	public Key getKey() {
		return this.key;
	}

}