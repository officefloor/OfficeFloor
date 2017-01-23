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

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Meta-data of the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationMetaData<E, F extends Enum<F>, G extends Enum<G>> extends ManagedFunctionLogicMetaData {

	/**
	 * Obtains the name of the {@link Administration}.
	 * 
	 * @return Name of the {@link Administration}.
	 */
	String getAdministrationName();

	/**
	 * Obtains the {@link AdministrationFactory}.
	 * 
	 * @return {@link AdministrationFactory}.
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the extension interface to administer the {@link ManagedObject}
	 * instances.
	 * 
	 * @return Extension interface to administer the {@link ManagedObject}
	 *         instances.
	 */
	Class<E> getExtensionInterface();

	/**
	 * Obtains the {@link ManagedObjectExtensionMetaData} over the
	 * {@link ManagedObject} instances to be administered by this
	 * {@link Administration}.
	 * 
	 * @return {@link ManagedObjectExtensionMetaData} over the
	 *         {@link ManagedObject} instances to be administered by this
	 *         {@link Administration}.
	 */
	ManagedObjectExtensionMetaData<E>[] getManagedObjectExtensionMetaData();

	/**
	 * Translates the {@link Administration} {@link Governance} index to the
	 * {@link ThreadState} {@link Governance} index.
	 * 
	 * @param governanceIndex
	 *            {@link Administration} {@link Governance} index.
	 * @return {@link ThreadState} {@link Governance} index.
	 */
	int translateGovernanceIndexToThreadIndex(int governanceIndex);

}