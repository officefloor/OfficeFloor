/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.internal.structure.ActiveGovernanceManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link GovernanceMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceMetaDataImpl<I, F extends Enum<F>> implements
		GovernanceMetaData<I, F> {

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<? super I, F> governanceFactory;

	/**
	 * {@link FlowMetaData} to activate {@link Governance}.
	 */
	private FlowMetaData<?> activateFlow;

	/**
	 * {@link FlowMetaData} for {@link Governance} of a {@link ManagedObject}.
	 */
	private FlowMetaData<?> governFlow;

	/**
	 * {@link FlowMetaData} to enforce {@link Governance}.
	 */
	private FlowMetaData<?> enforceFlow;

	/**
	 * {@link FlowMetaData} to disregard {@link Governance}.
	 */
	private FlowMetaData<?> disregardFlow;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceFactory
	 *            {@link GovernanceFactory}.
	 */
	public GovernanceMetaDataImpl(String governanceName,
			GovernanceFactory<? super I, F> governanceFactory) {
		this.governanceName = governanceName;
		this.governanceFactory = governanceFactory;
	}

	/**
	 * Loads the {@link FlowMetaData} for the {@link Governance} {@link Task}
	 * instances.
	 * 
	 * @param activateFlow
	 *            {@link FlowMetaData} to activate {@link Governance}.
	 * @param governFlow
	 *            {@link FlowMetaData} for {@link Governance} of a
	 *            {@link ManagedObject}.
	 * @param enforceFlow
	 *            {@link FlowMetaData} to enforce {@link Governance}.
	 * @param disregardFlow
	 *            {@link FlowMetaData} to disregard {@link Governance}.
	 */
	public void loadFlows(FlowMetaData<?> activateFlow,
			FlowMetaData<?> governFlow, FlowMetaData<?> enforceFlow,
			FlowMetaData<?> disregardFlow) {
		this.activateFlow = activateFlow;
		this.governFlow = governFlow;
		this.enforceFlow = enforceFlow;
		this.disregardFlow = disregardFlow;
	}

	/*
	 * ================== GovernanceMetaData ==========================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public GovernanceFactory<? super I, F> getGovernanceFactory() {
		return this.governanceFactory;
	}

	@Override
	public GovernanceContainer<I> createGovernanceContainer(
			ProcessState processState, int processRegisteredIndex) {
		return new GovernanceContainerImpl<I, F>(this, processState,
				processRegisteredIndex);
	}

	@Override
	public ActiveGovernanceManager createActiveGovernance(
			GovernanceContainer<I> governanceContainer,
			GovernanceControl<I, F> governanceControl, I extensionInterface,
			ManagedObjectContainer managedobjectContainer,
			int managedObjectContainerRegisteredIndex) {
		return new ActiveGovernanceImpl<I, F>(governanceContainer, this,
				governanceControl, extensionInterface, managedobjectContainer,
				managedObjectContainerRegisteredIndex);
	}

	@Override
	public FlowMetaData<?> getActivateFlowMetaData() {
		return this.activateFlow;
	}

	@Override
	public FlowMetaData<?> getGovernFlowMetaData() {
		return this.governFlow;
	}

	@Override
	public FlowMetaData<?> getEnforceFlowMetaData() {
		return this.enforceFlow;
	}

	@Override
	public FlowMetaData<?> getDisregardFlowMetaData() {
		return this.disregardFlow;
	}

	@Override
	public GovernanceContext<F> createGovernanceContext(
			TaskContext<?, ?, F> taskContext) {
		return new GovernanceContextImpl<F>(taskContext);
	}

}