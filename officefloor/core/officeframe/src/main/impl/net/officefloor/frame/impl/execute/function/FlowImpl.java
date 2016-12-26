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
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.LinkedListSetItem;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionDutyAssociation;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Implementation of the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowImpl extends AbstractLinkedListSetEntry<Flow, ThreadState> implements Flow {

	/**
	 * Activate {@link FunctionState} instances for this {@link Flow}.
	 */
	private final LinkedListSet<FunctionState, Flow> activeFunctions = new StrictLinkedListSet<FunctionState, Flow>() {
		@Override
		protected Flow getOwner() {
			return FlowImpl.this;
		}
	};

	/**
	 * {@link FlowCompletion} of this {@link Flow}.
	 */
	private final FlowCompletion completion;

	/**
	 * {@link ThreadState} that this {@link Flow} is bound.
	 */
	private final ThreadState threadState;

	/**
	 * Initiate.
	 * 
	 * @param completion
	 *            {@link FlowCompletion} of this {@link Flow}.
	 * @param threadState
	 *            {@link ThreadState} containing this {@link Flow}.
	 */
	public FlowImpl(FlowCompletion completion, ThreadState threadState) {
		this.completion = completion;
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
	public FunctionState createFunction(FunctionLogic logic) {
		FunctionState function = new AbstractFunctionState(this) {
			@Override
			public FunctionState execute() throws Throwable {
				return logic.execute(FlowImpl.this);
			}
		};
		this.activeFunctions.addEntry(function);
		return function;
	}

	@Override
	public <O extends Enum<O>, F extends Enum<F>> ManagedFunctionContainer createManagedFunction(
			ManagedFunctionMetaData<O, F> managedFunctionMetaData, ManagedFunctionContainer parallelFunctionOwner,
			Object parameter, GovernanceDeactivationStrategy governanceDeactivationStrategy) {

		// Obtain the administration meta-data to determine if require proxy
		ManagedFunctionDutyAssociation<?>[] preTaskDuties = managedFunctionMetaData.getPreAdministrationMetaData();
		ManagedFunctionDutyAssociation<?>[] postTaskDuties = managedFunctionMetaData.getPostAdministrationMetaData();

		// Create the managed function container
		ManagedFunctionContainer managedFunctionContainer = managedFunctionMetaData.createManagedFunctionContainer(this,
				parallelFunctionOwner, parameter, governanceDeactivationStrategy);

		// First and last function (to add administration duties)
		ManagedFunctionContainer[] firstLastFunctions = new ManagedFunctionContainer[2];

		// Load the pre-function administrator duty functions
		this.loadDutyFunctions(firstLastFunctions, preTaskDuties, managedFunctionContainer, parallelFunctionOwner,
				managedFunctionMetaData);

		// Load and register the managed function container
		this.activeFunctions.addEntry(managedFunctionContainer);
		this.loadFunction(firstLastFunctions, managedFunctionContainer);

		// Load the post-function administrator duty functions
		this.loadDutyFunctions(firstLastFunctions, postTaskDuties, managedFunctionContainer, parallelFunctionOwner,
				managedFunctionMetaData);

		// Return the starting function
		return firstLastFunctions[0];
	}

	@Override
	public <F extends Enum<F>> FunctionState createGovernanceFunction(GovernanceActivity<F> governanceActivity,
			GovernanceMetaData<?, F> governanceMetaData) {

		// Create and register the governance function
		FunctionState governanceFunction = governanceMetaData.createGovernanceFunction(governanceActivity, this);
		this.activeFunctions.addEntry(governanceFunction);

		// Return the governance function
		return governanceFunction;
	}

	/**
	 * Loads the {@link DutyFunction} instances.
	 * 
	 * @param firstLastFunctions
	 *            First and last {@link ManagedFunctionContainer} instances.
	 * @param functionDutyAssociations
	 *            {@link ManagedFunctionDutyAssociation} instances for the
	 *            {@link DutyFunction} instances.
	 * @param managedFunctionContainer
	 *            {@link ManagedFunctionContainer} for the
	 *            {@link ManagedObject}.
	 * @param parallelOwner
	 *            Parallel owning {@link ManagedFunctionContainer}.
	 * @param administeringFunctionMetaData
	 *            {@link ManagedFunctionMetaData} of the {@link ManagedFunction}
	 *            being administered.
	 */
	private void loadDutyFunctions(ManagedFunctionContainer[] firstLastFunctions,
			ManagedFunctionDutyAssociation<?>[] functionDutyAssociations, ManagedFunctionContainer parallelOwner,
			ManagedFunctionContainer managedFunctionContainer,
			ManagedFunctionMetaData<?, ?> administeringFunctionMetaData) {

		// Load the duty functions
		for (int i = 0; i < functionDutyAssociations.length; i++) {
			ManagedFunctionDutyAssociation functionDutyAssociation = functionDutyAssociations[i];

			// Obtain the administrator container
			AdministratorContainer<?> adminContainer;
			AdministratorIndex adminIndex = functionDutyAssociation.getAdministratorIndex();
			int scopeIndex = adminIndex.getIndexOfAdministratorWithinScope();
			switch (adminIndex.getAdministratorScope()) {
			case WORK:
				adminContainer = managedFunctionContainer.getAdministratorContainer(scopeIndex);
				break;

			case THREAD:
				adminContainer = this.threadState.getAdministratorContainer(scopeIndex);
				break;

			case PROCESS:
				adminContainer = this.threadState.getProcessState().getAdministratorContainer(scopeIndex);
				break;

			default:
				throw new IllegalStateException("Unknown administrator scope " + adminIndex.getAdministratorScope());
			}

			// Create and register the active duty function
			ManagedFunctionContainer dutyFunction = adminContainer.administerManagedObjects(functionDutyAssociation,
					managedFunctionContainer, administeringFunctionMetaData);
			this.activeFunctions.addEntry(dutyFunction);

			// Load the duty function
			this.loadFunction(firstLastFunctions, dutyFunction);
		}
	}

	/**
	 * Loads the {@link ManagedFunctionContainer} to the listing of
	 * {@link ManagedFunctionContainer} instances.
	 * 
	 * @param firstLastJobs
	 *            Array containing two elements, first and last
	 *            {@link ManagedFunctionContainer} instances.
	 * @param nextFunction
	 *            Next {@link ManagedFunctionContainer}.
	 */
	private void loadFunction(ManagedFunctionContainer[] firstLastJobs, ManagedFunctionContainer nextFunction) {
		if (firstLastJobs[0] == null) {
			// First function
			firstLastJobs[0] = nextFunction;
			firstLastJobs[1] = nextFunction;
		} else {
			// Another function (append for sequential execution)
			firstLastJobs[1].setNextManagedFunctionContainer(nextFunction);
			firstLastJobs[1] = nextFunction;
		}
	}

	@Override
	public FunctionState handleEscalation(Throwable escalation) {

		// Clean up the functions of this flow
		FunctionState cleanUpFunctions = null;
		FunctionState flowFunctions = this.activeFunctions.purgeEntries();
		while (flowFunctions != null) {
			cleanUpFunctions = Promise.then(cleanUpFunctions, flowFunctions.cancel(escalation));
			flowFunctions = flowFunctions.getNext();
		}

		// Attempt handling by flow completion
		EscalationFlow escalationFlow = (this.completion != null)
				? this.completion.getFlowEscalationProcedure().getEscalation(escalation) : null;
		if (escalationFlow != null) {
			// Handle by flow escalation within new flow
			Flow flow = this.threadState.createFlow(null); // escalate to thread
			return Promise.then(cleanUpFunctions,
					flow.createManagedFunction(escalationFlow.getManagedFunctionMetaData(), null, escalation,
							GovernanceDeactivationStrategy.DISREGARD));

		} else {
			// No flow escalation, so handle by thread state
			return Promise.then(cleanUpFunctions, this.threadState.handleEscalation(escalation));
		}
	}

	@Override
	public FunctionState managedFunctionComplete(FunctionState function) {

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