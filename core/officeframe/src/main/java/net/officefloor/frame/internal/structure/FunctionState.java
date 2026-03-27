/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;

/**
 * Node within the graph of {@link FunctionState} instances to execute.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionState extends LinkedListSetEntry<FunctionState, Flow> {

	@Override
	default Flow getLinkedListSetOwner() {
		throw new IllegalStateException(this.getClass().getName()
				+ " must override getLinkedListSetOwner to be added to a " + LinkedListSet.class.getName());
	}

	/**
	 * <p>
	 * Obtains the {@link TeamManagement} responsible for this
	 * {@link FunctionState}.
	 * <p>
	 * By default, {@link FunctionState} may be executed by any
	 * {@link TeamManagement}.
	 * 
	 * @return {@link TeamManagement} responsible for this {@link FunctionState}.
	 *         May be <code>null</code> to indicate any {@link Team} may execute the
	 *         {@link FunctionState}.
	 */
	default TeamManagement getResponsibleTeam() {
		return null; // any team by default
	}

	/**
	 * <p>
	 * Obtains the {@link ThreadState} for this {@link FunctionState}.
	 * <p>
	 * This provides access to the {@link ThreadState} that this
	 * {@link FunctionState} resides within.
	 * 
	 * @return {@link ThreadState} for this {@link FunctionState}.
	 */
	ThreadState getThreadState();

	/**
	 * Indicates if the {@link FunctionState} requires {@link ThreadState} safety.
	 * 
	 * @return <code>true</code> should {@link FunctionState} require
	 *         {@link ThreadState} safety.
	 */
	default boolean isRequireThreadStateSafety() {
		return false; // no thread safety required by default
	}

	/**
	 * Executes the {@link FunctionState}.
	 * 
	 * @param context {@link FunctionStateContext} for executing the
	 *                {@link FunctionState}.
	 * @return Next {@link FunctionState} to be executed. May be <code>null</code>
	 *         to indicate no further {@link FunctionState} instances to execute.
	 * @throws Throwable Possible failure of {@link FunctionState} logic.
	 */
	FunctionState execute(FunctionStateContext context) throws Throwable;

	/**
	 * Cancels this {@link FunctionState} returning an optional
	 * {@link FunctionState} to clean up this {@link FunctionState}.
	 * 
	 * @return Optional clean up {@link FunctionState}. May be <code>null</code>.
	 */
	default FunctionState cancel() {
		return null; // no clean up by default
	}

	/**
	 * Handles {@link Escalation} from the {@link ManagedFunction}.
	 * 
	 * @param escalation {@link Escalation}.
	 * @param completion Optional {@link EscalationCompletion} to be notified once
	 *                   {@link Escalation} has been handled.
	 * @return Optional {@link FunctionState} to handle the {@link Escalation}.
	 */
	default FunctionState handleEscalation(Throwable escalation, EscalationCompletion completion) {
		return this.getThreadState().handleEscalation(escalation, completion);
	}

}
