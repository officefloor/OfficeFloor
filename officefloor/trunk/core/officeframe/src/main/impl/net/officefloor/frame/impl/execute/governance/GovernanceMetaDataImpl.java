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

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.internal.structure.ActiveGovernanceManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.governance.source.GovernanceSource;

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
	 * {@link GovernanceSource}.
	 */
	private final GovernanceSource<I, F> governanceSource;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceSource
	 *            {@link GovernanceSource}.
	 */
	public GovernanceMetaDataImpl(String governanceName,
			GovernanceSource<I, F> governanceSource) {
		this.governanceName = governanceName;
		this.governanceSource = governanceSource;
	}

	/*
	 * ================== GovernanceMetaData ==========================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public GovernanceContainer<I> createGovernanceContainer(Object processLock) {
		return new GovernanceContainerImpl<I, F>(this, processLock);
	}

	@Override
	public Governance<I, F> createGovernance() throws Throwable {
		return this.governanceSource.createGovernance();
	}

	@Override
	public ActiveGovernanceManager createActiveGovernance(
			GovernanceContainer<I> governanceContainer,
			Governance<I, F> governance, I extensionInterface,
			ManagedObjectContainer managedobjectContainer) {
		return new ActiveGovernanceImpl<I, F>(governanceContainer, governance,
				extensionInterface, managedobjectContainer);
	}

	@Override
	public FlowMetaData<?> getActivateFlowMetaData() {
		// TODO implement GovernanceMetaData<I,F>.getActivateFlowMetaData
		throw new UnsupportedOperationException(
				"TODO implement GovernanceMetaData<I,F>.getActivateFlowMetaData");
	}

	@Override
	public FlowMetaData<?> getEnforceFlowMetaData() {
		// TODO implement GovernanceMetaData<I,F>.getEnforceFlowMetaData
		throw new UnsupportedOperationException(
				"TODO implement GovernanceMetaData<I,F>.getEnforceFlowMetaData");
	}

	@Override
	public FlowMetaData<?> getDisregardFlowMetaData() {
		// TODO implement GovernanceMetaData<I,F>.getDisregardFlowMetaData
		throw new UnsupportedOperationException(
				"TODO implement GovernanceMetaData<I,F>.getDisregardFlowMetaData");
	}

	@Override
	public GovernanceContext<F> createGovernanceContext(
			TaskContext<?, ?, F> taskContext) {
		// TODO implement GovernanceMetaData<I,F>.createGovernanceContext
		throw new UnsupportedOperationException(
				"TODO implement GovernanceMetaData<I,F>.createGovernanceContext");
	}

}