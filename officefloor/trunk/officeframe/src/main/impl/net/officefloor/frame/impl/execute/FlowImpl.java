/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute;

import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadWorkLink;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.team.Job;

/**
 * Implementation of the {@link Flow}.
 * 
 * @author Daniel
 */
public class FlowImpl extends AbstractLinkedListEntry<Flow, JobActivateSet>
		implements Flow {

	/**
	 * {@link ThreadState} that this {@link Flow} is bound.
	 */
	protected final ThreadState threadState;

	/**
	 * Count of active {@link Job} instances.
	 */
	protected int activeTaskCount = 0;

	/**
	 * Completion flag indicating if this {@link Flow} is complete.
	 */
	protected volatile boolean isFlowComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param threadState
	 *            {@link ThreadState} for this {@link Flow}.
	 * @param flows
	 *            {@link LinkedList} of {@link Flow} instances for the
	 *            {@link ThreadState}.
	 */
	public FlowImpl(ThreadState threadState,
			LinkedList<Flow, JobActivateSet> flows) {
		super(flows);
		this.threadState = threadState;
	}

	/*
	 * ========================================================================
	 * Flow
	 * ========================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Flow#createTaskContainer(net.officefloor.frame.internal.structure.TaskMetaData,
	 *      net.officefloor.frame.internal.structure.TaskNode, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Job createJob(TaskMetaData<?, ?, ?, ?> taskMetaData,
			JobNode parallelNodeOwner, Object parameter,
			ThreadWorkLink<?> currentWorkLink) {

		// Obtain the work meta-data
		WorkMetaData workMetaData = taskMetaData.getWorkMetaData();

		// Obtain the work link
		ThreadWorkLink newWorkLink;
		if (currentWorkLink.getWorkContainer().getWorkId() == workMetaData
				.getWorkId()) {
			// Same work, thus re-use work
			newWorkLink = currentWorkLink
					.createThreadWorkLink(this.threadState);
		} else {
			// Source work (as different work)
			// TODO: consider bounding work to Process
			WorkContainer newWorkContainer = new WorkContainerImpl(workMetaData
					.getWorkFactory().createWork(), workMetaData,
					this.threadState.getProcessState());

			// Create thread work link for the new work
			newWorkLink = new ThreadWorkLinkImpl(this.threadState,
					newWorkContainer);
		}

		// First and last job
		JobContainer<?, ?>[] firstLastJobs = new JobContainer<?, ?>[2];

		// Load the pre-task administrator duty jobs
		this.loadDutyJobs(firstLastJobs, taskMetaData
				.getPreAdministrationMetaData(), workMetaData, newWorkLink,
				parallelNodeOwner);

		// Load the task job
		JobContainer<?, ?> taskJob = new TaskJob(this.threadState, this,
				newWorkLink, taskMetaData, parallelNodeOwner, parameter);
		this.loadJob(firstLastJobs, taskJob);

		// Load the post-task administrator duty jobs
		this.loadDutyJobs(firstLastJobs, taskMetaData
				.getPostAdministrationMetaData(), workMetaData, newWorkLink,
				parallelNodeOwner);

		// Register the jobs with the work
		JobContainer<?, ?> job = firstLastJobs[0];
		while (job != null) {
			newWorkLink.registerJob(job);
			job = (JobContainer<?, ?>) job.getNextNode();
		}

		// Increment the number of active task containers
		this.activeTaskCount++;

		// Return the starting job
		return firstLastJobs[0];
	}

	/**
	 * Loads the {@link DutyJob} instances.
	 * 
	 * @param firstLastJobs
	 *            First and last {@link JobNode} instances.
	 * @param taskDutyAssociations
	 *            {@link TaskDutyAssociation} instances for the {@link DutyJob}
	 *            instances.
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @param threadWorkLink
	 *            {@link ThreadWorkLink}.
	 * @param parallelNodeOwner
	 *            Parallel owning {@link JobNode}.
	 */
	@SuppressWarnings("unchecked")
	private void loadDutyJobs(JobContainer<?, ?>[] firstLastJobs,
			TaskDutyAssociation<?>[] taskDutyAssociations,
			WorkMetaData<?> workMetaData, ThreadWorkLink<?> threadWorkLink,
			JobNode parallelNodeOwner) {
		// Load the duty jobs
		for (TaskDutyAssociation<?> taskDutyAssociation : taskDutyAssociations) {

			// Obtain the associated administrator meta-data
			AdministratorMetaData<?, ?> adminMetaData = workMetaData
					.getAdministratorMetaData()[taskDutyAssociation
					.getAdministratorIndex()];

			// Create the duty job
			JobContainer<?, ?> dutyJob = new DutyJob(this.threadState, this,
					threadWorkLink, adminMetaData, taskDutyAssociation,
					parallelNodeOwner);

			// Load the duty job
			this.loadJob(firstLastJobs, dutyJob);
		}
	}

	/**
	 * Loads the {@link Job} to the listing of {@link Job} instances.
	 * 
	 * @param firstLastJobs
	 *            Array containing two elements, first and last {@link Job}
	 *            instances.
	 * @param newJob
	 *            New {@link JobNode}.
	 */
	private void loadJob(JobContainer<?, ?>[] firstLastJobs,
			JobContainer<?, ?> newJob) {
		if (firstLastJobs[0] == null) {
			// First job
			firstLastJobs[0] = newJob;
			firstLastJobs[1] = newJob;
		} else {
			// Another job (append for sequential execution)
			firstLastJobs[1].setNextNode(newJob);
			firstLastJobs[1] = newJob;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Flow#taskContainerComplete(net.officefloor.frame.spi.team.TaskContainer,
	 *      net.officefloor.frame.internal.structure.AssetNotifySet)
	 */
	public void jobComplete(Job taskContainer, JobActivateSet notifySet) {
		// Task container now inactive
		this.activeTaskCount--;

		// Determine if flow is complete
		if (this.activeTaskCount == 0) {
			// Flow complete
			this.isFlowComplete = true;
			this.removeFromLinkedList(notifySet);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Flow#getThreadState()
	 */
	public ThreadState getThreadState() {
		return this.threadState;
	}

	/*
	 * ====================================================================
	 * FlowFuture
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.FlowFuture#isComplete()
	 */
	public boolean isComplete() {
		return this.isFlowComplete;
	}

	/*
	 * ========================================================================
	 * FlowAsset
	 * ========================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.FlowAsset#waitOnFlow(net.officefloor.frame.spi.team.TaskContainer,
	 *      net.officefloor.frame.internal.structure.AssetNotifySet)
	 */
	public boolean waitOnFlow(Job taskContainer, JobActivateSet notifySet) {
		// Delegate to the thread
		return this.threadState.waitOnFlow(taskContainer, notifySet);
	}

}
