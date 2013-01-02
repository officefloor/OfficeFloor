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
package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link ThreadMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadMetaDataImpl implements ThreadMetaData {

	/**
	 * {@link ManagedObjectMetaData} instances.
	 */
	private final ManagedObjectMetaData<?>[] managedObjectMetaData;

	/**
	 * {@link GovernanceMetaData} instances.
	 */
	private final GovernanceMetaData<?, ?>[] governanceMetaData;

	/**
	 * {@link AdministratorMetaData} instances.
	 */
	private final AdministratorMetaData<?, ?>[] administratorMetaData;

	/**
	 * {@link GovernanceDeactivationStrategy} on {@link ThreadState} completion.
	 */
	private final GovernanceDeactivationStrategy governanceDeactivationStrategy;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} instances.
	 * @param governanceMetaData
	 *            {@link GovernanceMetaData} instances.
	 * @param administratorMetaData
	 *            {@link AdministratorMetaData} instances.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy} on {@link ThreadState}
	 *            completion.
	 */
	public ThreadMetaDataImpl(ManagedObjectMetaData<?>[] managedObjectMetaData,
			GovernanceMetaData<?, ?>[] governanceMetaData,
			AdministratorMetaData<?, ?>[] administratorMetaData,
			GovernanceDeactivationStrategy governanceDeactivationStrategy) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.governanceMetaData = governanceMetaData;
		this.administratorMetaData = administratorMetaData;
		this.governanceDeactivationStrategy = governanceDeactivationStrategy;
	}

	/*
	 * ================== ThreadMetaData =============================
	 */

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public GovernanceMetaData<?, ?>[] getGovernanceMetaData() {
		return this.governanceMetaData;
	}

	@Override
	public AdministratorMetaData<?, ?>[] getAdministratorMetaData() {
		return this.administratorMetaData;
	}

	@Override
	public GovernanceDeactivationStrategy getGovernanceDeactivationStrategy() {
		return this.governanceDeactivationStrategy;
	}

}