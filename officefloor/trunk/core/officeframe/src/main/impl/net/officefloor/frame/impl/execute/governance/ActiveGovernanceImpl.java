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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.ActiveGovernanceManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * {@link ActiveGovernance} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ActiveGovernanceImpl<I, F extends Enum<F>> extends
		AbstractSingleTask<ActiveGovernanceImpl<I, F>, None, None> implements
		ActiveGovernanceManager, ActiveGovernance, GovernanceContext<F> {

	/**
	 * {@link GovernanceContainer}.
	 */
	private final GovernanceContainer<I> governanceContainer;

	/**
	 * {@link Governance}.
	 */
	private final Governance<I, F> governance;

	/**
	 * Extension interface.
	 */
	private final I extensionInterface;

	/**
	 * {@link ManagedObjectContainer}.
	 */
	private final ManagedObjectContainer managedObject;

	/**
	 * Initiate.
	 * 
	 * @param governanceContainer
	 *            {@link GovernanceContainer}.
	 * @param governance
	 *            {@link Governance}.
	 * @param extensionInterface
	 *            Extension interface.
	 * @param managedObject
	 *            {@link ManagedObjectContainer}.
	 */
	public ActiveGovernanceImpl(GovernanceContainer<I> governanceContainer,
			Governance<I, F> governance, I extensionInterface,
			ManagedObjectContainer managedObject) {
		this.governanceContainer = governanceContainer;
		this.governance = governance;
		this.extensionInterface = extensionInterface;
		this.managedObject = managedObject;
	}

	/*
	 * ================== ActiveGovernanceManager ========================
	 */

	@Override
	public ActiveGovernance getActiveGovernance() {
		return this;
	}

	@Override
	public void unregisterManagedObject() {
		this.managedObject.unregisterManagedObjectFromGovernance(this);
	}

	/*
	 * ================== ActiveGovernance ========================
	 */

	@Override
	public boolean isActive() {
		return this.governanceContainer.isActive();
	}

	@Override
	public FlowMetaData<?> getFlowMetaData() {
		// TODO implement ActiveGovernance.getFlowMetaData
		throw new UnsupportedOperationException(
				"TODO implement ActiveGovernance.getFlowMetaData");
	}

	/*
	 * ======================= Task ===============================
	 */

	@Override
	public Object doTask(
			TaskContext<ActiveGovernanceImpl<I, F>, None, None> context)
			throws Throwable {

		// Govern the managed object
		this.governance.governManagedObject(this.extensionInterface, this);

		// Nothing to return
		return null;
	}

	/*
	 * ==================== GovernanceContext =============================
	 */

	@Override
	public void doFlow(F key, Object parameter) {
		// TODO implement GovernanceContext<F>.doFlow
		throw new UnsupportedOperationException(
				"TODO implement GovernanceContext<F>.doFlow");
	}

	@Override
	public void doFlow(int flowIndex, Object parameter) {
		// TODO implement GovernanceContext<F>.doFlow
		throw new UnsupportedOperationException(
				"TODO implement GovernanceContext<F>.doFlow");
	}

}