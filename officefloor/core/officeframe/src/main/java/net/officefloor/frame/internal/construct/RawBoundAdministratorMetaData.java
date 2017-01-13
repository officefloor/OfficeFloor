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
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.AdministrationDuty;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;

/**
 * Raw meta-data of a bound {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawBoundAdministratorMetaData<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Obtains the name the {@link Administration} is bound under.
	 * 
	 * @return Name the {@link Administration} is bound under.
	 */
	String getBoundAdministrationName();

	/**
	 * Obtains the listing of the {@link RawBoundManagedObjectMetaData} of the
	 * {@link ManagedObject} instances being administered.
	 * 
	 * @return Listing of the {@link RawBoundManagedObjectMetaData} of the
	 *         {@link ManagedObject} instances being administered.
	 */
	RawBoundManagedObjectMetaData[] getAdministeredRawBoundManagedObjects();

	/**
	 * Obtains the {@link AdministrationMetaData} for this
	 * {@link AdministrationDuty}.
	 * 
	 * @return {@link AdministrationMetaData} for this
	 *         {@link AdministrationDuty}.
	 */
	AdministrationMetaData<E, F, G> getAdministratorMetaData();

	/**
	 * Links the {@link ManagedFunctionMetaData} instances to create
	 * {@link Flow} of execution, along with the {@link Governance}.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param functionLocator
	 *            {@link ManagedFunctionLocator}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	void linkOfficeMetaData(OfficeMetaData officeMetaData, ManagedFunctionLocator functionLocator,
			OfficeFloorIssues issues);

}