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

import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data for the {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadMetaData {

	/**
	 * Obtains the {@link ManagedObjectMetaData} of the {@link ManagedObject}
	 * instances bound to the {@link ThreadState}.
	 * 
	 * @return {@link ManagedObjectMetaData} of the {@link ManagedObject}
	 *         instances bound to the {@link ThreadState}.
	 */
	ManagedObjectMetaData<?>[] getManagedObjectMetaData();

	/**
	 * Obtains the {@link GovernanceMetaData} of the possible {@link Governance}
	 * within this {@link ThreadState}.
	 * 
	 * @return {@link GovernanceMetaData} instances.
	 */
	GovernanceMetaData<?, ?>[] getGovernanceMetaData();

	/**
	 * Obtains the {@link AdministratorMetaData} of the {@link Administrator}
	 * instances bound to the {@link ThreadState}.
	 * 
	 * @return {@link AdministratorMetaData} of the {@link Administrator}
	 *         instances bound to the {@link ThreadState}.
	 */
	AdministratorMetaData<?, ?>[] getAdministratorMetaData();

	/**
	 * Obtains the {@link GovernanceDeactivationStrategy} for active
	 * {@link Governance} on {@link ThreadState} completion.
	 * 
	 * @return {@link GovernanceDeactivationStrategy} for active
	 *         {@link Governance} on {@link ThreadState} completion.
	 */
	GovernanceDeactivationStrategy getGovernanceDeactivationStrategy();

}