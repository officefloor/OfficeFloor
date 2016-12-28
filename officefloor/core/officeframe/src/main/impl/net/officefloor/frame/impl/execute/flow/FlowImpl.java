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
package net.officefloor.frame.impl.execute.flow;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.impl.execute.administrator.AdministratorContainerImpl;
import net.officefloor.frame.impl.execute.function.ManagedFunctionContainerImpl;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionLogicImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.internal.structure.Administration;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionDutyAssociation;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;
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
		FunctionState function = new FunctionLogicFunctionState(logic);
		this.activeFunctions.addEntry(function);
		return function;
	}

	@Override
	public <O extends Enum<O>, F extends Enum<F>> ManagedFunctionContainer createManagedFunction(Object parameter,
			ManagedFunctionMetaData<O, F> managedFunctionMetaData, boolean isEnforceGovernance,
			ManagedFunctionContainer parallelFunctionOwner) {

		// Create the managed object containers for this managed function
		ManagedObjectMetaData<?>[] moMetaData = managedFunctionMetaData.getManagedObjectMetaData();
		ManagedObjectContainer[] managedObjects = new ManagedObjectContainer[moMetaData.length];
		for (int i = 0; i < moMetaData.length; i++) {
			managedObjects[i] = new ManagedObjectContainerImpl(moMetaData[i], this.threadState);
		}

		// Obtain the administration meta-data to determine
		ManagedFunctionDutyAssociation<?>[] preFunctionDuties = managedFunctionMetaData.getPreAdministrationMetaData();
		ManagedFunctionDutyAssociation<?>[] postFunctionDuties = managedFunctionMetaData
				.getPostAdministrationMetaData();

		// Determine if administration duties
		if ((preFunctionDuties.length == 0) && (postFunctionDuties.length == 0)) {
			// Just managed function, so load only it
			return this.loadManagedFunction(parameter, managedFunctionMetaData, managedObjects, isEnforceGovernance,
					parallelFunctionOwner, true);
		}

		// Create the administrator containers for this managed function
		AdministratorMetaData<?, ?>[] adminMetaData = managedFunctionMetaData.getAdministratorMetaData();
		AdministratorContainer<?>[] administrators = new AdministratorContainer<?>[adminMetaData.length];
		for (int i = 0; i < adminMetaData.length; i++) {
			administrators[i] = new AdministratorContainerImpl<>(adminMetaData[i]);
		}

		// First and last function (to add administration duties)
		ManagedFunctionContainer[] firstLastFunctions = new ManagedFunctionContainer[2];

		// Load the pre-function administrator duties
		this.loadDuties(firstLastFunctions, preFunctionDuties, administrators, managedObjects, managedFunctionMetaData,
				isEnforceGovernance, parallelFunctionOwner, false);

		// Determine which is responsible for unloading
		boolean isFunctionUnload = (postFunctionDuties.length == 0);

		// Load the managed function
		ManagedFunctionContainer managedFunctionContainer = this.loadManagedFunction(parameter, managedFunctionMetaData,
				managedObjects, isEnforceGovernance, parallelFunctionOwner, isFunctionUnload);
		this.loadFunction(firstLastFunctions, managedFunctionContainer);

		// Load the post-function administrator duties
		this.loadDuties(firstLastFunctions, postFunctionDuties, administrators, managedObjects, managedFunctionMetaData,
				isEnforceGovernance, parallelFunctionOwner, true);

		// Return the starting function
		return firstLastFunctions[0];
	}

	@Override
	public <F extends Enum<F>> FunctionState createGovernanceFunction(GovernanceActivity<F> governanceActivity,
			GovernanceMetaData<?, F> governanceMetaData) {

		// Create and register the governance function
		ManagedFunctionLogic governanceLogic = governanceMetaData.createGovernanceFunctionLogic(governanceActivity);
		ManagedFunctionContainer governanceFunctionContainer = new ManagedFunctionContainerImpl<GovernanceMetaData<?, F>>(
				null, governanceLogic, null, null, null, true, governanceMetaData, null, this, true);
		this.activeFunctions.addEntry(governanceFunctionContainer);

		// Return the governance function
		return governanceFunctionContainer;
	}

	/**
	 * Loads the {@link ManagedFunction} to this {@link Flow}.
	 * 
	 * @param parameter
	 *            Parameter for the {@link ManagedFunction}.
	 * @param managedFunctionMetaData
	 *            {@link ManagedFunctionMetaData} for the
	 *            {@link ManagedFunction}.
	 * @param functionBoundManagedObjects
	 *            {@link ManagedFunction} bound {@link ManagedObjectContainer}
	 *            instances.
	 * @param isEnforceGovernance
	 *            Whether to enforce {@link Governance}.
	 * @param parallelOwner
	 *            Parallel {@link ManagedFunctionContainer} owner.
	 * @param isUnloadManagedObjects
	 *            Whether the last {@link Duty} is to unload the
	 *            {@link ManagedObject} instances for the
	 *            {@link ManagedFunction}.
	 * @return {@link ManagedFunctionContainer} for the {@link ManagedFunction}.
	 */
	private <O extends Enum<O>, F extends Enum<F>> ManagedFunctionContainer loadManagedFunction(Object parameter,
			ManagedFunctionMetaData<O, F> managedFunctionMetaData, ManagedObjectContainer[] functionBoundManagedObjects,
			boolean isEnforceGovernance, ManagedFunctionContainer parallelOwner, boolean isUnloadManagedObjects) {
		ManagedFunctionLogic managedFunctionLogic = new ManagedFunctionLogicImpl<>(managedFunctionMetaData, parameter,
				this.threadState.getProcessState());
		ManagedFunctionContainer managedFunctionContainer = new ManagedFunctionContainerImpl<ManagedFunctionMetaData<?, ?>>(
				null, managedFunctionLogic, functionBoundManagedObjects,
				managedFunctionMetaData.getRequiredManagedObjects(), managedFunctionMetaData.getRequiredGovernance(),
				isEnforceGovernance, managedFunctionMetaData, parallelOwner, this, isUnloadManagedObjects);
		this.activeFunctions.addEntry(managedFunctionContainer);
		return managedFunctionContainer;
	}

	/**
	 * Loads the {@link Duty} to this {@link Flow}.
	 * 
	 * @param firstLastFunctions
	 *            Array of first and last {@link ManagedFunctionContainer}
	 *            instances.
	 * @param functionDutyAssociations
	 *            {@link ManagedFunctionDutyAssociation} instances.
	 * @param functionBoundAdministrators
	 *            {@link ManagedFunction} bound {@link AdministratorContainer}
	 *            instances.
	 * @param functionBoundManagedObjects
	 *            {@link ManagedFunction} bound {@link ManagedObjectContainer}
	 *            instances.
	 * @param administeringFunctionMetaData
	 *            {@link ManagedFunctionMetaData} of the {@link ManagedFunction}
	 *            being administered.
	 * @param isEnforceGovernance
	 *            Whether to enforce {@link Governance}.
	 * @param parallelOwner
	 *            Parallel {@link ManagedFunctionContainer} owner.
	 * @param isUnloadManagedObjects
	 *            Whether the last {@link Duty} is to unload the
	 *            {@link ManagedObject} instances for the
	 *            {@link ManagedFunction}.
	 */
	private void loadDuties(ManagedFunctionContainer[] firstLastFunctions,
			ManagedFunctionDutyAssociation<?>[] functionDutyAssociations,
			AdministratorContainer<?>[] functionBoundAdministrators,
			ManagedObjectContainer[] functionBoundManagedObjects,
			ManagedFunctionMetaData<?, ?> administeringFunctionMetaData, boolean isEnforceGovernance,
			ManagedFunctionContainer parallelOwner, boolean isUnloadManagedObjects) {

		// Load the duty functions
		for (int i = 0; i < functionDutyAssociations.length; i++) {
			@SuppressWarnings("rawtypes")
			ManagedFunctionDutyAssociation functionDutyAssociation = functionDutyAssociations[i];

			// Obtain the administrator container
			AdministratorContainer<?> adminContainer;
			AdministratorIndex adminIndex = functionDutyAssociation.getAdministratorIndex();
			int scopeIndex = adminIndex.getIndexOfAdministratorWithinScope();
			switch (adminIndex.getAdministratorScope()) {
			case FUNCTION:
				adminContainer = functionBoundAdministrators[scopeIndex];
				break;

			case THREAD:
				adminContainer = this.threadState.getAdministratorContainer(scopeIndex);
				break;

			default:
				throw new IllegalStateException("Unknown administrator scope " + adminIndex.getAdministratorScope());
			}

			// Obtain the administration
			@SuppressWarnings("unchecked")
			Administration administration = adminContainer.administerManagedObjects(functionDutyAssociation,
					functionBoundManagedObjects, this.threadState);

			// Determine if unload managed objects (last administration)
			boolean isUnloadResponsible = isUnloadManagedObjects && (i == (functionDutyAssociations.length - 1));

			// Create the managed function container for administration
			ManagedFunctionContainer dutyFunction = new ManagedFunctionContainerImpl<>(administration.getSetup(),
					administration.getDutyLogic(), functionBoundManagedObjects,
					administeringFunctionMetaData.getRequiredManagedObjects(),
					administeringFunctionMetaData.getRequiredGovernance(), isEnforceGovernance,
					administration.getAdministratorMetaData(), parallelOwner, this, isUnloadResponsible);

			// Register the active duty function
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
	public FunctionState cancel(Throwable escalation) {

		// Clean up the functions of this flow
		FunctionState cleanUpFunctions = null;
		FunctionState flowFunctions = this.activeFunctions.purgeEntries();
		while (flowFunctions != null) {
			cleanUpFunctions = Promise.then(cleanUpFunctions, flowFunctions.cancel(escalation));
			flowFunctions = flowFunctions.getNext();
		}

		// Return clean up of the flow
		return cleanUpFunctions;
	}

	@Override
	public FunctionState handleEscalation(Throwable escalation) {

		// Cancel this flow
		FunctionState cleanUpFunctions = this.cancel(escalation);

		// Attempt handling by flow completion
		if (this.completion != null) {
			// Handle by flow completion
			return Promise.then(cleanUpFunctions, this.completion.complete(escalation));

		}

		// No flow completion, so handle by thread state
		return Promise.then(cleanUpFunctions, this.threadState.handleEscalation(escalation));
	}

	@Override
	public FunctionState managedFunctionComplete(FunctionState function) {

		// Remove function from active function listing
		if (this.activeFunctions.removeEntry(function)) {

			// Determine if flow completion
			FunctionState flowCompletion = null;
			if (this.completion != null) {
				flowCompletion = this.completion.complete(null);
			}

			// Last active function so flow is now complete
			return Promise.then(flowCompletion, this.threadState.flowComplete(this));
		}

		// Flow still active
		return null;
	}

	@Override
	public ThreadState getThreadState() {
		return this.threadState;
	}

	/**
	 * {@link FunctionState} to execute a {@link FunctionLogic} within this
	 * {@link Flow}.
	 */
	private class FunctionLogicFunctionState extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements FunctionState {

		/**
		 * {@link FunctionLogic}.
		 */
		private final FunctionLogic functionLogic;

		/**
		 * Instantiate.
		 * 
		 * @param functionLogic
		 *            {@link FunctionLogic}.
		 */
		public FunctionLogicFunctionState(FunctionLogic functionLogic) {
			this.functionLogic = functionLogic;
		}

		/*
		 * ==================== LinkedListSetEntry ===================
		 */

		@Override
		public Flow getLinkedListSetOwner() {
			return FlowImpl.this;
		}

		/*
		 * ====================== FunctionState ======================
		 */

		@Override
		public ThreadState getThreadState() {
			return FlowImpl.this.getThreadState();
		}

		@Override
		public boolean isRequireThreadStateSafety() {
			return this.functionLogic.isRequireThreadStateSafety();
		}

		@Override
		public FunctionState execute() throws Throwable {
			return this.functionLogic.execute(FlowImpl.this);
		}

		@Override
		public FunctionState cancel(Throwable cause) {
			return FlowImpl.this.cancel(cause);
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation) {
			return FlowImpl.this.handleEscalation(escalation);
		}
	}

}