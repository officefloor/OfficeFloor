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

import java.lang.reflect.Array;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.execute.administration.AdministrationFunctionLogic;
import net.officefloor.frame.impl.execute.function.LinkedListSetPromise;
import net.officefloor.frame.impl.execute.function.ManagedFunctionBoundManagedObjects;
import net.officefloor.frame.impl.execute.function.ManagedFunctionContainerImpl;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionContext;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractor;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;

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

		// Obtain the administration meta-data to determine
		AdministrationMetaData<?, ?, ?>[] preAdministration = managedFunctionMetaData.getPreAdministrationMetaData();
		AdministrationMetaData<?, ?, ?>[] postAdministration = managedFunctionMetaData.getPostAdministrationMetaData();

		// Post administration unloads managed objects, otherwise function
		boolean isFunctionUnload = (postAdministration.length == 0);

		// Load the managed function
		ManagedFunctionBoundManagedObjects boundManagedObjects = new ManagedFunctionBoundManagedObjects(parameter,
				managedFunctionMetaData, isEnforceGovernance, parallelFunctionOwner, isFunctionUnload, this);
		ManagedFunctionContainer managedFunctionContainer = boundManagedObjects.managedFunctionContainer;
		this.activeFunctions.addEntry(managedFunctionContainer);

		// Load the pre-function administration (as parallel functions)
		for (int i = 0; i < preAdministration.length; i++) {
			AdministrationMetaData<?, ?, ?> administrationMetaData = preAdministration[i];

			// Create the administration function container
			ManagedFunctionContainer adminFunction = this.createAdministrationFunction(administrationMetaData,
					managedFunctionMetaData, isEnforceGovernance, parallelFunctionOwner, boundManagedObjects, false);

			// Push out previous administration functions to do this last
			managedFunctionContainer.setParallelManagedFunctionContainer(adminFunction);
		}

		// Load the post-function administration (as next functions)
		ManagedFunctionContainer lastAdministration = null;
		for (int i = 0; i < postAdministration.length; i++) {
			AdministrationMetaData<?, ?, ?> administrationMetaData = postAdministration[i];

			// Determine if unload managed objects (last administration)
			boolean isUnloadResponsible = (i == (postAdministration.length - 1));

			// Create the administration function container
			ManagedFunctionContainer adminFunction = this.createAdministrationFunction(administrationMetaData,
					managedFunctionMetaData, isEnforceGovernance, parallelFunctionOwner, boundManagedObjects,
					isUnloadResponsible);

			// Load the post-administration function
			if (i == 0) {
				// Load administration as first next function
				managedFunctionContainer.setNextManagedFunctionContainer(adminFunction);
				lastAdministration = adminFunction;
			} else {
				// Load subsequent administration
				lastAdministration.setNextManagedFunctionContainer(adminFunction);
				lastAdministration = adminFunction;
			}
		}

		// Return the function
		return managedFunctionContainer;
	}

	@Override
	public <F extends Enum<F>> ManagedFunctionContainer createGovernanceFunction(
			GovernanceActivity<F> governanceActivity, GovernanceMetaData<?, F> governanceMetaData) {

		// Create and register the governance function
		ManagedFunctionLogic governanceLogic = governanceMetaData.createGovernanceFunctionLogic(governanceActivity);
		ManagedFunctionContainer governanceFunctionContainer = new ManagedFunctionContainerImpl<GovernanceMetaData<?, F>>(
				null, governanceLogic, null, null, null, true, governanceMetaData, null, this, false);
		this.activeFunctions.addEntry(governanceFunctionContainer);

		// Return the governance function
		return governanceFunctionContainer;
	}

	/**
	 * Creates the {@link ManagedFunctionContainer} for the
	 * {@link Administration}.
	 * 
	 * @param administrationMetaData
	 *            {@link AdministrationMetaData}.
	 * @param administeringFunctionMetaData
	 *            {@link ManagedFunctionMetaData} of the {@link ManagedFunction}
	 *            being administered.
	 * @param isEnforceGovernance
	 *            Whether to enforce {@link Governance}.
	 * @param functionBoundManagedObjects
	 *            {@link ManagedFunction} bound {@link ManagedObjectContainer}
	 *            instances.
	 * @param parallelOwner
	 *            Parallel {@link ManagedFunctionContainer} owner.
	 * @param isUnloadManagedObjects
	 *            Whether the {@link Administration} is to unload the
	 *            {@link ManagedObject} instances for the
	 *            {@link ManagedFunction}.
	 * @return {@link AdministrationFunctionLogic}.
	 */
	private <E> ManagedFunctionContainer createAdministrationFunction(
			AdministrationMetaData<E, ?, ?> administrationMetaData,
			ManagedFunctionMetaData<?, ?> administeringFunctionMetaData, boolean isEnforceGovernance,
			ManagedFunctionContainer parallelOwner, ManagedFunctionBoundManagedObjects functionBoundManagedObjects,
			boolean isUnloadResponsible) {

		// Obtain the responsible team (ensure all done on same thread)
		// (this ensures safe to add to extensions list in setup)
		final TeamManagement responsibleTeam = administrationMetaData.getResponsibleTeam();

		// Obtain the extension meta-data
		ManagedObjectExtensionMetaData<E>[] eiMetaDatas = administrationMetaData.getManagedObjectExtensionMetaData();

		// Create the array of extensions
		Class<E> extensionInterface = administrationMetaData.getExtensionInterface();
		@SuppressWarnings("unchecked")
		E[] extensions = (E[]) Array.newInstance(extensionInterface, eiMetaDatas.length);

		// Obtain the extensions to be managed
		FunctionState allExtractions = null;
		for (int e = 0; e < eiMetaDatas.length; e++) {
			ManagedObjectExtensionMetaData<E> eiMetaData = eiMetaDatas[e];

			// Obtain the index of managed object to administer
			ManagedObjectIndex moIndex = eiMetaData.getManagedObjectIndex();

			// Obtain the managed object container
			ManagedObjectContainer moContainer;
			int scopeIndex = moIndex.getIndexOfManagedObjectWithinScope();
			switch (moIndex.getManagedObjectScope()) {
			case FUNCTION:
				moContainer = functionBoundManagedObjects.managedObjects[scopeIndex];
				break;

			case THREAD:
				moContainer = threadState.getManagedObjectContainer(scopeIndex);
				break;

			case PROCESS:
				moContainer = threadState.getProcessState().getManagedObjectContainer(scopeIndex);
				break;

			default:
				throw new IllegalStateException("Unknown managed object scope " + moIndex.getManagedObjectScope());
			}

			// Extract the extension interface
			ManagedObjectExtensionExtractor<E> extractor = eiMetaData.getManagedObjectExtensionExtractor();
			FunctionState moExtraction = moContainer.extractExtension(extractor, extensions, e, responsibleTeam);
			allExtractions = Promise.then(allExtractions, moExtraction);
		}

		// Create the administration function logic
		AdministrationFunctionLogic<E, ?, ?> administrationLogic = new AdministrationFunctionLogic<>(
				administrationMetaData, extensions, this.threadState);

		// Create the managed function container for administration
		ManagedFunctionContainer adminFunction = new ManagedFunctionContainerImpl<>(allExtractions, administrationLogic,
				functionBoundManagedObjects, administeringFunctionMetaData.getRequiredManagedObjects(),
				administeringFunctionMetaData.getRequiredGovernance(), isEnforceGovernance, administrationMetaData,
				parallelOwner, this, isUnloadResponsible);

		// Register the administration function
		this.activeFunctions.addEntry(adminFunction);

		// Return the managed function container
		return adminFunction;
	}

	@Override
	public FunctionState cancel() {
		return LinkedListSetPromise.all(this.activeFunctions, (function) -> function.cancel());
	}

	@Override
	public FunctionState handleEscalation(Throwable escalation, FunctionContext context) {

		// Cancel this flow
		FunctionState cleanUpFunctions = this.cancel();

		// Attempt handling by flow completion
		if (this.completion != null) {
			// Handle by flow completion
			return Promise.then(cleanUpFunctions, this.completion.complete(escalation));

		} else {
			// Last flow, so handle by thread state
			return Promise.then(cleanUpFunctions, this.threadState.handleEscalation(escalation));
		}
	}

	@Override
	public FunctionState managedFunctionComplete(FunctionState function, boolean isCancel) {

		// Remove function from active function listing
		if (this.activeFunctions.removeEntry(function)) {

			// Determine if flow completion
			FunctionState flowCompletion = null;
			if (!isCancel && (this.completion != null)) {
				flowCompletion = this.completion.complete(null);
			}

			// Last active function so flow is now complete
			return Promise.then(flowCompletion, this.threadState.flowComplete(this, isCancel));
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
		 * ========================= Object =========================
		 */

		@Override
		public String toString() {
			return this.functionLogic.toString();
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
		public TeamManagement getResponsibleTeam() {
			return this.functionLogic.getResponsibleTeam();
		}

		@Override
		public FunctionState execute(FunctionContext context) throws Throwable {
			FlowImpl.this.activeFunctions.removeEntry(this);
			return this.functionLogic.execute(FlowImpl.this);
		}

		@Override
		public FunctionState cancel() {
			return FlowImpl.this.cancel();
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation, FunctionContext context) {
			return FlowImpl.this.handleEscalation(escalation, context);
		}
	}

}