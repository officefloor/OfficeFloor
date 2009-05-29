/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.impl.construct.officefloor.RawOfficeFloorMetaDataImpl;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;

/**
 * Factory for creating {@link RawOfficeFloorMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawOfficeFloorMetaDataFactory {

	/**
	 * Constructs the {@link RawOfficeFloorMetaDataImpl} from the
	 * {@link OfficeFloorConfiguration}.
	 * 
	 * @param configuration
	 *            {@link OfficeFloorConfiguration}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param rawTeamFactory
	 *            {@link RawTeamMetaDataFactory}.
	 * @param rawMosFactory
	 *            {@link RawManagedObjectMetaDataFactory}.
	 * @param rawBoundMoFactory
	 *            {@link RawBoundManagedObjectMetaDataFactory}.
	 * @param rawBoundAdminFactory
	 *            {@link RawBoundAdministratorMetaDataFactory}.
	 * @param rawOfficeFactory
	 *            {@link RawOfficeMetaDataFactory}.
	 * @param rawWorkFactory
	 *            {@link RawWorkMetaDataFactory}.
	 * @param rawTaskFactory
	 *            {@link RawTaskMetaDataFactory}.
	 * @return {@link RawOfficeFloorMetaData}.
	 */
	RawOfficeFloorMetaData constructRawOfficeFloorMetaData(
			OfficeFloorConfiguration configuration, OfficeFloorIssues issues,
			RawTeamMetaDataFactory rawTeamFactory,
			RawManagedObjectMetaDataFactory rawMosFactory,
			RawBoundManagedObjectMetaDataFactory rawBoundMoFactory,
			RawBoundAdministratorMetaDataFactory rawBoundAdminFactory,
			RawOfficeMetaDataFactory rawOfficeFactory,
			RawWorkMetaDataFactory rawWorkFactory,
			RawTaskMetaDataFactory rawTaskFactory);

}