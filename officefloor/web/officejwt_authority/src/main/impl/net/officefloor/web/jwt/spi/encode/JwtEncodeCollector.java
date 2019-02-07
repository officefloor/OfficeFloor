package net.officefloor.web.jwt.spi.encode;

import java.util.concurrent.TimeUnit;

import net.officefloor.web.jwt.spi.decode.JwtDecodeKey;

/**
 * <p>
 * Collects {@link JwtEncodeKey} instances for JWT validation.
 * <p>
 * It is expected that the {@link JwtEncodeKey} instances (and their
 * corresponding {@link JwtDecodeKey} instances) are rotated. This minimises the
 * impact of "leaked" keys (for whatever reason) from creating security
 * problems.
 * <p>
 * Furthermore, in a clustered environment, co-ordinating the creation of
 * {@link JwtEncodeKey} instances can become complicated. It is, therefore,
 * possible to have multiple {@link JwtEncodeKey} instances in play, with the
 * example following algorithm:
 * <ol>
 * <li>A collect of keys is triggered for a particular instance in the
 * cluster</li>
 * <li>The instance retrieves all {@link JwtEncodeKey} instances from a central
 * store, and identifies a new {@link JwtEncodeKey} is required.</li>
 * <li>The instance creates the {@link JwtEncodeKey} and stores it in the
 * central store.</li>
 * <ul>
 * <li>Note: the active window for the {@link JwtEncodeKey} should be in the
 * future. It should only be active after a time that all instances in the
 * cluster will have collected the new {@link JwtEncodeKey} (and corresponding
 * {@link JwtDecodeKey} instances).</li>
 * </ul>
 * </li>
 * <li>The instance then includes the {@link JwtEncodeKey} in its encoding</li>
 * <li>Other instances in the cluster trigger a collect, and pull in the created
 * {@link JwtEncodeKey} from the central store.</li>
 * <li>Should two instances in the cluster create a {@link JwtEncodeKey}
 * simultaneously, then both {@link JwtEncodeKey} instances can be arbitrarily
 * used. This is ok as all instances should load both corresponding
 * {@link JwtDecodeKey} instances.
 * <ul>
 * <li>Note: this does come with the cost of extra computation on the consumers
 * to validate the JWT instances. However, this algorithm also works if the
 * cluster is co-ordinated to only create the one {@link JwtEncodeKey} per time
 * period (reducing this computation).</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtEncodeCollector {

	/**
	 * Obtains the current {@link JwtEncodeKey} instances.
	 * 
	 * @return Current {@link JwtEncodeKey} instances.
	 */
	JwtEncodeKey[] getCurrentKeys();

	/**
	 * Specifies the {@link JwtEncodeKey} instances.
	 * 
	 * @param timeToNextCheck Time in milliseconds to collect {@link JwtEncodeKey}
	 *                        instances again.
	 * @param keys            {@link JwtEncodeKey} instances.
	 */
	void setEncoding(JwtEncodeKey[] keys);

	/**
	 * Indicates failure in retrieving the {@link JwtEncodeKey} instances.
	 * 
	 * @param cause           Cause of the failure.
	 * @param timeToNextCheck Allows overriding the default poll refresh interval.
	 *                        This typically allows retrying earlier than the
	 *                        default refresh period.
	 * @param unit            {@link TimeUnit} for the next time to check.
	 */
	void setFailure(Throwable cause, long timeToNextCheck, TimeUnit unit);

}