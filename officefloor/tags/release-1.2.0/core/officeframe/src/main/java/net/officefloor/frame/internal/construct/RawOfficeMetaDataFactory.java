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

package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;

/**
 * Factory for creating the {@link RawOfficeMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawOfficeMetaDataFactory {

	/**
	 * Constructs the {@link RawOfficeMetaData}.
	 * 
	 * @param configuration
	 *            {@link OfficeConfiguration}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param officeManagingManagedObjects
	 *            {@link RawManagingOfficeMetaData} instances.
	 * @param rawOfficeFloorMetaData
	 *            {@link RawOfficeFloorMetaData}.
	 * @param rawBoundManagedObjectFactory
	 *            {@link RawBoundManagedObjectMetaDataFactory}.
	 * @param rawBoundAdministratorFactory
	 *            {@link RawBoundAdministratorMetaDataFactory}.
	 * @param rawWorkFactory
	 *            {@link RawWorkMetaDataFactory}.
	 * @param rawTaskFactory
	 *            {@link RawTaskMetaDataFactory}.
	 * @return {@link RawOfficeMetaData}.
	 */
	RawOfficeMetaData constructRawOfficeMetaData(
			OfficeConfiguration configuration, OfficeFloorIssues issues,
			RawManagingOfficeMetaData<?>[] officeManagingManagedObjects,
			RawOfficeFloorMetaData rawOfficeFloorMetaData,
			RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory,
			RawBoundAdministratorMetaDataFactory rawBoundAdministratorFactory,
			RawWorkMetaDataFactory rawWorkFactory,
			RawTaskMetaDataFactory rawTaskFactory);

}