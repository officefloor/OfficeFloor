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
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Factory for the creation of {@link RawManagedObjectMetaData}.
 * 
 * @author Daniel
 */
public interface RawManagedObjectMetaDataFactory {

	/**
	 * Creates the {@link RawManagedObjectMetaDataImpl}.
	 * 
	 * @param configuration
	 *            {@link ManagedObjectSourceConfiguration}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param officeFloorConfiguration
	 *            {@link OfficeFloorConfiguration} of the {@link OfficeFloor}
	 *            containing the {@link ManagedObjectSource}.
	 * @return {@link RawManagedObjectMetaDataImpl} or <code>null</code> if
	 *         issue.
	 */
	<D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> RawManagedObjectMetaData<D, H> constructRawManagedObjectMetaData(
			ManagedObjectSourceConfiguration<H, MS> configuration,
			OfficeFloorIssues issues, AssetManagerFactory assetManagerFactory,
			OfficeFloorConfiguration officeFloorConfiguration);

}
