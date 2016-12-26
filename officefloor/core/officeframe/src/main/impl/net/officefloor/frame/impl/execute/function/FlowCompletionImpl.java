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

import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionDutyAssociation;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * {@link FlowCompletion} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class FlowCompletionImpl<O extends Enum<O>, F extends Enum<F>>
		extends AbstractLinkedListSetEntry<FlowCompletion, ManagedFunctionContainer>
		implements FlowCompletion, EscalationProcedure {

	/**
	 * {@link FlowCallback}.
	 */
	private final FlowCallback callback;

	/**
	 * {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionLogicMetaData metaData;

	/**
	 * {@link ManagedFunctionContainer}.
	 */
	private final ManagedFunctionContainer managedFunctionContainer;

	/**
	 * Instantiate.
	 * 
	 * @param callback
	 *            {@link FlowCallback}.
	 * @param metaData
	 *            {@link ManagedFunctionMetaData}.
	 * @param managedFunctionContainer
	 *            {@link ManagedFunctionContainer}.
	 */
	public FlowCompletionImpl(FlowCallback callback, ManagedFunctionLogicMetaData metaData,
			ManagedFunctionContainer managedFunctionContainer) {
		this.callback = callback;
		this.metaData = metaData;
		this.managedFunctionContainer = managedFunctionContainer;
	}

	/*
	 * =============== LinkedListSetEntry ====================
	 */

	@Override
	public ManagedFunctionContainer getLinkedListSetOwner() {
		return this.managedFunctionContainer;
	}

	/*
	 * ================= FlowCompletion ======================
	 */

	@Override
	public FunctionState getFlowCompletionFunction() {
		// TODO implement FlowCompletion.getFlowCompletionFunction
		throw new UnsupportedOperationException("TODO implement FlowCompletion.getFlowCompletionFunction");

	}

	@Override
	public EscalationProcedure getFlowEscalationProcedure() {
		return this;
	}

	/*
	 * ================= EscalationProcedure ======================
	 */

	@Override
	public EscalationFlow getEscalation(Throwable cause) {
		return new FlowCallbackEscalationFlow(cause);
	}

	/**
	 * {@link EscalationFlow} to use {@link FlowCallback}.
	 */
	private class FlowCallbackEscalationFlow implements EscalationFlow, ManagedFunctionMetaData<O, F> {

		/**
		 * {@link Escalation}.
		 */
		private final Throwable escalation;

		/**
		 * Instantiate.
		 * 
		 * @param escalation
		 *            {@link Escalation}.
		 */
		public FlowCallbackEscalationFlow(Throwable escalation) {
			this.escalation = escalation;
		}

		/*
		 * ================= EscalationFlow =======================
		 */

		@Override
		public Class<? extends Throwable> getTypeOfCause() {
			return this.escalation.getClass();
		}

		@Override
		public ManagedFunctionMetaData<?, ?> getManagedFunctionMetaData() {
			return this;
		}

		@Override
		public Object getDifferentiator() {
			return FlowCompletionImpl.this.metaData.getDifferentiator();
		}

		@Override
		public ManagedObjectIndex[] getRequiredManagedObjects() {
			return FlowCompletionImpl.this.metaData.getRequiredManagedObjects();
		}

		@Override
		public boolean[] getRequiredGovernance() {
			return FlowCompletionImpl.this.metaData.getRequiredGovernance();
		}

		@Override
		public FlowMetaData getFlow(int flowIndex) {
			return FlowCompletionImpl.this.metaData.getFlow(flowIndex);
		}

		/*
		 * =============== ManagedFunctionMetaData =================
		 */

		@Override
		public String getFunctionName() {
			return "Callback for " + FlowCompletionImpl.this.metaData.getFunctionName();
		}

		@Override
		public ManagedFunctionMetaData<?, ?> getNextManagedFunctionMetaData() {
			return null; // no next function after escalation handling
		}

		@Override
		public ManagedFunctionFactory<O, F> getManagedFunctionFactory() {
			// TODO implement
			// FlowCallbackEscalationProcedure.FlowCallbackEscalationFlow.getManagedFunctionFactory
			throw new UnsupportedOperationException(
					"TODO implement FlowCallbackEscalationProcedure.FlowCallbackEscalationFlow.getManagedFunctionFactory");

		}

		@Override
		public ManagedFunctionDutyAssociation<?>[] getPreAdministrationMetaData() {
			return null;
		}

		@Override
		public ManagedFunctionDutyAssociation<?>[] getPostAdministrationMetaData() {
			return null;
		}

		@Override
		public Class<?> getParameterType() {
			return this.getTypeOfCause();
		}

		/*
		 * ======= ManagedFunctionMetaData delegate =================
		 */

		@Override
		public TeamManagement getResponsibleTeam() {
			return FlowCompletionImpl.this.metaData.getResponsibleTeam();
		}

		@Override
		public FunctionLoop getFunctionLoop() {
			return FlowCompletionImpl.this.metaData.getFunctionLoop();
		}

		@Override
		public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
			return FlowCompletionImpl.this.metaData.getManagedObjectMetaData();
		}

		@Override
		public AdministratorMetaData<?, ?>[] getAdministratorMetaData() {
			return FlowCompletionImpl.this.metaData.getAdministratorMetaData();
		}

		@Override
		public EscalationProcedure getEscalationProcedure() {
			return FlowCompletionImpl.this;
		}

		@Override
		public ManagedFunctionContainer createManagedFunctionContainer(Flow flow,
				ManagedFunctionContainer parallelFunctionOwner, Object parameter,
				GovernanceDeactivationStrategy governanceDeactivationStrategy) {
			return FlowCompletionImpl.this.metaData.createManagedFunctionContainer(flow, parallelFunctionOwner,
					parameter, governanceDeactivationStrategy);
		}
	}

}
