/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.flow;

import java.lang.reflect.Array;
import java.util.logging.Logger;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.escalate.Escalation;
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
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionLogicImpl;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.BlockState;
import net.officefloor.frame.internal.structure.EscalationCompletion;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractor;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractorMetaData;
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
	private final FlowCompletion flowCompletion;

	/**
	 * {@link ThreadState} that this {@link Flow} is bound.
	 */
	private final ThreadState threadState;

	/**
	 * Possible {@link Escalation} from this {@link Flow}.
	 */
	private Throwable flowEscalation = null;

	/**
	 * {@link EscalationCompletion} for this {@link Flow}.
	 */
	private EscalationCompletion escalationCompletion;

	/**
	 * Indicates if this {@link Flow} has been completed.
	 */
	private boolean isFlowComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param flowCompletion       {@link FlowCompletion} of this {@link Flow}.
	 * @param escalationCompletion {@link EscalationCompletion} for this
	 *                             {@link Flow}.
	 * @param threadState          {@link ThreadState} containing this {@link Flow}.
	 */
	public FlowImpl(FlowCompletion flowCompletion, EscalationCompletion escalationCompletion, ThreadState threadState) {
		this.flowCompletion = flowCompletion;
		this.escalationCompletion = escalationCompletion;
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
			BlockState parallelFunctionOwner) {

		// Obtain the administration meta-data to determine
		ManagedFunctionAdministrationMetaData<?, ?, ?>[] preAdministration = managedFunctionMetaData
				.getPreAdministrationMetaData();
		ManagedFunctionAdministrationMetaData<?, ?, ?>[] postAdministration = managedFunctionMetaData
				.getPostAdministrationMetaData();

		// Post administration unloads managed objects, otherwise function
		boolean isFunctionUnload = (postAdministration.length == 0);

		// Obtain the required managed objects and governance
		ManagedObjectIndex[] requiredManagedObjects = managedFunctionMetaData.getRequiredManagedObjects();
		boolean[] requiredGovernance = managedFunctionMetaData.getRequiredGovernance();

		// Load the managed function
		ManagedFunctionLogic managedFunctionLogic = new ManagedFunctionLogicImpl<>(managedFunctionMetaData, parameter);
		ManagedFunctionBoundManagedObjects boundManagedObjects = new ManagedFunctionBoundManagedObjects(
				managedFunctionLogic, managedFunctionMetaData.getManagedObjectMetaData(), requiredManagedObjects,
				requiredGovernance, isEnforceGovernance, managedFunctionMetaData, parallelFunctionOwner, this,
				isFunctionUnload);
		ManagedFunctionContainer managedFunctionContainer = boundManagedObjects.managedFunctionContainer;
		this.activeFunctions.addEntry(managedFunctionContainer);

		// Load the pre-function administration (as parallel functions)
		for (int i = 0; i < preAdministration.length; i++) {
			ManagedFunctionAdministrationMetaData<?, ?, ?> administrationMetaData = preAdministration[i];

			// Create the administration function container
			ManagedFunctionContainer adminFunction = this.createAdministrationFunction(
					administrationMetaData.getAdministrationMetaData(), administrationMetaData.getLogger(),
					requiredManagedObjects, requiredGovernance, isEnforceGovernance, parallelFunctionOwner,
					boundManagedObjects, false);

			// Push out previous administration functions to do this last
			managedFunctionContainer.loadParallelBlock(adminFunction);
		}

		// Load the post-function administration (as next functions)
		ManagedFunctionContainer lastAdministration = null;
		for (int i = 0; i < postAdministration.length; i++) {
			ManagedFunctionAdministrationMetaData<?, ?, ?> administrationMetaData = postAdministration[i];

			// Determine if unload managed objects (last administration)
			boolean isUnloadResponsible = (i == (postAdministration.length - 1));

			// Create the administration function container
			ManagedFunctionContainer adminFunction = this.createAdministrationFunction(
					administrationMetaData.getAdministrationMetaData(), administrationMetaData.getLogger(),
					requiredManagedObjects, requiredGovernance, isEnforceGovernance, parallelFunctionOwner,
					boundManagedObjects, isUnloadResponsible);

			// Load the post-administration function
			if (i == 0) {
				// Load administration as first next function
				managedFunctionContainer.loadSequentialBlock(adminFunction);
				lastAdministration = adminFunction;
			} else {
				// Load subsequent administration
				lastAdministration.loadSequentialBlock(adminFunction);
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

	@Override
	public <E, F extends Enum<F>, G extends Enum<G>> ManagedFunctionContainer createAdministrationFunction(
			ManagedObjectAdministrationMetaData<E, F, G> adminMetaData,
			ManagedFunctionContainer parallelFunctionOwner) {
		return this.createAdministrationFunction(adminMetaData.getAdministrationMetaData(), adminMetaData.getLogger(),
				adminMetaData.getRequiredManagedObjects(), null, true, parallelFunctionOwner, null, false);
	}

	/**
	 * Creates the {@link ManagedFunctionContainer} for the {@link Administration}.
	 * 
	 * @param administrationMetaData      {@link AdministrationMetaData}.
	 * @param logger                      {@link Logger} for
	 *                                    {@link AdministrationContext}.
	 * @param requiredManagedObjects      {@link ManagedObjectIndex} instances to
	 *                                    the {@link ManagedObject} instances that
	 *                                    must be loaded before the
	 *                                    {@link ManagedFunction} may be executed.
	 * @param requiredGovernance          Identifies the required activation state
	 *                                    of the {@link Governance} for this
	 *                                    {@link ManagedFunction}.
	 * @param isEnforceGovernance         Whether to enforce {@link Governance}.
	 * @param functionBoundManagedObjects {@link ManagedFunction} bound
	 *                                    {@link ManagedObjectContainer} instances.
	 * @param parallelOwner               Parallel {@link BlockState} owner.
	 * @param isUnloadManagedObjects      Whether the {@link Administration} is to
	 *                                    unload the {@link ManagedObject} instances
	 *                                    for the {@link ManagedFunction}.
	 * @return {@link AdministrationFunctionLogic}.
	 */
	private <E> ManagedFunctionContainer createAdministrationFunction(
			AdministrationMetaData<E, ?, ?> administrationMetaData, Logger logger,
			ManagedObjectIndex[] requiredManagedObjects, boolean[] requiredGovernance, boolean isEnforceGovernance,
			BlockState parallelOwner, ManagedFunctionBoundManagedObjects functionBoundManagedObjects,
			boolean isUnloadResponsible) {

		// Obtain the responsible team (ensure all done on same thread)
		// (this ensures safe to add to extensions list in setup)
		final TeamManagement responsibleTeam = administrationMetaData.getResponsibleTeam();

		// Obtain the extension meta-data
		ManagedObjectExtensionExtractorMetaData<E>[] eiMetaDatas = administrationMetaData
				.getManagedObjectExtensionExtractorMetaData();

		// Create the array of extensions
		Class<E> extensionInterface = administrationMetaData.getExtensionInterface();
		@SuppressWarnings("unchecked")
		E[] extensions = (E[]) Array.newInstance(extensionInterface, eiMetaDatas.length);

		// Create the administration function logic
		// (extensions loaded all on same thread in setup functions)
		AdministrationFunctionLogic<E, ?, ?> administrationLogic = new AdministrationFunctionLogic<>(
				administrationMetaData, extensions, logger);

		// Obtain the extensions to be managed
		FunctionState allExtractions = null;
		for (int e = 0; e < eiMetaDatas.length; e++) {
			ManagedObjectExtensionExtractorMetaData<E> eiMetaData = eiMetaDatas[e];

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

		// Create the managed function container for administration
		ManagedFunctionContainer adminFunction = new ManagedFunctionContainerImpl<>(allExtractions, administrationLogic,
				functionBoundManagedObjects, requiredManagedObjects, requiredGovernance, isEnforceGovernance,
				administrationMetaData, parallelOwner, this, isUnloadResponsible);

		// Register the administration function
		this.activeFunctions.addEntry(adminFunction);

		// Return the managed function container
		return adminFunction;
	}

	@Override
	public FunctionState cancel() {

		// Undertake cancel
		FunctionState cancelFunctions = null;

		// Ensure escalation completion
		if (this.escalationCompletion != null) {
			cancelFunctions = Promise.then(cancelFunctions, this.escalationCompletion.escalationComplete());
		}

		// Completion functions
		cancelFunctions = Promise.then(cancelFunctions,
				LinkedListSetPromise.all(this.activeFunctions, (function) -> function.cancel()));

		// Return the cancel
		return cancelFunctions;
	}

	@Override
	public FunctionState managedFunctionComplete(FunctionState function, Throwable functionEscalation,
			EscalationCompletion escalationCompletion) {

		// Handle completion
		FunctionState completionFunctions = null;

		// Handle escalation
		if (functionEscalation != null) {

			// Cancel this flow as escalation
			completionFunctions = this.cancel();

			// Determine if already an escalation for this flow
			if (this.flowEscalation == null) {

				// Escalation for this flow
				this.flowEscalation = functionEscalation;
				this.escalationCompletion = escalationCompletion;

			} else {
				// Let thread state handle additional escalations
				completionFunctions = Promise.then(completionFunctions,
						this.threadState.handleEscalation(functionEscalation, escalationCompletion));
			}
		}

		// Determine if flow complete
		if (this.activeFunctions.removeEntry(function)) {

			// Notify of flow completion
			Throwable threadEscalation;
			EscalationCompletion threadEscalationCompletion;
			if (this.flowCompletion != null) {
				// Handle escalation by flow completion
				completionFunctions = Promise.then(completionFunctions,
						this.flowCompletion.flowComplete(this.flowEscalation));
				if (this.escalationCompletion != null) {
					completionFunctions = Promise.then(completionFunctions,
							this.escalationCompletion.escalationComplete());
				}
				threadEscalation = null;
				threadEscalationCompletion = null;

			} else {
				// Escalate to thread escalation
				threadEscalation = this.flowEscalation;
				threadEscalationCompletion = this.escalationCompletion;
			}

			// Include flow complete clean up
			completionFunctions = Promise.then(completionFunctions, new FlowOperation() {
				@Override
				public FunctionState execute(FunctionStateContext context) throws Throwable {

					// Easy access to flow
					FlowImpl flow = FlowImpl.this;

					// Do nothing if flow is complete
					if (flow.isFlowComplete) {
						return null;
					}

					// Flow is now considered complete
					flow.isFlowComplete = true;

					// Complete the flow
					return flow.threadState.flowComplete(flow, threadEscalation, threadEscalationCompletion);
				}
			});
		}

		// Provide clean up for managed function (and possibly flow)
		return completionFunctions;
	}

	@Override
	public ThreadState getThreadState() {
		return this.threadState;
	}

	/**
	 * Abstract {@link FunctionState} operation for the {@link Flow}.
	 */
	private abstract class FlowOperation extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements FunctionState {

		@Override
		public ThreadState getThreadState() {
			return FlowImpl.this.threadState;
		}
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
		 * @param functionLogic {@link FunctionLogic}.
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
		public FunctionState execute(FunctionStateContext context) throws Throwable {
			FlowImpl.this.activeFunctions.removeEntry(this);
			return this.functionLogic.execute(FlowImpl.this);
		}

		@Override
		public FunctionState cancel() {
			return FlowImpl.this.cancel();
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation, EscalationCompletion completion) {
			return FlowImpl.this.threadState.handleEscalation(escalation, completion);
		}
	}

}
