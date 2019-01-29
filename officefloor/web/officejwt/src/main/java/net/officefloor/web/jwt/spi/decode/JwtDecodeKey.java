package net.officefloor.web.jwt.spi.decode;

import java.io.Serializable;
import java.security.Key;
import java.util.concurrent.TimeUnit;

import net.officefloor.frame.api.clock.Clock;

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
	 * @param startTime  Seconds since Epoch for when this {@link JwtDecodeKey}
	 *                   becomes active.
	 * @param expireTime Seconds since Epoch for expiry of this
	 *                   {@link JwtDecodeKey}.
	 * @param key        {@link Key} to validate the JWT.
	 * @throws IllegalArgumentException If invalid arguments.
	 */
	public JwtDecodeKey(long startTime, long expireTime, Key key) throws IllegalArgumentException {
		this.startTime = startTime;
		this.expireTime = expireTime;
		this.key = key;
		this.validate();
	}

	/**
	 * Instantiate to become active immediately and expire within the specified
	 * time.
	 * 
	 * @param timeInSeconds  {@link Clock} to obtain the seconds since Epoch.
	 * @param periodToExpire Period to expire the {@link Key}.
	 * @param unit           {@link TimeUnit} for period.
	 * @param key            {@link Key} to validate the JWT.
	 * @throws IllegalArgumentException If invalid arguments.
	 */
	public JwtDecodeKey(Clock<Long> timeInSeconds, long periodToExpire, TimeUnit unit, Key key)
			throws IllegalArgumentException {
		if (timeInSeconds == null) {
			throw new IllegalArgumentException("Must provide " + Clock.class.getSimpleName());
		}
		this.startTime = timeInSeconds.getTime();
		this.expireTime = this.startTime + (unit != null ? unit : TimeUnit.SECONDS).toSeconds(periodToExpire);
		this.key = key;
		this.validate();
	}

	/**
	 * <p>
	 * Instantiates to (effectively) never expire.
	 * <p>
	 * <strong>This should only be used for testing.</strong> Within production
	 * environments, {@link Key} instances should be rotated at a semi-regular basis
	 * to reduce impact of compromised keys.
	 * 
	 * @param key {@link Key}.
	 * @throws IllegalArgumentException If missing {@link Key}.
	 */
	public JwtDecodeKey(Key key) throws IllegalArgumentException {
		this.startTime = 0;
		this.expireTime = Long.MAX_VALUE;
		this.key = key;
		this.validate();
	}

	/**
	 * Validates this {@link JwtDecodeKey}.
	 * 
	 * @throws IllegalArgumentException If invalid {@link JwtDecodeKey}.
	 */
	private void validate() throws IllegalArgumentException {
		if (this.key == null) {
			throw new IllegalArgumentException("Must provide " + Key.class.getSimpleName());
		}
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