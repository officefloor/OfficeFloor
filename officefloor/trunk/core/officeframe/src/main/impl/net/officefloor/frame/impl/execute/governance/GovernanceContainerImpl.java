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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.administration.GovernanceManager;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link GovernanceContainer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceContainerImpl<I, F extends Enum<F>> implements
		GovernanceContainer<I>, GovernanceManager, GovernanceContext<F> {

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaData<I, F> metaData;

	/**
	 * {@link ProcessState} lock.
	 */
	private final Object processLock;

	/**
	 * {@link ActiveGovernanceImpl} instances.
	 */
	private final List<ActiveGovernanceImpl> activeGovernances = new LinkedList<ActiveGovernanceImpl>();

	/**
	 * {@link Governance}.
	 */
	private Governance<I> governance = null;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link GovernanceMetaData}.
	 * @param processLock
	 *            {@link ProcessState} lock.
	 */
	public GovernanceContainerImpl(GovernanceMetaData<I, F> metaData,
			Object processLock) {
		this.metaData = metaData;
		this.processLock = processLock;
	}

	/*
	 * ==================== GovernanceContainer =========================
	 */

	@Override
	public ActiveGovernance governManagedObject(I extensionInterface,
			ManagedObjectContainer managedobjectContainer) throws Exception {

		// Create the governance
		if (this.governance == null) {
			return null; // no governance
		}

		// Govern the managed object
		this.governance.governManagedObject(extensionInterface);

		// Create the active governance
		ActiveGovernanceImpl activeGovernance = new ActiveGovernanceImpl(
				managedobjectContainer);

		// Register the active governance
		this.activeGovernances.add(activeGovernance);

		// Return the active governance
		return activeGovernance;
	}

	@Override
	public void disregardGovernance() {

		// Stop the governance
		this.governance.stopGovernance();

		// Unregister managed objects from governance
		for (ActiveGovernanceImpl activeGovernance : this.activeGovernances) {
			activeGovernance.unregisterManagedObject();
		}

		// Disregard the governance
		this.governance = null;
	}

	@Override
	public GovernanceManager getGovernanceManager() {
		return this;
	}

	/*
	 * ======================= GovernanceManager =========================
	 */

	@Override
	public void activateGovernance() {

		// Determine if already active governance
		if (this.governance != null) {
			return;
		}

		// Create the governance
		this.governance = this.metaData.createGovernance(this);

		// Start governance
		this.governance.startGovernance();
	}

	@Override
	public void enforceGovernance() {

		// Apply the governance
		this.governance.applyGovernance();

		// Governance applied, so now stop governance
		this.disregardGovernance();
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

	/**
	 * {@link ActiveGovernance} implementation.
	 */
	private class ActiveGovernanceImpl implements ActiveGovernance {

		/**
		 * {@link ManagedObjectContainer}.
		 */
		private final ManagedObjectContainer managedObject;

		/**
		 * Initiate.
		 * 
		 * @param managedObject
		 *            {@link ManagedObjectContainer}.
		 */
		public ActiveGovernanceImpl(ManagedObjectContainer managedObject) {
			this.managedObject = managedObject;
		}

		/**
		 * Unregisters the {@link ManagedObject} from {@link Governance}.
		 */
		public void unregisterManagedObject() {
			this.managedObject.unregisterManagedObjectFromGovernance(this);
		}

		/*
		 * ================== ActiveGovernance ========================
		 */

		@Override
		public boolean isActive() {
			return (GovernanceContainerImpl.this.governance != null);
		}
	}

}