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

import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Factory for the creation of {@link RawBoundManagedObjectMetaData}.
 * 
 * @author Daniel Sagenschneider
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
	 * @param inputManagedObjects
	 *            Meta-data about input {@link ManagedObject} instances by
	 *            {@link ManagedObjectSource} instances.
	 * @param boundInputManagedObjects
	 *            Default {@link ManagedObjectSource} names for multiple input
	 *            {@link ManagedObject} instances bound to the same name.
	 *            Mapping is of input {@link ManagedObject} name to the default
	 *            {@link ManagedObjectSource} name.
	 * @param governanceMetaData
	 *            {@link RawGovernanceMetaData} by its {@link Office} registered
	 *            name.
	 * @return {@link RawBoundManagedObjectMetaData} instances for the bound
	 *         {@link ManagedObject} instances.
	 */
	RawBoundManagedObjectMetaData[] constructBoundManagedObjectMetaData(
			ManagedObjectConfiguration<?>[] boundManagedObjectConfiguration,
			OfficeFloorIssues issues,
			ManagedObjectScope managedObjectScope,
			AssetType assetType,
			String assetName,
			AssetManagerFactory assetManagerFactory,
			Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjects,
			Map<String, RawBoundManagedObjectMetaData> scopeManagedObjects,
			RawManagingOfficeMetaData<?>[] inputManagedObjects,
			Map<String, String> boundInputManagedObjects,
			Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaData);

}