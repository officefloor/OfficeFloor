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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data of the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceMetaData<I, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	String getGovernanceName();

	/**
	 * Creates the {@link GovernanceContainer}.
	 * 
	 * @param processState
	 *            {@link ProcessState}.
	 * @param processRegisteredIndex
	 *            Index of the {@link Governance} within the
	 *            {@link ProcessState}.
	 * @return {@link GovernanceContainer}.
	 */
	GovernanceContainer<I> createGovernanceContainer(ProcessState processState,
			int processRegisteredIndex);

	/**
	 * Create the {@link Governance}.
	 * 
	 * @return {@link Governance}.
	 * @throws Throwable
	 *             If fails to create the {@link Governance}.
	 * 
	 * @deprecated Use GovernanceFactory and provide to GovernanceContainer.
	 */
	Governance<I, F> createGovernance() throws Throwable;

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
	 * @param managedObjectContainerRegisteredIndex
	 *            Registered index of the {@link ActiveGovernance} within the
	 *            {@link ManagedObjectContainer}. This is to enable easier
	 *            identification of the {@link ActiveGovernance} within the
	 *            {@link ManagedObjectContainer} for unregistering.
	 * @return {@link ActiveGovernanceManager}.
	 */
	ActiveGovernanceManager createActiveGovernance(
			GovernanceContainer<I> governanceContainer,
			GovernanceControl<I, F> governanceControl, I extensionInterface,
			ManagedObjectContainer managedobjectContainer,
			int managedObjectContainerRegisteredIndex);

	/**
	 * Obtains the {@link FlowMetaData} for activating the {@link Governance}.
	 * 
	 * @return {@link FlowMetaData} for activating the {@link Governance}.
	 */
	FlowMetaData<?> getActivateFlowMetaData();

	/**
	 * Obtains the {@link FlowMetaData} for {@link Governance} over a
	 * {@link ManagedObject}.
	 * 
	 * @return {@link FlowMetaData} for {@link Governance} over a
	 *         {@link ManagedObject}.
	 */
	FlowMetaData<?> getGovernFlowMetaData();

	/**
	 * Obtains the {@link FlowMetaData} for enforcing the {@link Governance}.
	 * 
	 * @return {@link FlowMetaData} for enforcing the {@link Governance}.
	 */
	FlowMetaData<?> getEnforceFlowMetaData();

	/**
	 * Obtains the {@link FlowMetaData} for disregarding the {@link Governance}.
	 * 
	 * @return {@link FlowMetaData} for disregarding the {@link Governance}.
	 */
	FlowMetaData<?> getDisregardFlowMetaData();

	/**
	 * Creates the {@link GovernanceContext}.
	 * 
	 * @param taskContext
	 *            {@link TaskContext}.
	 * @return {@link GovernanceContext}.
	 */
	GovernanceContext<F> createGovernanceContext(
			TaskContext<?, ?, F> taskContext);

}