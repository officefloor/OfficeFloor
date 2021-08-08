/*-
 * #%L
 * OfficeCompiler
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
