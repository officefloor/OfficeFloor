/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.api.team.Team;

/**
 * <p>
 * State of a thread within the {@link ProcessState}.
 * <p>
 * May be used as a {@link LinkedListSetEntry} in a list of {@link ThreadState}
 * instances for a {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadState extends LinkedListSetEntry<ThreadState, ProcessState> {

	/**
	 * Indicates if this {@link ThreadState} is attached to the current
	 * {@link Thread}.
	 * 
	 * @return <code>true</code> if {@link ThreadState} is attached to the current
	 *         {@link Thread}.
	 */
	boolean isAttachedToThread();

	/**
	 * Indicates if changes to the {@link ThreadState} are safe on the current
	 * {@link Thread}.
	 * 
	 * @return <code>true</code> should changes to the {@link ThreadState} be safe
	 *         on the current {@link Thread}.
	 */
	boolean isThreadStateSafe();

	/**
	 * Creates {@link FunctionState} to execute the chain of the first
	 * {@link FunctionState} before moving onto execute the chain of the second
	 * {@link FunctionState}.
	 * 
	 * @param function     Head of initial {@link FunctionState} chain to complete.
	 * @param thenFunction Head of the second {@link FunctionState} chain to then
	 *                     complete next.
	 * @return {@link FunctionState} to execute the chains one after another.
	 */
	FunctionState then(FunctionState function, FunctionState thenFunction);

	/**
	 * Runs the {@link FunctionState} within this {@link ThreadState}.
	 * 
	 * @param function {@link FunctionState} to run within this {@link ThreadState}.
	 * @return {@link FunctionState} running within this {@link ThreadState}.
	 */
	FunctionState runWithin(FunctionState function);

	/**
	 * Runs avoiding the specified {@link Team}.
	 * 
	 * @param function {@link FunctionState} to avoid being executed by the
	 *                 specified {@link Team}.
	 * @param team     {@link TeamManagement} of {@link Team} to avoid.
	 * @return {@link AvoidTeam}.
	 */
	AvoidTeam avoidTeam(FunctionState function, TeamManagement team);

	/**
	 * <p>
	 * Obtains the maximum {@link FunctionState} chain length for this
	 * {@link ThreadState}.
	 * <p>
	 * Once the {@link FunctionState} chain has reached this length, it will be
	 * broken. (spawned in another {@link Thread}). This avoids
	 * {@link StackOverflowError} issues in {@link FunctionState} chain being too
	 * large.
	 * 
	 * @return Maximum {@link FunctionState} chain length for this
	 *         {@link ThreadState}.
	 */
	int getMaximumFunctionChainLength();

	/**
	 * <p>
	 * Run the {@link ThreadSafeOperation}.
	 * <p>
	 * Initially locks are not taken to make the main {@link ThreadState} safe. This
	 * ensures the {@link ThreadSafeOperation} is run under critical section of this
	 * {@link ThreadState}.
	 * 
	 * @param <R>       Return type from {@link ThreadSafeOperation}.
	 * @param <T>       Possible {@link Escalation} from
	 *                  {@link ThreadSafeOperation}.
	 * @param operation {@link ThreadSafeOperation}.
	 * @return Optional return value from {@link ThreadSafeOperation}.
	 * @throws T Optional {@link Throwable} from {@link ThreadSafeOperation}.
	 */
	<R, T extends Throwable> R runThreadSafeOperation(ThreadSafeOperation<R, T> operation) throws T;

	/**
	 * Runs the {@link ProcessSafeOperation}.
	 *
	 * @param <R>       Return type from {@link ProcessSafeOperation}.
	 * @param <T>       Possible {@link Escalation} from
	 *                  {@link ProcessSafeOperation}.
	 * @param operation {@link ProcessSafeOperation}.
	 * @return Optional return value from {@link ProcessSafeOperation}.
	 * @throws T Optional {@link Throwable} from {@link ProcessSafeOperation}.
	 */
	<R, T extends Throwable> R runProcessSafeOperation(ProcessSafeOperation<R, T> operation) throws T;

	/**
	 * Creates a {@link Flow} contained in this {@link ThreadState}.
	 * 
	 * @param flowCompletion       Optional {@link FlowCompletion} to handle
	 *                             completion of the {@link Flow}. May be
	 *                             <code>null</code>.
	 * @param escalationCompletion Optional {@link EscalationCompletion} to handle
	 *                             completion of {@link Flow} and notify
	 *                             {@link Escalation} handling complete. May be
	 *                             <code>null</code>.
	 * @return New {@link Flow}.
	 */
	Flow createFlow(FlowCompletion flowCompletion, EscalationCompletion escalationCompletion);

	/**
	 * Handles the {@link Escalation} from a {@link Flow} of this
	 * {@link ThreadState}.
	 * 
	 * @param escalation {@link Escalation}.
	 * @param completion Optional {@link EscalationCompletion} to be notified of
	 *                   completion of {@link Escalation} handling.
	 * @return {@link FunctionState} to handle the {@link Escalation}.
	 */
	FunctionState handleEscalation(Throwable escalation, EscalationCompletion completion);

	/**
	 * Flags that the input {@link Flow} has completed.
	 * 
	 * @param flow           {@link Flow} that has completed.
	 * @param flowEscalation Possible {@link Escalation} from the {@link Flow}. May
	 *                       be <code>null</code>.
	 * @param completion     {@link EscalationCompletion}. May be <code>null</code>.
	 * @return Optional {@link FunctionState} to complete the {@link Flow}.
	 */
	FunctionState flowComplete(Flow flow, Throwable flowEscalation, EscalationCompletion completion);

	/**
	 * Obtains the {@link ProcessState} of the process containing this
	 * {@link ThreadState}.
	 * 
	 * @return {@link ProcessState} of the process containing this
	 *         {@link ThreadState}.
	 */
	ProcessState getProcessState();

	/**
	 * Obtains the {@link ManagedObjectContainer} for the input index.
	 * 
	 * @param index Index of the {@link ManagedObjectContainer} to be returned.
	 * @return {@link ManagedObjectContainer} for the index.
	 */
	ManagedObjectContainer getManagedObjectContainer(int index);

	/**
	 * Obtains the {@link GovernanceContainer} for the input index.
	 * 
	 * @param index Index of the {@link GovernanceContainer} to be returned.
	 * @return {@link GovernanceContainer} for the index only if active. If not
	 *         active will return <code>null</code>.
	 */
	GovernanceContainer<?> getGovernanceContainer(int index);

	/**
	 * <p>
	 * Indicates if the {@link Governance} is active.
	 * <p>
	 * This provides a quick check and avoids creation of the
	 * {@link GovernanceContainer}.
	 * 
	 * @param index Index of the {@link Governance} to check active.
	 * @return <code>true</code> if the {@link Governance} is active.
	 */
	boolean isGovernanceActive(int index);

	/**
	 * Obtains the {@link FunctionState} to register the {@link ThreadProfiler}.
	 * 
	 * @return {@link FunctionState} to register the {@link ThreadProfiler}. May be
	 *         <code>null</code> if no {@link ThreadProfiler} required.
	 */
	FunctionState registerThreadProfiler();

	/**
	 * Profiles that {@link ManagedObjectContainer} is being executed.
	 * 
	 * @param functionMetaData {@link ManagedFunctionLogicMetaData} of the
	 *                         {@link ManagedFunctionContainer} being executed.
	 */
	void profile(ManagedFunctionLogicMetaData functionMetaData);

}
