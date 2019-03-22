/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicContext;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link GovernanceMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceMetaDataImpl<I, F extends Enum<F>> implements GovernanceMetaData<I, F> {

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<? super I, F> governanceFactory;

	/**
	 * {@link TeamManagement} of {@link Team} responsible for the
	 * {@link GovernanceActivity} instances.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * {@link AsynchronousFlow} timeout.
	 */
	private final long asynchronousFlowTimeout;

	/**
	 * {@link AssetManager} for the instigated {@link AsynchronousFlow} instances.
	 */
	private final AssetManager asynchronousFlowAssetManager;

	/**
	 * {@link OfficeMetaData}.
	 */
	private OfficeMetaData officeMetaData;

	/**
	 * {@link FlowMetaData} instances.
	 */
	private FlowMetaData[] flowMetaData;

	/**
	 * {@link EscalationProcedure} for the {@link GovernanceActivity} failures.
	 */
	private EscalationProcedure escalationProcedure;

	/**
	 * Initiate.
	 * 
	 * @param governanceName               Name of the {@link Governance}.
	 * @param governanceFactory            {@link GovernanceFactory}.
	 * @param responsibleTeam              {@link TeamManagement} of {@link Team}
	 *                                     responsible for the
	 *                                     {@link GovernanceActivity} instances.
	 * @param asynchronousFlowTimeout      {@link AsynchronousFlow} timeout.
	 * @param asynchronousFlowAssetManager {@link AssetManager} for
	 *                                     {@link AsynchronousFlow} instances.
	 */
	public GovernanceMetaDataImpl(String governanceName, GovernanceFactory<? super I, F> governanceFactory,
			TeamManagement responsibleTeam, long asynchronousFlowTimeout, AssetManager asynchronousFlowAssetManager) {
		this.governanceName = governanceName;
		this.governanceFactory = governanceFactory;
		this.responsibleTeam = responsibleTeam;
		this.asynchronousFlowTimeout = asynchronousFlowTimeout;
		this.asynchronousFlowAssetManager = asynchronousFlowAssetManager;
	}

	/**
	 * Loads the remaining state.
	 * 
	 * @param officeMetaData      {@link OfficeMetaData}.
	 * @param flowMetaData        {@link FlowMetaData} instances.
	 * @param escalationProcedure {@link EscalationProcedure}.
	 */
	public void loadOfficeMetaData(OfficeMetaData officeMetaData, FlowMetaData[] flowMetaData,
			EscalationProcedure escalationProcedure) {
		this.flowMetaData = flowMetaData;
		this.escalationProcedure = escalationProcedure;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * ============ ManagedFunctionContainerMetaData ==================
	 */

	@Override
	public String getFunctionName() {
		return this.governanceName;
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
	}

	@Override
	public long getAsynchronousFlowTimeout() {
		return this.asynchronousFlowTimeout;
	}

	@Override
	public AssetManager getAsynchronousFlowManager() {
		return this.asynchronousFlowAssetManager;
	}

	@Override
	public ManagedFunctionMetaData<?, ?> getNextManagedFunctionMetaData() {
		// Never a next function for governance activity
		return null;
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	@Override
	public OfficeMetaData getOfficeMetaData() {
		return this.officeMetaData;
	}

	/*
	 * ================== GovernanceMetaData ==========================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public GovernanceContainer<I> createGovernanceContainer(ThreadState threadState, int governanceIndex) {
		return new GovernanceContainerImpl<>(this, threadState, governanceIndex);
	}

	@Override
	public ManagedFunctionLogic createGovernanceFunctionLogic(GovernanceActivity<F> activity) {
		return new GovernanceFunctionLogic(activity);
	}

	@Override
	public GovernanceFactory<? super I, F> getGovernanceFactory() {
		return this.governanceFactory;
	}

	@Override
	public FlowMetaData getFlow(int flowIndex) {
		return this.flowMetaData[flowIndex];
	}

	/**
	 * {@link ManagedFunctionLogic} to undertake the {@link GovernanceActivity}.
	 */
	private class GovernanceFunctionLogic implements ManagedFunctionLogic {

		/**
		 * {@link GovernanceActivity}.
		 */
		private final GovernanceActivity<F> activity;

		/**
		 * Instantiate.
		 * 
		 * @param activity {@link GovernanceActivity}.
		 */
		public GovernanceFunctionLogic(GovernanceActivity<F> activity) {
			this.activity = activity;
		}

		/*
		 * ================ ManagedFunctionContainer =======================
		 */

		@Override
		public Object execute(final ManagedFunctionLogicContext context) throws Throwable {

			// Create the governance context
			GovernanceContext<F> governanceContext = new GovernanceContext<F>() {

				@Override
				public void doFlow(F key, Object parameter, FlowCallback callback) {
					this.doFlow(key.ordinal(), parameter, callback);
				}

				@Override
				public void doFlow(int flowIndex, Object parameter, FlowCallback callback) {

					// Obtain the flow meta-data
					FlowMetaData flowMetaData = GovernanceMetaDataImpl.this.flowMetaData[flowIndex];

					// Undertake the flow
					context.doFlow(flowMetaData, parameter, callback);
				}

				@Override
				public AsynchronousFlow createAsynchronousFlow() {
					return context.createAsynchronousFlow();
				}
			};

			// Execute the activity
			final FunctionState next = this.activity.doActivity(governanceContext);

			// Specify next function
			if (next != null) {
				context.next(new FunctionLogic() {
					@Override
					public FunctionState execute(Flow flow) throws Throwable {
						return next;
					}
				});
			}

			// No next function for governance
			return null;
		}
	}

}