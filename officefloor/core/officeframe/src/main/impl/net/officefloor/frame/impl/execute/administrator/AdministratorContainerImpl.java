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
package net.officefloor.frame.impl.execute.administrator;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.Duty;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.DutyKey;
import net.officefloor.frame.api.administration.GovernanceManager;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.internal.structure.AdministrationDuty;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.AdministrationDuty;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionDutyAssociation;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicContext;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of an {@link AdministratorContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorContainerImpl<E extends Object, A extends Enum<A>, F extends Enum<F>, G extends Enum<G>>
		implements AdministratorContainer<A> {

	/**
	 * {@link AdministrationMetaData}.MetaData} for disregarding the
	 * {@link Governance}.
	 */
	private final AdministrationMetaData<E, A> metaData;

	/**
	 * {@link AdministrationDuty}.
	 */
	private Administration<E, A> administrator;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link AdministrationMetaData}.
	 */
	public AdministratorContainerImpl(AdministrationMetaData<E, A> metaData) {
		this.metaData = metaData;
	}

	/*
	 * ===================== AdministratorContainer =======================
	 */

	@Override
	public Administration administerManagedObjects(final ManagedFunctionDutyAssociation<A> functionDutyAssociation,
			final ManagedObjectContainer[] functionBoundManagedObjects, final ThreadState threadState) {

		// Obtain the extension interfaces to be managed
		final ExtensionInterfaceMetaData<?>[] eiMetaDatas = this.metaData.getExtensionInterfaceMetaData();

		// Obtain the responsible team (ensure all done on same thread)
		// (this ensures safe to add to extensions list in setup)
		final TeamManagement responsibleTeam = this.metaData.getResponsibleTeam();

		// Create the listing of extensions
		@SuppressWarnings("rawtypes")
		final List extensions = new ArrayList<>(eiMetaDatas.length);

		// Return the administration
		return new Administration() {

			@Override
			public FunctionState getSetup() {

				// Create the extractions of managed object extension
				FunctionState allExtractions = null;
				for (int i = 0; i < eiMetaDatas.length; i++) {
					ExtensionInterfaceMetaData<?> eiMetaData = eiMetaDatas[i];

					// Obtain the index of managed object to administer
					ManagedObjectIndex moIndex = eiMetaData.getManagedObjectIndex();

					// Obtain the managed object container
					ManagedObjectContainer moContainer;
					int scopeIndex = moIndex.getIndexOfManagedObjectWithinScope();
					switch (moIndex.getManagedObjectScope()) {
					case FUNCTION:
						moContainer = functionBoundManagedObjects[scopeIndex];
						break;

					case THREAD:
						moContainer = threadState.getManagedObjectContainer(scopeIndex);
						break;

					case PROCESS:
						moContainer = threadState.getProcessState().getManagedObjectContainer(scopeIndex);
						break;

					default:
						throw new IllegalStateException(
								"Unknown managed object scope " + moIndex.getManagedObjectScope());
					}

					// Extract the extension interface
					@SuppressWarnings("unchecked")
					FunctionState moExtraction = moContainer.extractExtensionInterface(
							eiMetaData.getExtensionInterfaceExtractor(), extensions, responsibleTeam);
					allExtractions = Promise.then(allExtractions, moExtraction);
				}

				// Return the extraction function
				return allExtractions;
			}

			@Override
			public ManagedFunctionLogic getDutyLogic() {
				@SuppressWarnings("unchecked")
				ManagedFunctionLogic logic = new DutyFunctionLogic(functionDutyAssociation, extensions, threadState);
				return logic;
			}

			@Override
			public AdministrationMetaData<?, ?> getAdministratorMetaData() {
				return AdministratorContainerImpl.this.metaData;
			}
		};
	}

	/**
	 * {@link AdministrationDuty} {@link ManagedFunctionLogic}.
	 */
	public class DutyFunctionLogic implements ManagedFunctionLogic {

		/**
		 * {@link ManagedFunctionDutyAssociation}.
		 */
		private final ManagedFunctionDutyAssociation<A> functionDutyAssociation;

		/**
		 * Extensions.
		 */
		private final List<E> extensions;

		/**
		 * {@link ThreadState}.
		 */
		private final ThreadState threadState;

		/**
		 * Initiate.
		 * 
		 * @param functionDutyAssociation
		 *            {@link ManagedFunctionDutyAssociation}.
		 * @param extensions
		 *            Extensions to administer.
		 * @param threadState
		 *            {@link ThreadState}.
		 */
		public DutyFunctionLogic(ManagedFunctionDutyAssociation<A> functionDutyAssociation, List<E> extensions,
				ThreadState threadState) {
			this.functionDutyAssociation = functionDutyAssociation;
			this.extensions = extensions;
			this.threadState = threadState;
		}

		/*
		 * ===================== ManagedFunctionLogic =====================
		 */

		@Override
		public Object execute(ManagedFunctionLogicContext context) throws Throwable {

			// Easy access to the container
			AdministratorContainerImpl<E, A, F, G> container = AdministratorContainerImpl.this;

			// Lazy create the administrator
			if (container.administrator == null) {
				container.administrator = container.metaData.getAdministratorSource().createAdministrator();
			}

			// Obtain the key identifying the duty
			DutyKey<A> key = this.functionDutyAssociation.getDutyKey();

			// Obtain the duty
			@SuppressWarnings("unchecked")
			AdministrationDuty<E, F, G> duty = (AdministrationDuty<E, F, G>) container.administrator.getDuty(key);

			// Obtain the duty meta-data
			DutyMetaData dutyMetaData = container.metaData.getDutyMetaData(key);

			// Execute the duty
			DutyContextToken token = new DutyContextToken(this.threadState, context, this.extensions, dutyMetaData);
			duty.doDuty(token);

			// Undertake the governance actions
			FunctionState governanceAction = null;
			for (FunctionState governanceFunction : token.actionedGovernances) {
				governanceAction = Promise.then(governanceAction, governanceFunction);
			}
			if (governanceAction != null) {
				final FunctionState finalGovernanceAction = governanceAction;
				context.next(new FunctionLogic() {
					@Override
					public FunctionState execute(Flow flow) throws Throwable {
						return finalGovernanceAction;
					}
				});
			}

			// No next function
			return null;
		}
	}

	/**
	 * <p>
	 * Token class given to the {@link AdministrationDuty}.
	 * <p>
	 * As application code will be provided a {@link AdministrationContext} this exposes
	 * just the necessary functionality and prevents access to internals of the
	 * framework.
	 */
	private class DutyContextToken implements AdministrationContext<E, F, G> {

		/**
		 * {@link ThreadState}.
		 */
		private final ThreadState threadState;

		/**
		 * {@link ManagedFunctionLogicContext}.
		 */
		private final ManagedFunctionLogicContext context;

		/**
		 * Extension interfaces.
		 */
		private final List<E> extensionInterfaces;

		/**
		 * {@link DutyMetaData}.
		 */
		private final DutyMetaData dutyMetaData;

		/**
		 * <p>
		 * {@link FunctionState} instances regarding {@link Governance}.
		 * <p>
		 * Typically {@link AdministrationDuty} will only undertake a single
		 * {@link GovernanceActivity}.
		 */
		private final List<FunctionState> actionedGovernances = new ArrayList<>(1);

		/**
		 * Initiate.
		 * 
		 * @param threadState
		 *            {@link ThreadState}.
		 * @param context
		 *            {@link ManagedFunctionLogicContext}.
		 * @param extensionInterfaces
		 *            Extension interfaces.
		 * @param dutyMetaData
		 *            {@link DutyMetaData}.
		 */
		public DutyContextToken(ThreadState threadState, ManagedFunctionLogicContext context,
				List<E> extensionInterfaces, DutyMetaData dutyMetaData) {
			this.threadState = threadState;
			this.context = context;
			this.extensionInterfaces = extensionInterfaces;
			this.dutyMetaData = dutyMetaData;
		}

		/*
		 * ==================== DutyContext ===================================
		 */

		@Override
		public List<E> getExtensionInterfaces() {
			return this.extensionInterfaces;
		}

		@Override
		public void doFlow(F key, Object parameter, FlowCallback callback) {
			// Delegate with index of key
			this.doFlow(key.ordinal(), parameter, callback);
		}

		@Override
		public void doFlow(int flowIndex, Object parameter, FlowCallback callback) {
			// Obtain the flow meta-data
			FlowMetaData flowMetaData = this.dutyMetaData.getFlow(flowIndex);

			// Do the flow
			this.context.doFlow(flowMetaData, parameter, callback);
		}

		@Override
		public GovernanceManager getGovernance(G key) {
			return this.getGovernance(key.ordinal());
		}

		@Override
		public GovernanceManager getGovernance(int governanceIndex) {

			// Obtain the process index for the governance
			int processIndex = this.dutyMetaData.translateGovernanceIndexToThreadIndex(governanceIndex);

			// Create Governance Manager to wrap Governance Container
			GovernanceManager manager = new GovernanceManagerImpl(processIndex);

			// Return the governance manager
			return manager;
		}

		/**
		 * {@link GovernanceManager} implementation.
		 */
		private class GovernanceManagerImpl implements GovernanceManager {

			/**
			 * Index of {@link Governance} within the {@link ThreadState}.
			 */
			private final int governanceIndex;

			/**
			 * Initiate.
			 * 
			 * @param governanceIndex
			 *            Index of {@link Governance} within the
			 *            {@link ThreadState}.
			 */
			public GovernanceManagerImpl(int governanceIndex) {
				this.governanceIndex = governanceIndex;
			}

			/*
			 * ===================== GovernanceManager =====================
			 */

			@Override
			public void activateGovernance() {
				FunctionState activate = this.getGovernanceContainer().activateGovernance();
				DutyContextToken.this.actionedGovernances.add(activate);
			}

			@Override
			public void enforceGovernance() {
				FunctionState enforce = this.getGovernanceContainer().enforceGovernance();
				DutyContextToken.this.actionedGovernances.add(enforce);
			}

			@Override
			public void disregardGovernance() {
				FunctionState disregard = this.getGovernanceContainer().disregardGovernance();
				DutyContextToken.this.actionedGovernances.add(disregard);
			}

			/**
			 * Obtains the {@link GovernanceContainer}.
			 * 
			 * @return {@link GovernanceContainer}.
			 */
			private GovernanceContainer<?> getGovernanceContainer() {

				// Obtain the governance container
				GovernanceContainer<?> container = DutyContextToken.this.threadState
						.getGovernanceContainer(this.governanceIndex);

				// Return the governance container
				return container;
			}
		}
	}

}