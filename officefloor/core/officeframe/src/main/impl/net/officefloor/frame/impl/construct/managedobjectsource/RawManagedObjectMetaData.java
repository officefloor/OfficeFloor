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
package net.officefloor.frame.impl.construct.managedobjectsource;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.impl.construct.asset.AssetManagerFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Raw {@link ManagedObjectMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagedObjectMetaData<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Name of the {@link ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * {@link ManagedObjectSourceConfiguration}.
	 */
	private final ManagedObjectSourceConfiguration<F, ?> managedObjectSourceConfiguration;

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<O, F> managedObjectSource;

	/**
	 * {@link ManagedObjectSourceMetaData} for the {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSourceMetaData<O, F> managedObjectSourceMetaData;

	/**
	 * Timeout for sourcing the {@link ManagedObject} and asynchronous
	 * operations on the {@link ManagedObject}.
	 */
	private final long timeout;

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool;

	/**
	 * {@link ThreadCompletionListener} instances.
	 */
	private final ThreadCompletionListener[] threadCompletionListeners;

	/**
	 * Type of the {@link Object} returned from the {@link ManagedObject}.
	 */
	private final Class<?> objectType;

	/**
	 * Flag indiating if {@link ProcessAwareManagedObject}.
	 */
	private final boolean isProcessAware;

	/**
	 * Flag indicating if {@link NameAwareManagedObject}.
	 */
	private final boolean isNameAware;

	/**
	 * Flag indicating if {@link AsynchronousManagedObject}.
	 */
	private final boolean isAsynchronous;

	/**
	 * Flag indicating if {@link CoordinatingManagedObject}.
	 */
	private final boolean isCoordinating;

	/**
	 * {@link RawManagingOfficeMetaData}.
	 */
	private final RawManagingOfficeMetaData<F> rawManagingOfficeMetaData;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param managedObjectSourceConfiguration
	 *            {@link ManagedObjectSourceConfiguration}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @param managedObjectSourceMetaData
	 *            {@link ManagedObjectSourceMetaData} for the
	 *            {@link ManagedObjectSource}.
	 * @param Timeout
	 *            Timeout for the {@link ManagedObjectSource}.
	 * @param managedObjectPool
	 *            {@link ManagedObjectPool}.
	 * @param threadCompletionListeners
	 *            {@link ThreadCompletionListener} instances.
	 * @param objectType
	 *            Type of the {@link Object} returned from the
	 *            {@link ManagedObject}.
	 * @param isProcessAware
	 *            Flag indicating if {@link ProcessAwareManagedObject}.
	 * @param isNameAware
	 *            Flag indicating if {@link NameAwareManagedObject}.
	 * @param isAsynchronous
	 *            Flag indicating if {@link AsynchronousManagedObject}.
	 * @param isCoordinating
	 *            Flag indicating if {@link CoordinatingManagedObject}.
	 * @param rawManagingOfficeMetaData
	 *            {@link RawManagingOfficeMetaData}.
	 */
	public RawManagedObjectMetaData(String managedObjectName,
			ManagedObjectSourceConfiguration<F, ?> managedObjectSourceConfiguration,
			ManagedObjectSource<O, F> managedObjectSource,
			ManagedObjectSourceMetaData<O, F> managedObjectSourceMetaData, long timeout,
			ManagedObjectPool managedObjectPool, ThreadCompletionListener[] threadCompletionListeners,
			Class<?> objectType, boolean isProcessAware, boolean isNameAware, boolean isAsynchronous,
			boolean isCoordinating, RawManagingOfficeMetaData<F> rawManagingOfficeMetaData) {
		this.managedObjectName = managedObjectName;
		this.managedObjectSourceConfiguration = managedObjectSourceConfiguration;
		this.managedObjectSource = managedObjectSource;
		this.managedObjectSourceMetaData = managedObjectSourceMetaData;
		this.timeout = timeout;
		this.managedObjectPool = managedObjectPool;
		this.threadCompletionListeners = threadCompletionListeners;
		this.objectType = objectType;
		this.isProcessAware = isProcessAware;
		this.isNameAware = isNameAware;
		this.isAsynchronous = isAsynchronous;
		this.isCoordinating = isCoordinating;
		this.rawManagingOfficeMetaData = rawManagingOfficeMetaData;
	}

	/**
	 * Obtains the name of the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedObject}.
	 */
	public String getManagedObjectName() {
		return this.managedObjectName;
	}

	/**
	 * Obtains the {@link ManagedObjectSourceConfiguration}.
	 * 
	 * @return {@link ManagedObjectSourceConfiguration}.
	 */
	public ManagedObjectSourceConfiguration<F, ?> getManagedObjectSourceConfiguration() {
		return this.managedObjectSourceConfiguration;
	}

	/**
	 * Obtains the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	public ManagedObjectSource<O, F> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	/**
	 * Obtains the {@link ManagedObjectSourceMetaData}.
	 * 
	 * @return {@link ManagedObjectSourceMetaData}.
	 */
	public ManagedObjectSourceMetaData<O, F> getManagedObjectSourceMetaData() {
		return this.managedObjectSourceMetaData;
	}

	/**
	 * Obtains the {@link ManagedObjectPoolFactory}.
	 * 
	 * @return {@link ManagedObjectPoolFactory} or <code>null</code> if not
	 *         pooled.
	 */
	public ManagedObjectPool getManagedObjectPool() {
		return this.managedObjectPool;
	}

	/**
	 * Obtains the {@link ThreadCompletionListener} instances for the
	 * {@link ManagedObject}.
	 * 
	 * @return {@link ThreadCompletionListener} instances for the
	 *         {@link ManagedObject}.
	 */
	public ThreadCompletionListener[] getThreadCompletionListeners() {
		return this.threadCompletionListeners;
	}

	/**
	 * Obtains the type of {@link Object} returned from the
	 * {@link ManagedObject}.
	 * 
	 * @return Obtains the type of {@link Object} returned from the
	 *         {@link ManagedObject}.
	 */
	public Class<?> getObjectType() {
		return this.objectType;
	}

	/**
	 * Obtains the {@link RawManagingOfficeMetaData} of the {@link Office}
	 * managing this {@link ManagedObject}.
	 * 
	 * @return {@link RawManagingOfficeMetaData} of the {@link Office} managing
	 *         this {@link ManagedObject}.
	 */
	public RawManagingOfficeMetaData<F> getRawManagingOfficeMetaData() {
		return this.rawManagingOfficeMetaData;
	}

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
	public ManagedObjectMetaData<?> createManagedObjectMetaData(AssetType assetType, String assetName,
			RawBoundManagedObjectMetaData boundMetaData, int instanceIndex,
			RawBoundManagedObjectInstanceMetaData<?> boundInstanceMetaData, ManagedObjectIndex[] dependencyMappings,
			ManagedObjectGovernanceMetaData<?>[] governanceMetaData, AssetManagerFactory assetManagerFactory,
			OfficeFloorIssues issues) {

		// Obtain the bound name and scope
		String boundName = boundMetaData.getBoundManagedObjectName();
		ManagedObjectScope scope = boundMetaData.getManagedObjectIndex().getManagedObjectScope();

		// Create the bound reference name
		String boundReferenceName = scope + ":" + (scope == ManagedObjectScope.FUNCTION ? assetName + ":" : "")
				+ instanceIndex + ":" + boundName;

		// Create the source managed object asset manager
		AssetManager sourcingAssetManager = assetManagerFactory.createAssetManager(AssetType.MANAGED_OBJECT,
				boundReferenceName, "source", issues);

		// Create operations asset manager only if asynchronous
		AssetManager operationsAssetManager = null;
		if (this.isAsynchronous) {
			// Asynchronous so provide operations manager
			operationsAssetManager = assetManagerFactory.createAssetManager(AssetType.MANAGED_OBJECT,
					boundReferenceName, "operations", issues);
		}

		// Create the managed object meta-data
		ManagedObjectMetaDataImpl<O> moMetaData = new ManagedObjectMetaDataImpl<O>(boundName, this.objectType,
				instanceIndex, this.managedObjectSource, this.managedObjectPool, this.isProcessAware, this.isNameAware,
				sourcingAssetManager, this.isAsynchronous, operationsAssetManager, this.isCoordinating,
				dependencyMappings, this.timeout, governanceMetaData);

		// Have the managed object managed by its managing office
		this.rawManagingOfficeMetaData.manageManagedObject(moMetaData);

		// Return the managed object meta-data
		return moMetaData;
	}

}