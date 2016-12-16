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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data of the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceMetaData<I, F extends Enum<F>> extends JobMetaData {

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the {@link GovernanceFactory}.
	 * 
	 * @return {@link GovernanceFactory}.
	 */
	GovernanceFactory<? super I, F> getGovernanceFactory();

	/**
	 * Creates the {@link GovernanceContainer}.
	 * 
	 * @param threadState
	 *            {@link ThreadState}.
	 * @param threadRegisteredIndex
	 *            Index of {@link Governance} within the {@link ThreadState}.
	 * @return {@link GovernanceContainer}.
	 */
	GovernanceContainer<I, F> createGovernanceContainer(
			ThreadState threadState, int threadRegisteredIndex);

	/**
	 * Creates the {@link ActiveGovernance}.
	 * 
	 * @param governanceContainer
	 *            {@link GovernanceContainer}.
	 * @param governanceControl
	 *            {@link GovernanceControl}.
	 * @param extensionInterface
	 *            Extension interface to {@link ManagedObject} to enable
	 *            {@link Governance} over the {@link ManagedObject}.
	 * @param managedobjectContainer
	 *            {@link ManagedObjectContainer}.
	 * @param workContainer
	 *            {@link WorkContainer}.
	 * @param managedObjectContainerRegisteredIndex
	 *            Registered index of the {@link ActiveGovernance} within the
	 *            {@link ManagedObjectContainer}. This is to enable easier
	 *            identification of the {@link ActiveGovernance} within the
	 *            {@link ManagedObjectContainer} for unregistering.
	 * @return {@link ActiveGovernanceManager}.
	 */
	ActiveGovernanceManager<I, F> createActiveGovernance(
			GovernanceContainer<I, F> governanceContainer,
			GovernanceControl<I, F> governanceControl, I extensionInterface,
			ManagedObjectContainer managedobjectContainer,
			WorkContainer<?> workContainer,
			int managedObjectContainerRegisteredIndex);

	/**
	 * Creates a {@link GovernanceActivity} to activate the {@link Governance}.
	 * 
	 * @param governanceControl
	 *            {@link GovernanceControl}.
	 * @return {@link GovernanceActivity} to activate the {@link Governance}.
	 */
	GovernanceActivity<I, F> createActivateActivity(
			GovernanceControl<I, F> governanceControl);

	/**
	 * Creates a {@link GovernanceActivity} to provide {@link Governance} to the
	 * {@link ManagedObject}.
	 * 
	 * @param activeGovernanceControl
	 *            {@link ActiveGovernanceControl}.
	 * @return {@link GovernanceActivity} to provide {@link Governance} to the
	 *         {@link ManagedObject}.
	 */
	GovernanceActivity<I, F> createGovernActivity(
			ActiveGovernanceControl<F> activeGovernanceControl);

	/**
	 * Creates the {@link GovernanceActivity} to enforce the {@link Governance}.
	 * 
	 * @param governanceControl
	 *            {@link GovernanceControl}.
	 * @return {@link GovernanceActivity} to enforce the {@link Governance}.
	 */
	GovernanceActivity<I, F> createEnforceActivity(
			GovernanceControl<I, F> governanceControl);

	/**
	 * Creates the {@link GovernanceActivity} to disregard the
	 * {@link Governance}.
	 * 
	 * @param governanceControl
	 *            {@link GovernanceContext}.
	 * @return {@link GovernanceActivity} to disregard the {@link Governance}.
	 */
	GovernanceActivity<I, F> createDisregardActivity(
			GovernanceControl<I, F> governanceControl);

	/**
	 * Obtains the {@link FlowMetaData} for the specified index.
	 * 
	 * @param flowIndex
	 *            Index of the {@link FlowMetaData}.
	 * @return {@link FlowMetaData} for the specified index.
	 */
	FlowMetaData<?> getFlow(int flowIndex);

	/**
	 * Creates the {@link JobNode} for the {@link GovernanceActivity}.
	 * 
	 * @param flow
	 *            {@link Flow} for containing this
	 *            {@link GovernanceActivity}.
	 * @param governanceActivity
	 *            {@link GovernanceActivity}.
	 * @param parallelJobNodeOwner
	 *            Parallel {@link JobNode} owner.
	 * @return {@link JobNode} for the {@link GovernanceActivity}.
	 */
	JobNode createGovernanceJob(Flow flow,
			GovernanceActivity<I, F> governanceActivity,
			JobNode parallelJobNodeOwner);

}