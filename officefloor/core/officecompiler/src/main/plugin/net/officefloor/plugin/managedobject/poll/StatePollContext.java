package net.officefloor.plugin.managedobject.poll;

import java.util.concurrent.TimeUnit;

/**
 * Context for a particular poll of state.
 * 
 * @author Daniel Sagenschneider
 */
public interface StatePollContext<S> {

	/**
	 * Obtains the current state.
	 * 
	 * @return Current state. May be <code>null</code> if initial poll.
	 */
	S getCurrentState();

	/**
	 * Sets the next state.
	 * 
	 * @param nextState        Next state.
	 * @param nextPollInterval Interval until next poll. Value of 0 or less results
	 *                         in the default poll interval.
	 * @param unit             {@link TimeUnit} for the poll interval.
	 */
	void setNextState(S nextState, long nextPollInterval, TimeUnit unit);

	/**
	 * Indicates there was a failure in polling.
	 * 
	 * @param cause            Cause of the failure.
	 * @param nextPollInterval Interval until next poll. Value of 0 or less results
	 *                         in the default poll interval.
	 * @param unit             {@link TimeUnit} for the poll interval.
	 */
	void setFailure(Throwable cause, long nextPollInterval, TimeUnit unit);

}