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

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.impl.execute.administrator.AdministratorContainerImpl.DutyFunction;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.work.WorkContainerProxy;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionDutyAssociation;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
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
	private final LinkedListSet<ManagedFunctionContainer, Flow> activeFunctions = new StrictLinkedListSet<ManagedFunctionContainer, Flow>() {
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
	public ManagedFunctionContainer createManagedFunction(ManagedFunctionMetaData<?, ?, ?> managedFunctionMetaData,
			ManagedFunctionContainer parallelFunctionOwner, Object parameter,
			GovernanceDeactivationStrategy governanceDeactivationStrategy) {

		// Obtain the work meta-data
		WorkMetaData workMetaData = managedFunctionMetaData.getWorkMetaData();

		// Create the work container for a new work
		WorkContainer workContainer = workMetaData.createWorkContainer(this.threadState);

		// Obtain the administration meta-data to determine if require proxy
		ManagedFunctionDutyAssociation[] preTaskDuties = managedFunctionMetaData.getPreAdministrationMetaData();
		ManagedFunctionDutyAssociation[] postTaskDuties = managedFunctionMetaData.getPostAdministrationMetaData();

		// Create the work container proxy (if required)
		WorkContainerProxy proxyWorkContainer = null;
		if ((preTaskDuties.length + postTaskDuties.length) > 0) {
			proxyWorkContainer = new WorkContainerProxy(workContainer);
		}

		// First and last job
		ManagedFunctionContainer[] firstLastJobs = new ManagedFunctionContainer[2];

		// Load the pre-task administrator duty jobs.
		// Never use actual work container for pre duties.
		this.loadDutyJobs(firstLastJobs, preTaskDuties, workMetaData, null, proxyWorkContainer, parallelFunctionOwner,
				managedFunctionMetaData);

		// If no post duties then task is last job
		WorkContainer taskWorkContainer = (postTaskDuties.length == 0 ? workContainer : proxyWorkContainer);

		// Create and register the managed function
		ManagedFunctionContainer managedFunction = managedFunctionMetaData.createManagedFunctionContainer(this,
				workContainer, parallelFunctionOwner, parameter, governanceDeactivationStrategy);
		this.activeFunctions.addEntry(managedFunction);

		// Load the task job
		this.loadJob(firstLastJobs, managedFunction);

		// Load the post-task administrator duty jobs
		this.loadDutyJobs(firstLastJobs, postTaskDuties, workMetaData, workContainer, proxyWorkContainer,
				parallelFunctionOwner, managedFunctionMetaData);

		// Return the starting function
		return firstLastJobs[0];
	}

	@Override
	public <F extends Enum<F>> ManagedFunctionContainer createGovernanceFunction(
			GovernanceActivity<F> governanceActivity, GovernanceMetaData<?, F> governanceMetaData) {

		// Create the governance function
		ManagedFunctionContainer governanceFunction = governanceMetaData.createGovernanceFunction(governanceActivity,
				this);
		this.activeFunctions.addEntry(governanceFunction);

		// Return the governance function
		return governanceFunction;
	}

	/**
	 * Loads the {@link DutyFunction} instances.
	 * 
	 * @param firstLastJobs
	 *            First and last {@link ManagedFunctionContainer} instances.
	 * @param taskDutyAssociations
	 *            {@link ManagedFunctionDutyAssociation} instances for the
	 *            {@link DutyFunction} instances.
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @param actualWorkContainer
	 *            Actual {@link WorkContainer}.
	 * @param proxyWorkContainer
	 *            {@link WorkContainerProxy}.
	 * @param parallelNodeOwner
	 *            Parallel owning {@link ManagedFunctionContainer}.
	 * @param administeringTaskMetaData
	 *            {@link ManagedFunctionMetaData} of the {@link ManagedFunction}
	 *            being administered.
	 */
	private void loadDutyJobs(ManagedFunctionContainer[] firstLastJobs,
			ManagedFunctionDutyAssociation<?>[] taskDutyAssociations, WorkMetaData<?> workMetaData,
			WorkContainer<?> actualWorkContainer, WorkContainerProxy<?> proxyWorkContainer,
			ManagedFunctionContainer parallelNodeOwner, ManagedFunctionMetaData<?, ?, ?> administeringTaskMetaData) {

		// Load the duty jobs
		for (int i = 0; i < taskDutyAssociations.length; i++) {
			ManagedFunctionDutyAssociation<?> taskDutyAssociation = taskDutyAssociations[i];

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

			// Obtain the administrator container
			int adminScopeIndex = adminIndex.getIndexOfAdministratorWithinScope();
			AdministratorContainer adminContainer;
			switch (adminIndex.getAdministratorScope()) {
			case WORK:
				adminContainer = workContainer.getAdministratorContainer(adminScopeIndex);
				break;

			case THREAD:
				adminContainer = this.threadState.getAdministratorContainer(adminScopeIndex);
				break;

			case PROCESS:
				adminContainer = this.threadState.getProcessState().getAdministratorContainer(adminScopeIndex);
				break;

			default:
				throw new IllegalStateException("Unknown administrator scope " + adminIndex.getAdministratorScope());
			}

			// Create and register the active duty job
			ManagedFunctionContainer dutyFunction = adminContainer.administerManagedObjects(taskDutyAssociation, this,
					administeringTaskMetaData, workContainer);
			this.activeFunctions.addEntry(dutyFunction);

			// Load the duty job
			this.loadJob(firstLastJobs, dutyFunction);
		}
	}

	/**
	 * Loads the {@link ManagedFunctionContainer} to the listing of
	 * {@link ManagedFunctionContainer} instances.
	 * 
	 * @param firstLastJobs
	 *            Array containing two elements, first and last
	 *            {@link ManagedFunctionContainer} instances.
	 * @param newJob
	 *            New {@link ManagedFunctionContainer}.
	 */
	private void loadJob(ManagedFunctionContainer[] firstLastJobs, ManagedFunctionContainer newJob) {
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
	public FunctionState managedFunctionComplete(ManagedFunctionContainer function) {

		// Remove function from active function listing
		if (this.activeFunctions.removeEntry(function)) {
			// Last active function so flow is now complete
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