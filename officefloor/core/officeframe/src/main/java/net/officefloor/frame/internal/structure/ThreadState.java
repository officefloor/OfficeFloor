/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;

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
	 * @return <code>true</cod> if {@link ThreadState} is attached to the
	 *         current {@link Thread}.
	 */
	boolean isAttachedToThread();

	/**
	 * Indicates if changes to the {@link ThreadState} are safe on the current
	 * {@link Thread}.
	 * 
	 * @return <code>true</code> should changes to the {@link ThreadState} be
	 *         safe on the current {@link Thread}.
	 */
	boolean isThreadStateSafe();

	/**
	 * Creates a {@link Flow} contained in this {@link ThreadState}.
	 * 
	 * @param completion
	 *            Optional {@link FlowCompletion} to handle completion of the
	 *            {@link Flow}. May be <code>null</code>.
	 * @return New {@link Flow}.
	 */
	Flow createFlow(FlowCompletion completion);

	/**
	 * Handles the {@link Escalation} from a {@link Flow} of this
	 * {@link ThreadState}.
	 * 
	 * @param escalation
	 *            {@link Escalation}.
	 * @return {@link FunctionState} to handle the {@link Escalation}.
	 */
	FunctionState handleEscalation(Throwable escalation);

	/**
	 * Flags that the input {@link Flow} has completed.
	 * 
	 * @param flow
	 *            {@link Flow} that has completed.
	 * @param isCancel
	 *            Flags whether completing due to cancel.
	 * @return Optional {@link FunctionState} to complete the {@link Flow}.
	 */
	FunctionState flowComplete(Flow flow, boolean isCancel);

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
	 * @param index
	 *            Index of the {@link ManagedObjectContainer} to be returned.
	 * @return {@link ManagedObjectContainer} for the index.
	 */
	ManagedObjectContainer getManagedObjectContainer(int index);

	/**
	 * Obtains the {@link GovernanceContainer} for the input index.
	 * 
	 * @param index
	 *            Index of the {@link GovernanceContainer} to be returned.
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
	 * @param index
	 *            Index of the {@link Governance} to check active.
	 * @return <code>true</code> if the {@link Governance} is active.
	 */
	boolean isGovernanceActive(int index);

	/**
	 * Profiles that {@link ManagedObjectContainer} is being executed.
	 * 
	 * @param functionMetaData
	 *            {@link ManagedFunctionLogicMetaData} of the
	 *            {@link ManagedFunctionContainer} being executed.
	 */
	void profile(ManagedFunctionLogicMetaData functionMetaData);

}