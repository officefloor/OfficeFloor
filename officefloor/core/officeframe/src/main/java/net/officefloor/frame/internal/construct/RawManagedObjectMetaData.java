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
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Meta-data for a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawManagedObjectMetaData<O extends Enum<O>, F extends Enum<F>> {

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
	ManagedObjectSource<O, F> getManagedObjectSource();

	/**
	 * Obtains the {@link ManagedObjectSourceMetaData}.
	 * 
	 * @return {@link ManagedObjectSourceMetaData}.
	 */
	ManagedObjectSourceMetaData<O, F> getManagedObjectSourceMetaData();

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
	 * Obtains the {@link TeamManagement} responsible for {@link Escalation}
	 * handling by this {@link ManagedObject}.
	 * 
	 * @return {@link TeamManagement}.
	 */
	TeamManagement getEscalationResponsibleTeam();

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
	 * @param assetType
	 *            {@link AssetType} of the {@link Asset} requiring the
	 *            {@link ManagedObject}.
	 * @param assetName
	 *            Name of the {@link Asset} requiring the {@link ManagedObject}.
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
	ManagedObjectMetaData<O> createManagedObjectMetaData(AssetType assetType, String assetName,
			RawBoundManagedObjectMetaData boundMetaData, int instanceIndex,
			RawBoundManagedObjectInstanceMetaData<O> boundInstanceMetaData, ManagedObjectIndex[] dependencyMappings,
			ManagedObjectGovernanceMetaData<?>[] governanceMetaData, AssetManagerFactory assetManagerFactory,
			OfficeFloorIssues issues);

}