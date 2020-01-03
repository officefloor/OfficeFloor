package net.officefloor.frame.internal.structure;

/**
 * Context to check on the {@link Asset}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CheckAssetContext {

	/**
	 * <p>
	 * Obtains the time that check is being made.
	 * <p>
	 * As many {@link Asset} instances may be checked at the same time (or
	 * nanoseconds from each other) this provides optimisation to obtain the
	 * current time in milliseconds (equivalent to
	 * {@link System#currentTimeMillis()} for purpose of checking {@link Asset}
	 * timeouts).
	 * 
	 * @return {@link System#currentTimeMillis()} equivalent for checking
	 *         {@link Asset} timeouts.
	 */
	long getTime();

	/**
	 * Releases the {@link FunctionState} instances waiting on the {@link Asset}.
	 * 
	 * @param isPermanent
	 *            <code>true</code> indicates that all {@link FunctionState} instances
	 *            added to the {@link AssetLatch} from now on are activated
	 *            immediately. It is useful to flag an {@link AssetLatch} in
	 *            this state when the {@link Asset} is no longer being used to
	 *            stop a {@link FunctionState} from waiting forever.
	 */
	void releaseFunctions(boolean isPermanent);

	/**
	 * Fails the {@link FunctionState} instances waiting on this {@link Asset}.
	 * 
	 * @param failure
	 *            Failure to propagate to the {@link ThreadState} of the
	 *            {@link FunctionState} instances waiting on the {@link Asset}.
	 * @param isPermanent
	 *            <code>true</code> indicates that all {@link FunctionState} instances
	 *            added to the {@link AssetLatch} from now on are activated
	 *            immediately with the input failure. It is useful to flag an
	 *            {@link AssetLatch} in this state when the {@link Asset} is in
	 *            a failed state that can not be recovered from.
	 */
	void failFunctions(Throwable failure, boolean isPermanent);

}