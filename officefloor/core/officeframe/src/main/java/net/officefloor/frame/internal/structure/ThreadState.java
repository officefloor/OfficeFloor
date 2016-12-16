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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * <p>
 * State of a thread within the {@link ProcessState}.
 * <p>
 * May be used as a {@link LinkedListSetEntry} in a list of {@link ThreadState}
 * instances for a {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadState extends FlowAsset, LinkedListSetEntry<ThreadState, ProcessState> {

	/**
	 * Obtains the {@link ThreadMetaData} for this {@link ThreadState}.
	 * 
	 * @return {@link ThreadMetaData} for this {@link ThreadState}.
	 */
	ThreadMetaData getThreadMetaData();

	/**
	 * Undertakes executing the {@link JobNode} loop.
	 * 
	 * @param job
	 *            Head {@link JobNode} to execute.
	 * @param context
	 *            {@link JobContext}.
	 */
	void doJobNodeLoop(JobNode head, JobContext context);

	/**
	 * Indicates if the current {@link Thread} is the {@link Thread} executing
	 * the {@link JobNode} loop for this {@link ThreadState}.
	 * 
	 * @return <code>true</cod> if current {@link Thread} is the {@link Thread}
	 *         executing the {@link JobNode} loop for this {@link ThreadState}.
	 */
	boolean isJobNodeLoopThread();

	/**
	 * Registers the {@link JobNodeRunnable} to be executed by this
	 * {@link ThreadState}.
	 * 
	 * @param runnable
	 *            {@link JobNodeRunnable}.
	 * @param responsibleTeam
	 *            {@link TeamManagement} responsible for undertaking the
	 *            {@link JobNodeRunnable}. May be <code>null</code> to use
	 *            default {@link TeamManagement}.
	 */
	void run(JobNodeRunnable runnable, TeamManagement responsibleTeam);

	/**
	 * Spawns a new {@link ThreadState}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} for the {@link ThreadState}.
	 * @param parameter
	 *            Parameter for the first {@link Task} of the
	 *            {@link ThreadState}.
	 * @param instigatingJobNode
	 *            {@link JobNode} spawning the {@link ThreadState}.
	 * @param callback
	 *            Optional {@link FlowCallbackJobNodeFactory} to create a
	 *            {@link JobNode} be invoked on completion of the spawned
	 *            {@link ThreadState}.
	 */
	void spawnThreadState(FlowMetaData<?> flowMetaData, Object parameter, JobNode instigatingJobNode,
			FlowCallbackJobNodeFactory flowCallbackFactory);

	/**
	 * Undertakes a critical section for the {@link ProcessState}.
	 * 
	 * @param section
	 *            {@link JobNodeRunnable} containing the critical section logic.
	 * @param responsibleTeam
	 *            Responsible {@link TeamManagement} for the
	 *            {@link JobNodeRunnable}.
	 * @param continueJobNode
	 *            {@link JobNode} to continue with once the
	 *            {@link JobNodeRunnable} is complete.
	 */
	void runProcessCriticalSection(JobNodeRunnable criticalSection, TeamManagement responsibleTeam,
			JobNode continueJobNode);

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
	@Deprecated // change to createFlow
	Flow createJobSequence();

	/**
	 * Flags that the input {@link Flow} has completed.
	 * 
	 * @param jobSequence
	 *            {@link Flow} that has completed.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} completing
	 *            the {@link Flow}.
	 * @param continueJobNode
	 *            {@link JobNode} to continue in completing the {@link Flow}.
	 * @return Optional {@link JobNode} to complete the {@link Flow}.
	 */
	@Deprecated // change to flowComplete
	JobNode jobSequenceComplete(Flow jobSequence, TeamIdentifier currentTeam, JobNode continueJobNode);

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