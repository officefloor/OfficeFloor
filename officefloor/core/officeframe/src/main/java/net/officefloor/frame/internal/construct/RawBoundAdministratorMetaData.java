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

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyKey;
import net.officefloor.frame.spi.administration.source.AdministratorDutyMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Raw meta-data of a bound {@link Administrator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawBoundAdministratorMetaData<I, A extends Enum<A>> {

	/**
	 * Obtains the name the {@link Administrator} is bound under.
	 * 
	 * @return Name the {@link Administrator} is bound under.
	 */
	String getBoundAdministratorName();

	/**
	 * Obtains the {@link AdministratorIndex}.
	 * 
	 * @return {@link AdministratorIndex}.
	 */
	AdministratorIndex getAdministratorIndex();

	/**
	 * Obtains the listing of the {@link RawBoundManagedObjectMetaData} of the
	 * {@link ManagedObject} instances being administered.
	 * 
	 * @return Listing of the {@link RawBoundManagedObjectMetaData} of the
	 *         {@link ManagedObject} instances being administered.
	 */
	RawBoundManagedObjectMetaData[] getAdministeredRawBoundManagedObjects();

	/**
	 * Obtains the {@link AdministratorMetaData} for this {@link Administrator}.
	 * 
	 * @return {@link AdministratorMetaData} for this {@link Administrator}.
	 */
	AdministratorMetaData<I, A> getAdministratorMetaData();

	/**
	 * Obtains the {@link DutyKey} for the key identifying a {@link Duty}.
	 * 
	 * @param key
	 *            Key identifying a {@link Duty} as per
	 *            {@link AdministratorDutyMetaData}.
	 * @return {@link DutyKey} or <code>null</code> if could not find the
	 *         {@link Duty}.
	 */
	DutyKey<A> getDutyKey(Enum<?> key);

	/**
	 * Obtains the {@link DutyKey} for the name identifying the {@link Duty}.
	 * 
	 * @param dutyName
	 *            Name identifying a {@link Duty} as per
	 *            {@link AdministratorDutyMetaData}.
	 * @return {@link DutyKey} or <code>null</code> if could not find the
	 *         {@link Duty}.
	 */
	DutyKey<A> getDutyKey(String dutyName);

	/**
	 * Links the {@link TaskMetaData} instances to create {@link Flow} of
	 * execution, along with the {@link Governance}.
	 * 
	 * @param taskMetaDataLocator
	 *            {@link OfficeMetaDataLocator}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	void linkOfficeMetaData(OfficeMetaDataLocator taskMetaDataLocator,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues);

}