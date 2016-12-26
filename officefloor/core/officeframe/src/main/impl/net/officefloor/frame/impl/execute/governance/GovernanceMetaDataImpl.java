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
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.function.ManagedFunctionContainerImpl;
import net.officefloor.frame.impl.execute.work.WorkContainerImpl;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicContext;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.team.Team;

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
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * {@link TeamManagement} of {@link Team} responsible for the
	 * {@link GovernanceActivity} instances.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * {@link FlowMetaData} instances.
	 */
	private FlowMetaData<?>[] flowMetaData;

	/**
	 * {@link EscalationProcedure} for the {@link GovernanceActivity} failures.
	 */
	private EscalationProcedure escalationProcedure;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceFactory
	 *            {@link GovernanceFactory}.
	 * @param responsibleTeam
	 *            {@link TeamManagement} of {@link Team} responsible for the
	 *            {@link GovernanceActivity} instances.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 */
	public GovernanceMetaDataImpl(String governanceName, GovernanceFactory<? super I, F> governanceFactory,
			TeamManagement responsibleTeam, FunctionLoop functionLoop) {
		this.governanceName = governanceName;
		this.governanceFactory = governanceFactory;
		this.responsibleTeam = responsibleTeam;
		this.functionLoop = functionLoop;
	}

	/**
	 * Loads the remaining state.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} instances.
	 * @param escalationProcedure
	 *            {@link EscalationProcedure}.
	 */
	public void loadRemainingState(FlowMetaData<?>[] flowMetaData, EscalationProcedure escalationProcedure) {
		this.flowMetaData = flowMetaData;
		this.escalationProcedure = escalationProcedure;
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
	public FunctionLoop getFunctionLoop() {
		return this.functionLoop;

	}

	@Override
	public ManagedFunctionMetaData<?, ?, ?> getNextManagedFunctionMetaData() {
		// Never a next task for governance activity
		return null;
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	/*
	 * ================== GovernanceMetaData ==========================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public GovernanceContainer<I> createGovernanceContainer(ThreadState threadState) {
		return new GovernanceContainerImpl<>(this, threadState);
	}

	@Override
	public ManagedFunctionContainerImpl createGovernanceFunction(GovernanceActivity<F> activity, Flow flow) {
		return new GovernanceFunction(flow, activity);
	}

	@Override
	public GovernanceFactory<? super I, F> getGovernanceFactory() {
		return this.governanceFactory;
	}

	@Override
	public FlowMetaData<?> getFlow(int flowIndex) {
		return this.flowMetaData[flowIndex];
	}

	/**
	 * {@link ManagedFunctionContainerImpl} to undertake the
	 * {@link GovernanceActivity}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 */
	private class GovernanceFunction extends ManagedFunctionContainerImpl<Work, GovernanceMetaData<I, F>>
			implements GovernanceContext<F> {

		/**
		 * {@link GovernanceActivity}.
		 */
		private final GovernanceActivity<F> activity;

		/**
		 * Instantiate.
		 * 
		 * @param flow
		 *            {@link Flow}.
		 * @param workContainer
		 *            {@link WorkContainer}.
		 * @param governanceMetaData
		 *            {@link GovernanceMetaData}.
		 * @param activity
		 *            {@link GovernanceActivity}.
		 */
		public GovernanceFunction(Flow flow, GovernanceActivity<F> activity) {
			super(flow, new WorkContainerImpl<>(null, new GovernanceWork(), flow.getThreadState()),
					GovernanceMetaDataImpl.this, null, null, null, GovernanceDeactivationStrategy.DISREGARD);
			this.activity = activity;
		}

		/*
		 * ================ ManagedFunctionContainer =======================
		 */

		@Override
		protected Object executeFunction(ManagedFunctionLogicContext context) throws Throwable {
			context.next(this.activity.doActivity(this));
			return null;
		}

		/*
		 * ================== GovernanceContext ==========================
		 */

		@Override
		public void doFlow(F key, Object parameter, FlowCallback callback) {
			this.doFlow(key.ordinal(), parameter, callback);
		}

		@Override
		public void doFlow(int flowIndex, Object parameter, FlowCallback callback) {

			// Obtain the flow meta-data
			FlowMetaData<?> flowMetaData = this.functionContainerMetaData.getFlow(flowIndex);

			// Undertake the flow
			this.doFlow(flowMetaData, parameter, callback);
		}
	}

}