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
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Raw meta-data of {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawWorkMetaData<W extends Work> {

	/**
	 * Obtains the name of the {@link Work}.
	 * 
	 * @return Name of the {@link Work}.
	 */
	String getWorkName();

	/**
	 * Obtains the {@link RawOfficeMetaData} of the {@link Office} containing
	 * this {@link Work}.
	 * 
	 * @return {@link RawOfficeMetaData}.
	 */
	RawOfficeMetaData getRawOfficeMetaData();

	/**
	 * Constructs the {@link RawBoundManagedObjectMetaData} for the
	 * {@link ManagedObject} of the {@link Work}.
	 * 
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 * @return {@link RawBoundManagedObjectMetaData} or <code>null</code> not
	 *         found.
	 */
	RawBoundManagedObjectMetaData getScopeManagedObjectMetaData(
			String scopeManagedObjectName);

	/**
	 * Obtains the {@link AdministratorIndex} for the {@link Work}
	 * {@link Administrator} name.
	 * 
	 * @param scopeAdministratorName
	 *            Name of the {@link Administrator} within the
	 *            {@link ManagedObjectScope}.
	 * @return {@link RawBoundAdministratorMetaData} or <code>null</code> if not
	 *         found.
	 */
	RawBoundAdministratorMetaData<?, ?> getScopeAdministratorMetaData(
			String scopeAdministratorName);

	/**
	 * Links the {@link TaskMetaData} instances to enable {@link JobSequence} of
	 * execution. Also links the {@link Governance} for any possible associated
	 * {@link Duty}.
	 * 
	 * @param taskLocator
	 *            {@link OfficeMetaDataLocator}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	void linkOfficeMetaData(OfficeMetaDataLocator taskLocator,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues);

	/**
	 * Obtains the {@link WorkMetaData} for this {@link RawWorkMetaData}.
	 * 
	 * @return {@link WorkMetaData}.
	 */
	WorkMetaData<W> getWorkMetaData();

}