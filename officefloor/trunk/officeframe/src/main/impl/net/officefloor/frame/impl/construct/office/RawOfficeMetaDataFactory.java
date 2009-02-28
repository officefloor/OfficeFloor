/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.construct.office;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.impl.construct.administrator.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.impl.construct.officefloor.RawOfficeFloorMetaData;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;

/**
 * Factory for creating the {@link RawOfficeMetaData}.
 * 
 * @author Daniel
 */
public interface RawOfficeMetaDataFactory {

	// TODO consider how to link in the managed objects that invoke tasks but
	// are not used by the office.

	/**
	 * Constructs the {@link RawOfficeMetaData}.
	 * 
	 * @param configuration
	 *            {@link OfficeConfiguration}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param rawOfficeFloorMetaData
	 *            {@link RawOfficeFloorMetaData}.
	 * @param rawBoundManagedObjectFactory
	 *            {@link RawBoundManagedObjectMetaDataFactory}.
	 * @param rawBoundAdministratorFactory
	 *            {@link RawBoundAdministratorMetaDataFactory}.
	 * @return {@link RawOfficeMetaData}.
	 */
	RawOfficeMetaData constructRawOfficeMetaData(
			OfficeConfiguration configuration, OfficeFloorIssues issues,
			RawOfficeFloorMetaData rawOfficeFloorMetaData,
			RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory,
			RawBoundAdministratorMetaDataFactory rawBoundAdministratorFactory);

}