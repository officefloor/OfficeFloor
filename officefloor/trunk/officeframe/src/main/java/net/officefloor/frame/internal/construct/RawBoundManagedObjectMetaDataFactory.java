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

import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Factory for the creation of {@link RawBoundManagedObjectMetaData}.
 * 
 * @author Daniel
 */
public interface RawBoundManagedObjectMetaDataFactory {

	/**
	 * Constructs the {@link RawBoundManagedObjectMetaData} instances.
	 * 
	 * @param boundManagedObjectConfiguration
	 *            {@link ManagedObjectConfiguration} of the
	 *            {@link RawBoundManagedObjectMetaData} instances.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} for the
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param assetType
	 *            {@link AssetType} that {@link ManagedObject} instances are
	 *            being bound.
	 * @param assetName
	 *            Name of the {@link Asset} that {@link ManagedObject} instances
	 *            are being bound.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param registeredManagedObjects
	 *            Registered {@link ManagedObject} instances that may be
	 *            selected for being bound.
	 * @param scopeManagedObjects
	 *            Already bound {@link ManagedObject} instances that may full
	 *            fill dependencies of bound {@link ManagedObject} instances.
	 * @return {@link RawBoundManagedObjectMetaData} instances for the bound
	 *         {@link ManagedObject} instances.
	 */
	RawBoundManagedObjectMetaData<?>[] constructBoundManagedObjectMetaData(
			ManagedObjectConfiguration<?>[] boundManagedObjectConfiguration,
			OfficeFloorIssues issues,
			ManagedObjectScope managedObjectScope,
			AssetType assetType,
			String assetName,
			AssetManagerFactory assetManagerFactory,
			Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjects,
			Map<String, RawBoundManagedObjectMetaData<?>> scopeManagedObjects);

	/**
	 * <p>
	 * For the input list of {@link ProcessState} bound
	 * {@link RawBoundManagedObjectMetaData} instances, this method returns the
	 * list appending in any {@link Office} managed {@link ManagedObject}
	 * instances that are not already bound.
	 * <p>
	 * This provides for the {@link ManagedObjectMetaData} within the
	 * {@link ProcessState} of the {@link Office} for
	 * {@link ManagedObjectSource} instances that invoke {@link Task} instances
	 * within the {@link Office} but are not used by the {@link Office}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office} for raising issues.
	 * @param processBoundManagedObjectMetaData
	 *            {@link RawBoundManagedObjectMetaData} instances bound to the
	 *            {@link ProcessState} of the {@link Office}.
	 * @param officeManagingManagedObjects
	 *            {@link RawManagingOfficeMetaData} instances.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return Resulting new list of {@link RawBoundManagedObjectMetaData}
	 *         instances affixing in the {@link RawManagingOfficeMetaData}
	 *         instances to the original {@link RawBoundManagedObjectMetaData}
	 *         list.
	 */
	RawBoundManagedObjectMetaData<?>[] affixOfficeManagingManagedObjects(
			String officeName,
			RawBoundManagedObjectMetaData<?>[] processBoundManagedObjectMetaData,
			RawManagingOfficeMetaData<?>[] officeManagingManagedObjects,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues);

}