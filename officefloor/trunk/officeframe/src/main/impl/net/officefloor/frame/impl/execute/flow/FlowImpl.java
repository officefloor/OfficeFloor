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
package net.officefloor.frame.impl.execute.flow;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.execute.duty.DutyJob;
import net.officefloor.frame.impl.execute.job.AbstractJobContainer;
import net.officefloor.frame.impl.execute.linkedlist.AbstractLinkedListEntry;
import net.officefloor.frame.impl.execute.task.TaskJob;
import net.officefloor.frame.impl.execute.work.WorkContainerProxy;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
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
	private final ThreadState threadState;

	/**
	 * Count of active {@link Job} instances.
	 */
	private int activeJobCount = 0;

	/**
	 * Completion flag indicating if this {@link Flow} is complete.
	 */
	private volatile boolean isFlowComplete = false;

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
	 * ======================= Flow ===========================================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public JobNode createJobNode(TaskMetaData<?, ?, ?> taskMetaData,
			JobNode parallelNodeOwner, Object parameter) {

		// Obtain the work meta-data
		WorkMetaData workMetaData = taskMetaData.getWorkMetaData();

		// Create the work container for a new work
		WorkContainer workContainer = workMetaData
				.createWorkContainer(this.threadState.getProcessState());

		// Obtain the administration meta-data to determine if require proxy
		TaskDutyAssociation[] preTaskDuties = taskMetaData
				.getPreAdministrationMetaData();
		TaskDutyAssociation[] postTaskDuties = taskMetaData
				.getPostAdministrationMetaData();

		// Create the work container proxy (if required)
		WorkContainerProxy proxyWorkContainer = null;
		if ((preTaskDuties.length + postTaskDuties.length) > 0) {
			proxyWorkContainer = new WorkContainerProxy(workContainer);
		}

		// First and last job
		AbstractJobContainer<?, ?>[] firstLastJobs = new AbstractJobContainer<?, ?>[2];

		// Load the pre-task administrator duty jobs.
		// Never use actual work container for pre duties.
		this.loadDutyJobs(firstLastJobs, preTaskDuties, workMetaData, null,
				proxyWorkContainer, parallelNodeOwner, taskMetaData);

		// If no post duties then task is last job
		WorkContainer taskWorkContainer = (postTaskDuties.length == 0 ? workContainer
				: proxyWorkContainer);

		// Load the task job
		AbstractJobContainer<?, ?> taskJob = new TaskJob(this,
				taskWorkContainer, taskMetaData, parallelNodeOwner, parameter);
		this.loadJob(firstLastJobs, taskJob);

		// Load the post-task administrator duty jobs
		this.loadDutyJobs(firstLastJobs, postTaskDuties, workMetaData,
				workContainer, proxyWorkContainer, parallelNodeOwner,
				taskMetaData);

		// Add the jobs to the job count for this flow
		JobNode jobNode = firstLastJobs[0];
		while (jobNode != null) {
			this.activeJobCount++;
			jobNode = jobNode.getNextNode();
		}

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
	 * @param actualWorkContainer
	 *            Actual {@link WorkContainer}.
	 * @param proxyWorkContainer
	 *            {@link WorkContainerProxy}.
	 * @param parallelNodeOwner
	 *            Parallel owning {@link JobNode}.
	 * @param administeringTaskMetaData
	 *            {@link TaskMetaData} of the {@link Task} being administered.
	 */
	@SuppressWarnings("unchecked")
	private void loadDutyJobs(AbstractJobContainer<?, ?>[] firstLastJobs,
			TaskDutyAssociation<?>[] taskDutyAssociations,
			WorkMetaData<?> workMetaData, WorkContainer<?> actualWorkContainer,
			WorkContainerProxy<?> proxyWorkContainer,
			JobNode parallelNodeOwner,
			TaskMetaData<?, ?, ?> administeringTaskMetaData) {

		// Load the duty jobs
		for (int i = 0; i < taskDutyAssociations.length; i++) {
			TaskDutyAssociation<?> taskDutyAssociation = taskDutyAssociations[i];

			// Obtain the associated administrator meta-data
			AdministratorMetaData<?, ?> adminMetaData;
			AdministratorIndex adminIndex = taskDutyAssociation
					.getAdministratorIndex();
			int indexInScope = adminIndex.getIndexOfAdministratorWithinScope();
			switch (adminIndex.getAdministratorScope()) {
			case WORK:
				adminMetaData = workMetaData.getAdministratorMetaData()[indexInScope];
				break;
			case THREAD:
				adminMetaData = this.threadState.getThreadMetaData()
						.getAdministratorMetaData()[indexInScope];
				break;
			case PROCESS:
				adminMetaData = this.threadState.getProcessState()
						.getProcessMetaData().getAdministratorMetaData()[indexInScope];
				break;
			default:
				throw new IllegalStateException("Unknown administrator scope "
						+ adminIndex.getAdministratorScope());
			}

			// Determine the work container to use
			WorkContainer<?> workContainer;
			if (actualWorkContainer == null) {
				// No actual so always use the proxy
				workContainer = proxyWorkContainer;
			} else {
				// Only use actual on last duty
				workContainer = (i == (taskDutyAssociations.length - 1)) ? actualWorkContainer
						: proxyWorkContainer;
			}

			// Create the duty job
			AbstractJobContainer<?, ?> dutyJob = new DutyJob(this,
					workContainer, adminMetaData, taskDutyAssociation,
					parallelNodeOwner, administeringTaskMetaData);

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
	private void loadJob(AbstractJobContainer<?, ?>[] firstLastJobs,
			AbstractJobContainer<?, ?> newJob) {
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

	@Override
	public void jobComplete(Job taskContainer, JobActivateSet notifySet) {
		// Task container now inactive
		this.activeJobCount--;

		// Determine if flow is complete
		if (this.activeJobCount == 0) {
			// Flow complete
			this.isFlowComplete = true;
			this.removeFromLinkedList(notifySet);
		}
	}

	@Override
	public ThreadState getThreadState() {
		return this.threadState;
	}

	/*
	 * ===================== FlowFuture ===================================
	 */

	@Override
	public boolean isComplete() {
		return this.isFlowComplete;
	}

	/*
	 * ===================== FlowAsset ========================================
	 */

	@Override
	public boolean waitOnFlow(JobNode jobNode, JobActivateSet notifySet) {
		// Delegate to the thread
		return this.threadState.waitOnFlow(jobNode, notifySet);
	}

}