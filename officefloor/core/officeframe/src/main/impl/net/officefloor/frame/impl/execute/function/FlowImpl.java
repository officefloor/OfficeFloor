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
package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.execute.duty.DutyJob;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.work.WorkContainerProxy;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunction;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * Implementation of the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowImpl extends AbstractLinkedListSetEntry<Flow, ThreadState> implements Flow {

	/**
	 * Activate {@link FunctionState} instances for this {@link Flow}.
	 */
	private final LinkedListSet<ManagedFunction, Flow> activeJobNodes = new StrictLinkedListSet<ManagedFunction, Flow>() {
		@Override
		protected Flow getOwner() {
			return FlowImpl.this;
		}
	};

	/**
	 * {@link ThreadState} that this {@link Flow} is bound.
	 */
	private final ThreadState threadState;

	/**
	 * Initiate.
	 * 
	 * @param threadState
	 *            {@link ThreadState} containing this {@link Flow}.
	 */
	public FlowImpl(ThreadState threadState) {
		this.threadState = threadState;
	}

	/*
	 * ================== LinkedListSetEntry ================================
	 */

	@Override
	public ThreadState getLinkedListSetOwner() {
		return this.threadState;
	}

	/*
	 * ======================= Flow ===================================
	 */

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedFunction createManagedJobNode(TaskMetaData<?, ?, ?> taskMetaData, ManagedFunction parallelNodeOwner,
			Object parameter, GovernanceDeactivationStrategy governanceDeactivationStrategy) {

		// Obtain the work meta-data
		WorkMetaData workMetaData = taskMetaData.getWorkMetaData();

		// Create the work container for a new work
		WorkContainer workContainer = workMetaData.createWorkContainer(this.threadState);

		// Obtain the administration meta-data to determine if require proxy
		TaskDutyAssociation[] preTaskDuties = taskMetaData.getPreAdministrationMetaData();
		TaskDutyAssociation[] postTaskDuties = taskMetaData.getPostAdministrationMetaData();

		// Create the work container proxy (if required)
		WorkContainerProxy proxyWorkContainer = null;
		if ((preTaskDuties.length + postTaskDuties.length) > 0) {
			proxyWorkContainer = new WorkContainerProxy(workContainer);
		}

		// First and last job
		ManagedFunction[] firstLastJobs = new ManagedFunction[2];

		// Load the pre-task administrator duty jobs.
		// Never use actual work container for pre duties.
		this.loadDutyJobs(firstLastJobs, preTaskDuties, workMetaData, null, proxyWorkContainer, parallelNodeOwner,
				taskMetaData);

		// If no post duties then task is last job
		WorkContainer taskWorkContainer = (postTaskDuties.length == 0 ? workContainer : proxyWorkContainer);

		// Create and register the active task job
		ManagedFunction taskJob = taskMetaData.createTaskNode(this, taskWorkContainer, parallelNodeOwner, parameter,
				governanceDeactivationStrategy);
		this.activeJobNodes.addEntry(taskJob);

		// Load the task job
		this.loadJob(firstLastJobs, taskJob);

		// Load the post-task administrator duty jobs
		this.loadDutyJobs(firstLastJobs, postTaskDuties, workMetaData, workContainer, proxyWorkContainer,
				parallelNodeOwner, taskMetaData);

		// Return the starting job
		return firstLastJobs[0];
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ManagedFunction createGovernanceNode(GovernanceActivity<?, ?> governanceActivity,
			ManagedFunction parallelNodeOwner) {

		// Create and register the governance job
		GovernanceMetaData governanceMetaData = governanceActivity.getGovernanceMetaData();
		ManagedFunction governanceJob = governanceMetaData.createGovernanceJobNode(this, governanceActivity,
				parallelNodeOwner);
		this.activeJobNodes.addEntry(governanceJob);

		// Return the governance job
		return governanceJob;
	}

	/**
	 * Loads the {@link DutyJob} instances.
	 * 
	 * @param firstLastJobs
	 *            First and last {@link ManagedFunction} instances.
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
	 *            Parallel owning {@link ManagedFunction}.
	 * @param administeringTaskMetaData
	 *            {@link TaskMetaData} of the {@link Task} being administered.
	 */
	private void loadDutyJobs(ManagedFunction[] firstLastJobs, TaskDutyAssociation<?>[] taskDutyAssociations,
			WorkMetaData<?> workMetaData, WorkContainer<?> actualWorkContainer,
			WorkContainerProxy<?> proxyWorkContainer, ManagedFunction parallelNodeOwner,
			TaskMetaData<?, ?, ?> administeringTaskMetaData) {

		// Load the duty jobs
		for (int i = 0; i < taskDutyAssociations.length; i++) {
			TaskDutyAssociation<?> taskDutyAssociation = taskDutyAssociations[i];

			// Obtain the associated administrator meta-data
			AdministratorMetaData<?, ?> adminMetaData;
			AdministratorIndex adminIndex = taskDutyAssociation.getAdministratorIndex();
			int indexInScope = adminIndex.getIndexOfAdministratorWithinScope();
			switch (adminIndex.getAdministratorScope()) {
			case WORK:
				adminMetaData = workMetaData.getAdministratorMetaData()[indexInScope];
				break;
			case THREAD:
				adminMetaData = this.threadState.getThreadMetaData().getAdministratorMetaData()[indexInScope];
				break;
			case PROCESS:
				adminMetaData = this.threadState.getProcessState().getProcessMetaData()
						.getAdministratorMetaData()[indexInScope];
				break;
			default:
				throw new IllegalStateException("Unknown administrator scope " + adminIndex.getAdministratorScope());
			}

			// Determine the work container to use
			WorkContainer<?> workContainer;
			if (actualWorkContainer == null) {
				// No actual so always use the proxy
				workContainer = proxyWorkContainer;
			} else {
				// Only use actual on last duty
				workContainer = (i == (taskDutyAssociations.length - 1)) ? actualWorkContainer : proxyWorkContainer;
			}

			// Create and register the active duty job
			ManagedFunction dutyJob = adminMetaData.createDutyNode(administeringTaskMetaData, workContainer, this,
					taskDutyAssociation, parallelNodeOwner);
			this.activeJobNodes.addEntry(dutyJob);

			// Load the duty job
			this.loadJob(firstLastJobs, dutyJob);
		}
	}

	/**
	 * Loads the {@link ManagedFunction} to the listing of
	 * {@link ManagedFunction} instances.
	 * 
	 * @param firstLastJobs
	 *            Array containing two elements, first and last
	 *            {@link ManagedFunction} instances.
	 * @param newJob
	 *            New {@link ManagedFunction}.
	 */
	private void loadJob(ManagedFunction[] firstLastJobs, ManagedFunction newJob) {
		if (firstLastJobs[0] == null) {
			// First job
			firstLastJobs[0] = newJob;
			firstLastJobs[1] = newJob;
		} else {
			// Another job (append for sequential execution)
			firstLastJobs[1].setNextManagedFunction(newJob);
			firstLastJobs[1] = newJob;
		}
	}

	@Override
	public FunctionState managedJobNodeComplete(ManagedFunction jobNode) {

		// Remove JobNode from active JobNode listing
		if (this.activeJobNodes.removeEntry(jobNode)) {
			// Last active JobNode so flow is now complete
			return this.threadState.flowComplete(this);
		}

		// Flow still active
		return null;
	}

	@Override
	public ThreadState getThreadState() {
		return this.threadState;
	}

}