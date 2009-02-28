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
package net.officefloor.frame.impl.construct.work;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.administrator.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.internal.configuration.WorkConfiguration;

/**
 * Factory to create the {@link RawWorkMetaData}.
 * 
 * @author Daniel
 */
public interface RawWorkMetaDataFactory {

	/**
	 * Constructs the {@link RawWorkMetaData}.
	 * 
	 * @param configuration
	 *            {@link WorkConfiguration}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param rawOfficeMetaData
	 *            {@link RawOfficeMetaData}.
	 * @param rawBoundManagedObjectFactory
	 *            {@link RawBoundManagedObjectMetaDataFactory}.
	 * @param rawBoundAdministratorFactory
	 *            {@link RawBoundAdministratorMetaDataFactory}.
	 * @return {@link RawWorkMetaData}.
	 */
	<W extends Work> RawWorkMetaData<W> constructRawWorkMetaData(
			WorkConfiguration<W> configuration, OfficeFloorIssues issues,
			RawOfficeMetaData rawOfficeMetaData,
			RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory,
			RawBoundAdministratorMetaDataFactory rawBoundAdministratorFactory);

}