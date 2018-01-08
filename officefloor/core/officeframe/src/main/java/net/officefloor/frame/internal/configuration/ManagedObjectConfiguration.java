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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Configuration of a {@link ProcessState} or {@link ThreadState} bound
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectConfiguration<O extends Enum<O>> {

	/**
	 * Obtains the name of the {@link ManagedObject} registered within the
	 * {@link Office}.
	 * 
	 * @return Name of the {@link ManagedObject} registered within the
	 *         {@link Office}.
	 */
	String getOfficeManagedObjectName();

	/**
	 * Obtains name of the {@link ManagedObject} bound to either
	 * {@link ProcessState} or {@link ThreadState}.
	 * 
	 * @return Name of the {@link ManagedObject} bound to either
	 *         {@link ProcessState} or {@link ThreadState}.
	 */
	String getBoundManagedObjectName();

	/**
	 * Obtains the listing of {@link ManagedObjectDependencyConfiguration}
	 * instances.
	 * 
	 * @return {@link ManagedObjectDependencyConfiguration} instances.
	 */
	ManagedObjectDependencyConfiguration<O>[] getDependencyConfiguration();

	/**
	 * Obtains the listing of {@link ManagedObjectGovernanceConfiguration}
	 * instances.
	 * 
	 * @return {@link ManagedObjectGovernanceConfiguration} instances.
	 */
	ManagedObjectGovernanceConfiguration[] getGovernanceConfiguration();

	/**
	 * Obtains the listing of the {@link Administration} to be done before the
	 * {@link ManagedObject} is loaded.
	 * 
	 * @return Listing of the {@link Administration} to be done before the
	 *         {@link ManagedObject} is loaded.
	 */
	AdministrationConfiguration<?, ?, ?>[] getPreLoadAdministration();

}