/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
	 * Sets the state and stops polling.
	 * 
	 * @param finalState Final state before stop polling.
	 */
	void setFinalState(S finalState);

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
