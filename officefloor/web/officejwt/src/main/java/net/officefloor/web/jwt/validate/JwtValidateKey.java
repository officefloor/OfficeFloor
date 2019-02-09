package net.officefloor.web.jwt.validate;

import java.io.Serializable;
import java.security.Key;
import java.util.concurrent.TimeUnit;

import net.officefloor.frame.api.clock.Clock;

/**
 * JWT validate key.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtValidateKey implements Serializable {

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
	 * @param startTime  Seconds since Epoch for when this {@link JwtValidateKey}
	 *                   becomes active.
	 * @param expireTime Seconds since Epoch for expiry of this
	 *                   {@link JwtValidateKey}.
	 * @param key        {@link Key} to validate the JWT.
	 * @throws IllegalArgumentException If invalid arguments.
	 */
	public JwtValidateKey(long startTime, long expireTime, Key key) throws IllegalArgumentException {
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
	public JwtValidateKey(Clock<Long> timeInSeconds, long periodToExpire, TimeUnit unit, Key key)
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
	public JwtValidateKey(Key key) throws IllegalArgumentException {
		this.startTime = 0;
		this.expireTime = Long.MAX_VALUE;
		this.key = key;
		this.validate();
	}

	/**
	 * Validates this {@link JwtValidateKey}.
	 * 
	 * @throws IllegalArgumentException If invalid {@link JwtValidateKey}.
	 */
	private void validate() throws IllegalArgumentException {
		if (this.key == null) {
			throw new IllegalArgumentException("Must provide " + Key.class.getSimpleName());
		}
	}

	/**
	 * Obtains the milliseconds since Epoch for when this {@link JwtValidateKey}
	 * becomes active.
	 * 
	 * @return Milliseconds since Epoch for when this {@link JwtValidateKey} becomes
	 *         active.
	 */
	public long getStartTime() {
		return this.startTime;
	}

	/**
	 * Obtains the milliseconds since Epoch for expiry of this
	 * {@link JwtValidateKey}.
	 * 
	 * @return Milliseconds since Epoch for expiry of this {@link JwtValidateKey}.
	 */
	public long getExpireTime() {
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