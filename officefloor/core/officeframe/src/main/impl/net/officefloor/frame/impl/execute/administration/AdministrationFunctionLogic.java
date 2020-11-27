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

package net.officefloor.frame.impl.execute.administration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.GovernanceManager;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicContext;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link ManagedFunctionLogic} for {@link Administration}.
 *
 * @author Daniel Sagenschneider
 */
public class AdministrationFunctionLogic<E, F extends Enum<F>, G extends Enum<G>> implements ManagedFunctionLogic {

	/**
	 * {@link AdministrationMetaData}.
	 */
	private final AdministrationMetaData<E, F, G> metaData;

	/**
	 * Extensions.
	 */
	private final E[] extensions;

	/**
	 * {@link Logger}.
	 */
	private final Logger logger;

	/**
	 * Initiate.
	 * 
	 * @param metaData   {@link AdministrationMetaData}.
	 * @param extensions Extensions to administer.
	 * @param logger     {@link Logger}.
	 */
	public AdministrationFunctionLogic(AdministrationMetaData<E, F, G> metaData, E[] extensions, Logger logger) {
		this.metaData = metaData;
		this.extensions = extensions;
		this.logger = logger;
	}

	/*
	 * ===================== ManagedFunctionLogic =====================
	 */

	@Override
	public void execute(ManagedFunctionLogicContext context, ThreadState threadState) throws Throwable {

		// Create the administration
		Administration<E, F, G> administration = this.metaData.getAdministrationFactory().createAdministration();

		// Execute the administration
		AdministrationContextToken token = new AdministrationContextToken(context, threadState);
		administration.administer(token);

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
	}

	/**
	 * <p>
	 * Token class given to the {@link AdministrationFunctionLogic}.
	 * <p>
	 * As application code will be provided a {@link AdministrationContext} this
	 * exposes just the necessary functionality and prevents access to internals of
	 * the framework.
	 */
	private class AdministrationContextToken implements AdministrationContext<E, F, G> {

		/**
		 * {@link ManagedFunctionLogicContext}.
		 */
		private final ManagedFunctionLogicContext context;

		/**
		 * {@link ThreadState}.
		 */
		private final ThreadState threadState;

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
		 * @param context     {@link ManagedFunctionLogicContext}.
		 * @param threadState {@link ThreadState}.
		 */
		private AdministrationContextToken(ManagedFunctionLogicContext context, ThreadState threadState) {
			this.context = context;
			this.threadState = threadState;
		}

		/*
		 * ==================== AdministrationContext ====================
		 */

		@Override
		public Logger getLogger() {
			return AdministrationFunctionLogic.this.logger;
		}

		@Override
		public E[] getExtensions() {
			return AdministrationFunctionLogic.this.extensions;
		}

		@Override
		public void doFlow(F key, Object parameter, FlowCallback callback) {
			// Delegate with index of key
			this.doFlow(key.ordinal(), parameter, callback);
		}

		@Override
		public void doFlow(int flowIndex, Object parameter, FlowCallback callback) {

			// Obtain the flow meta-data
			FlowMetaData flowMetaData = AdministrationFunctionLogic.this.metaData.getFlow(flowIndex);

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
			int processIndex = AdministrationFunctionLogic.this.metaData
					.translateGovernanceIndexToThreadIndex(governanceIndex);

			// Create Governance Manager to wrap Governance Container
			GovernanceManager manager = new GovernanceManagerImpl(processIndex);

			// Return the governance manager
			return manager;
		}

		@Override
		public AsynchronousFlow createAsynchronousFlow() {
			return this.context.createAsynchronousFlow();
		}

		@Override
		public Executor getExecutor() {
			return this.threadState.getProcessState().getExecutor();
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
			 * @param governanceIndex Index of {@link Governance} within the
			 *                        {@link ThreadState}.
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
				AdministrationContextToken.this.actionedGovernances.add(activate);
			}

			@Override
			public void enforceGovernance() {
				FunctionState enforce = this.getGovernanceContainer().enforceGovernance();
				AdministrationContextToken.this.actionedGovernances.add(enforce);
			}

			@Override
			public void disregardGovernance() {
				FunctionState disregard = this.getGovernanceContainer().disregardGovernance();
				AdministrationContextToken.this.actionedGovernances.add(disregard);
			}

			/**
			 * Obtains the {@link GovernanceContainer}.
			 * 
			 * @return {@link GovernanceContainer}.
			 */
			private GovernanceContainer<?> getGovernanceContainer() {

				// Obtain the governance container
				GovernanceContainer<?> container = AdministrationContextToken.this.threadState
						.getGovernanceContainer(this.governanceIndex);

				// Return the governance container
				return container;
			}
		}
	}

}
