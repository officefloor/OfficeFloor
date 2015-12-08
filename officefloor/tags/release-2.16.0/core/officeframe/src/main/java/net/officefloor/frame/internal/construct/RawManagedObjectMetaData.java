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
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;

/**
 * Meta-data for a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawManagedObjectMetaData<D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedObject}.
	 */
	String getManagedObjectName();

	/**
	 * Obtains the {@link ManagedObjectSourceConfiguration}.
	 * 
	 * @return {@link ManagedObjectSourceConfiguration}.
	 */
	ManagedObjectSourceConfiguration<F, ?> getManagedObjectSourceConfiguration();

	/**
	 * Obtains the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	ManagedObjectSource<D, F> getManagedObjectSource();

	/**
	 * Obtains the {@link ManagedObjectSourceMetaData}.
	 * 
	 * @return {@link ManagedObjectSourceMetaData}.
	 */
	ManagedObjectSourceMetaData<D, F> getManagedObjectSourceMetaData();

	/**
	 * Obtains the {@link ManagedObjectPool}.
	 * 
	 * @return {@link ManagedObjectPool} or <code>null</code> if not pooled.
	 */
	ManagedObjectPool getManagedObjectPool();

	/**
	 * Obtains the type of {@link Object} returned from the
	 * {@link ManagedObject}.
	 * 
	 * @return Obtains the type of {@link Object} returned from the
	 *         {@link ManagedObject}.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the {@link RawManagingOfficeMetaData} of the {@link Office}
	 * managing this {@link ManagedObject}.
	 * 
	 * @return {@link RawManagingOfficeMetaData} of the {@link Office} managing
	 *         this {@link ManagedObject}.
	 */
	RawManagingOfficeMetaData<F> getRawManagingOfficeMetaData();

	/**
	 * Creates the {@link ManagedObjectMetaData}.
	 * 
	 * @param boundMetaData
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param instanceIndex
	 *            Index of the {@link RawBoundManagedObjectInstanceMetaData} on
	 *            the {@link RawBoundManagedObjectMetaData}.
	 * @param boundInstanceMetaData
	 *            {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param dependencyMappings
	 *            {@link ManagedObjectIndex} instances identifying the dependent
	 *            {@link ManagedObject} instances in dependency index order
	 *            required.
	 * @param governanceMetaData
	 *            {@link ManagedObjectGovernanceMetaData} identifying the
	 *            {@link Governance} for the {@link ManagedObject}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory} of the {@link Office} using the
	 *            {@link ManagedObject}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link ManagedObjectMetaData}.
	 */
	ManagedObjectMetaData<D> createManagedObjectMetaData(
			RawBoundManagedObjectMetaData boundMetaData, int instanceIndex,
			RawBoundManagedObjectInstanceMetaData<D> boundInstanceMetaData,
			ManagedObjectIndex[] dependencyMappings,
			ManagedObjectGovernanceMetaData<?>[] governanceMetaData,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues);

}