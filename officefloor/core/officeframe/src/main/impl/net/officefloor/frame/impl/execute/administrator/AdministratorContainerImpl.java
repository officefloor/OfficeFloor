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

import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.function.AbstractManagedFunctionContainer;
import net.officefloor.frame.impl.execute.function.FailThreadStateJobNode;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionContainerContext;
import net.officefloor.frame.internal.structure.ManagedFunctionDutyAssociation;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.Promise;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;
import net.officefloor.frame.spi.administration.DutyKey;
import net.officefloor.frame.spi.administration.GovernanceManager;
import net.officefloor.frame.spi.governance.Governance;

/**
 * Implementation of an {@link AdministratorContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorContainerImpl<I extends Object, A extends Enum<A>, F extends Enum<F>, G extends Enum<G>>
		implements AdministratorContainer<I, A> {

	/**
	 * {@link AdministratorMetaData}.MetaData} for disregarding the
	 * {@link Governance}.
	 */
	private final AdministratorMetaData<I, A> metaData;

	/**
	 * Responsible {@link ThreadState}.
	 */
	private final ThreadState responsibleThreadState;

	/**
	 * {@link Administrator}.
	 */
	private Administrator<I, A> administrator;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link AdministratorMetaData}.
	 * @param responsibleThreadState
	 *            Responsible {@link ThreadState}.
	 */
	public AdministratorContainerImpl(AdministratorMetaData<I, A> metaData, ThreadState responsibleThreadState) {
		this.metaData = metaData;
		this.responsibleThreadState = responsibleThreadState;
	}

	/*
	 * ===================== AdministratorContainer =======================
	 */

	@Override
	public ThreadState getResponsibleThreadState() {
		return this.responsibleThreadState;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedFunctionContainer administerManagedObjects(final ManagedFunctionDutyAssociation<A> duty, Flow flow,
			ManagedFunctionMetaData<?, ?, ?> managedFunctionMetaData, final WorkContainer<?> workContainer) {

		// Obtain the extension interfaces to be managed
		final ExtensionInterfaceMetaData<?>[] eiMetaDatas = this.metaData.getExtensionInterfaceMetaData();

		// Create the listing of extensions
		List extensions = new ArrayList(eiMetaDatas.length);
		FunctionState extractFunction = null;
		for (int i = 0; i < eiMetaDatas.length; i++) {
			ExtensionInterfaceMetaData<?> eiMetaData = eiMetaDatas[i];

			// Obtain the index of managed object to administer
			ManagedObjectIndex moIndex = eiMetaData.getManagedObjectIndex();

			// Obtain the managed object container
			ManagedObjectContainer moContainer = workContainer.getManagedObjectContainer(moIndex);

			// Extract the extension interface
			extractFunction = Promise.then(extractFunction,
					moContainer.extractExtensionInterface(eiMetaData.getExtensionInterfaceExtractor(), extensions));
		}

		// Administer (after extracting extensions)
		FunctionState administer = Promise.then(extractFunction,
				new DutyFunction(flow, workContainer, duty, managedFunctionMetaData, extensions));
		return new AdministerFunction<>(flow, workContainer, duty, managedFunctionMetaData, administer);
	}

	/**
	 * {@link ManagedFunctionContainer} to administer the {@link FunctionState}
	 * instances for administration.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 */
	@Deprecated // FunctionState to always be returned
	private class AdministerFunction<W extends Work>
			extends AbstractManagedFunctionContainer<W, AdministratorMetaData<I, A>> {

		private final FunctionState administer;

		public AdministerFunction(Flow flow, WorkContainer<W> workContainer,
				ManagedFunctionDutyAssociation<A> functionDutyAssociation,
				ManagedFunctionMetaData<?, ?, ?> administeringFunctionMetaData, FunctionState administer) {
			super(flow, workContainer, AdministratorContainerImpl.this.metaData, null,
					administeringFunctionMetaData.getRequiredManagedObjects(), null, null);
			this.administer = administer;
		}

		@Override
		public FunctionState execute() {
			return administer;
		}

		@Override
		protected Object executeFunction(ManagedFunctionContainerContext context) throws Throwable {
			// Not invoked
			return null;
		}
	}

	/**
	 * {@link Duty} implementation for a {@link ManagedFunctionContainer}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 */
	public class DutyFunction<W extends Work> extends AbstractManagedFunctionContainer<W, AdministratorMetaData<I, A>> {

		/**
		 * {@link ManagedFunctionDutyAssociation}.
		 */
		private final ManagedFunctionDutyAssociation<A> functionDutyAssociation;

		/**
		 * {@link AdministratorContext}.
		 */
		private final AdministratorContext administratorContext = new AdministratorContextImpl();

		/**
		 * Extensions.
		 */
		private final List<I> extensions;

		/**
		 * Initiate.
		 * 
		 * @param flow
		 *            {@link Flow}.
		 * @param workContainer
		 *            {@link WorkContainer}.
		 * @param adminMetaData
		 *            {@link AdministratorMetaData}.
		 * @param functionDutyAssociation
		 *            {@link ManagedFunctionDutyAssociation}.
		 * @param administeringFunctionMetaData
		 *            {@link ManagedFunctionMetaData} of the
		 *            {@link ManagedFunction} being administered.
		 * @param extensions
		 *            Extensions to administer.
		 */
		public DutyFunction(Flow flow, WorkContainer<W> workContainer,
				ManagedFunctionDutyAssociation<A> functionDutyAssociation,
				ManagedFunctionMetaData<?, ?, ?> administeringFunctionMetaData, List<I> extensions) {
			super(flow, workContainer, AdministratorContainerImpl.this.metaData, null,
					administeringFunctionMetaData.getRequiredManagedObjects(), null, null);
			this.functionDutyAssociation = functionDutyAssociation;
			this.extensions = extensions;
		}

		/*
		 * ===================== ManagedFunctionContainer =====================
		 */

		@Override
		protected Object executeFunction(ManagedFunctionContainerContext context) throws Throwable {

			// Easy access to the container
			AdministratorContainerImpl<I, A, F, G> container = AdministratorContainerImpl.this;

			try {

				// Lazy create the administrator
				if (container.administrator == null) {
					container.administrator = container.metaData.getAdministratorSource().createAdministrator();
				}

				// Obtain the key identifying the duty
				DutyKey<A> key = this.functionDutyAssociation.getDutyKey();

				// Obtain the duty
				Duty<I, F, G> duty = (Duty<I, F, G>) container.administrator.getDuty(key);

				// Obtain the duty meta-data
				DutyMetaData dutyMetaData = container.metaData.getDutyMetaData(key);

				// Execute the duty
				DutyContextToken token = new DutyContextToken(this.administratorContext, this.extensions, dutyMetaData);
				duty.doDuty(token);

				// Undertake the governance actions
				FunctionState governanceAction = null;
				for (FunctionState governanceFunction : token.actionedGovernances) {
					governanceAction = Promise.then(governanceAction, governanceFunction);
				}
				return governanceAction;

			} catch (Throwable ex) {
				// Fail the thread state
				return new FailThreadStateJobNode(ex, container.responsibleThreadState);
			}
		}

		/**
		 * {@link AdministratorContext} implementations.
		 */
		private class AdministratorContextImpl implements AdministratorContext {

			/*
			 * ================= AdministratorContext =======================
			 */

			@Override
			public ThreadState getThreadState() {
				return DutyFunction.this.flow.getThreadState();
			}

			@Override
			public void doFlow(FlowMetaData<?> flowMetaData, Object parameter, FlowCallback callback) {
				DutyFunction.this.doFlow(flowMetaData, parameter, callback);
			}
		}
	}

	/**
	 * <p>
	 * Token class given to the {@link Duty}.
	 * <p>
	 * As application code will be provided a {@link DutyContext} this exposes
	 * just the necessary functionality and prevents access to internals of the
	 * framework.
	 */
	private class DutyContextToken implements DutyContext<I, F, G> {

		/**
		 * {@link AdministratorContext}.
		 */
		private final AdministratorContext adminContext;

		/**
		 * Extension interfaces.
		 */
		private final List<I> extensionInterfaces;

		/**
		 * {@link DutyMetaData}.
		 */
		private final DutyMetaData dutyMetaData;

		/**
		 * {@link FunctionState} instances regarding {@link Governance}.
		 */
		private final List<FunctionState> actionedGovernances = new ArrayList<>(1);

		/**
		 * Initiate.
		 * 
		 * @param adminContext
		 *            {@link AdministratorContext}.
		 * @param extensionInterfaces
		 *            Extension interfaces.
		 * @param dutyMetaData
		 *            {@link DutyMetaData}.
		 */
		public DutyContextToken(AdministratorContext adminContext, List<I> extensionInterfaces,
				DutyMetaData dutyMetaData) {
			this.adminContext = adminContext;
			this.extensionInterfaces = extensionInterfaces;
			this.dutyMetaData = dutyMetaData;
		}

		/*
		 * ==================== DutyContext ===================================
		 */

		@Override
		public List<I> getExtensionInterfaces() {
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
			FlowMetaData<?> flowMetaData = this.dutyMetaData.getFlow(flowIndex);

			// Do the flow
			this.adminContext.doFlow(flowMetaData, parameter, callback);
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
			GovernanceManager manager = new GovernanceManagerImpl(this.adminContext, processIndex);

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
			 * @param adminContext
			 *            {@link AdministratorContext}.
			 *            {@link GovernanceContainer}.
			 * @param governanceIndex
			 *            Index of {@link Governance} within the
			 *            {@link ThreadState}.
			 */
			public GovernanceManagerImpl(AdministratorContext adminContext, int governanceIndex) {
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
				GovernanceContainer<?> container = DutyContextToken.this.adminContext.getThreadState()
						.getGovernanceContainer(this.governanceIndex);

				// Return the governance container
				return container;
			}
		}
	}

}