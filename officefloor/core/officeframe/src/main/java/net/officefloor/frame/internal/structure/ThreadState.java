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

import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.team.Job;

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
	 * Obtains the {@link ThreadMetaData} for this {@link ThreadState}.
	 * 
	 * @return {@link ThreadMetaData} for this {@link ThreadState}.
	 */
	ThreadMetaData getThreadMetaData();

	/**
	 * Attaches this {@link ThreadState} to the current {@link Thread}.
	 */
	void attachThreadStateToThread();

	/**
	 * Detaches this {@link ThreadState} from the current {@link Thread}.
	 */
	void detachThreadStateFromThread();

	/**
	 * Indicates if this {@link ThreadState} is attached to the current
	 * {@link Thread}.
	 * 
	 * @return <code>true</cod> if {@link ThreadState} is attached to the
	 *         current {@link Thread}.
	 */
	boolean isAttachedToThread();

	/**
	 * Returning a {@link Throwable} indicates that the thread has failed.
	 * 
	 * @return {@link Throwable} indicating the thread has failed or
	 *         <code>null</code> indicating thread still going fine.
	 */
	Throwable getFailure();

	/**
	 * Sets the {@link Throwable} cause to indicate that the thread has failed.
	 * 
	 * @param cause
	 *            Cause of the thread's failure.
	 */
	void setFailure(Throwable cause);

	/**
	 * Creates a {@link Flow} contained in this {@link ThreadState}.
	 * 
	 * @return New {@link Flow}.
	 */
	Flow createFlow();

	/**
	 * Flags that the input {@link Flow} has completed.
	 * 
	 * @param flow
	 *            {@link Flow} that has completed.
	 * @param continueJobNode
	 *            {@link JobNode} to continue in completing the {@link Flow}.
	 * @return Optional {@link JobNode} to complete the {@link Flow}.
	 */
	JobNode flowComplete(Flow flow, JobNode continueJobNode);

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
	 * <p>
	 * Checks whether the particular {@link Governance} is active.
	 * <p>
	 * This enables light weight checking by not having to create the
	 * {@link GovernanceContainer}.
	 * 
	 * @param index
	 *            Index of the {@link GovernanceContainer} to determine if the
	 *            {@link Governance} is active.
	 * @return <code>true</code> if the {@link Governance} is activate.
	 */
	boolean isGovernanceActive(int index);

	/**
	 * Obtains the {@link GovernanceContainer} for the input index.
	 * 
	 * @param index
	 *            Index of the {@link GovernanceContainer} to be returned.
	 * @return {@link GovernanceContainer} for the index only if active. If not
	 *         active will return <code>null</code>.
	 */
	GovernanceContainer<?, ?> getGovernanceContainer(int index);

	/**
	 * Flags the {@link Governance} has completed.
	 * 
	 * @param governanceContainer
	 *            {@link GovernanceContainer} of the completed
	 *            {@link Governance}.
	 */
	void governanceComplete(GovernanceContainer<?, ?> governanceContainer);

	/**
	 * Obtains the {@link AdministratorContainer} for the input index.
	 * 
	 * @param index
	 *            Index of the {@link AdministratorContainer} to be returned.
	 * @return {@link AdministratorContainer} for the index.
	 */
	AdministratorContainer<?, ?> getAdministratorContainer(int index);

	/**
	 * <p>
	 * Flags that escalation is about to happen on this {@link ThreadState}.
	 * <p>
	 * This allows the {@link ThreadState} to know not to clean up should all
	 * its {@link Flow} instances be closed and a new one will be created for
	 * the {@link EscalationFlow}.
	 * 
	 * @param currentJobNode
	 *            Current {@link JobNode} being executed.
	 */
	void escalationStart(JobNode currentJobNode);

	/**
	 * Flags that escalation has complete on this {@link ThreadState}.
	 * 
	 * @param currentJobNode
	 *            Current {@link JobNode} being executed.
	 */
	void escalationComplete(JobNode currentJobNode);

	/**
	 * Obtains the {@link EscalationLevel} of this {@link ThreadState}.
	 * 
	 * @return {@link EscalationLevel} of this {@link ThreadState}.
	 */
	EscalationLevel getEscalationLevel();

	/**
	 * Specifies the {@link EscalationLevel} for this {@link ThreadState}.
	 * 
	 * @param escalationLevel
	 *            {@link EscalationLevel}.
	 */
	void setEscalationLevel(EscalationLevel escalationLevel);

	/**
	 * Profiles that {@link Job} is being executed.
	 * 
	 * @param jobMetaData
	 *            {@link JobMetaData} of the {@link Job} being executed.
	 */
	void profile(JobMetaData jobMetaData);

}