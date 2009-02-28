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
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.Properties;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.asset.AssetManagerFactory;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceUnknownPropertyError;
import net.officefloor.frame.spi.managedobject.source.ResourceLocator;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Raw {@link ManagedObjectMetaData}.
 * 
 * @author Daniel
 */
public class RawManagedObjectMetaDataImpl<D extends Enum<D>, H extends Enum<H>>
		implements RawManagedObjectMetaDataFactory,
		RawManagedObjectMetaData<D, H> {

	/**
	 * Obtains the {@link RawManagedObjectMetaDataFactory}.
	 * 
	 * @return {@link RawManagedObjectMetaDataFactory}.
	 */
	@SuppressWarnings("unchecked")
	public static RawManagedObjectMetaDataFactory getFactory() {
		return new RawManagedObjectMetaDataImpl(null, null, null, null, -1,
				null, null, null, false, false, null);
	}

	/**
	 * Name of the {@link ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * {@link ManagedObjectSourceConfiguration}.
	 */
	private final ManagedObjectSourceConfiguration<H, ?> managedObjectSourceConfiguration;

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<D, H> managedObjectSource;

	/**
	 * {@link ManagedObjectSourceMetaData} for the {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSourceMetaData<D, H> managedObjectSourceMetaData;

	/**
	 * Default timeout for sourcing the {@link ManagedObject} and asynchronous
	 * operations on the {@link ManagedObject}.
	 */
	private final long defaultTimeout;

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool;

	/**
	 * Sourcing {@link AssetManager}.
	 */
	private final AssetManager sourcingAssetManager;

	/**
	 * Operations {@link AssetManager}.
	 */
	private final AssetManager operationsAssetManager;

	/**
	 * Flag indicating if {@link AsynchronousManagedObject}.
	 */
	private final boolean isAsynchronous;

	/**
	 * Flag indicating if {@link CoordinatingManagedObject}.
	 */
	private final boolean isCoordinating;

	/**
	 * Name of the {@link Work} to recycle the {@link ManagedObject}.
	 */
	private final String recycleWorkName;

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
	 * @param defaultTimeout
	 *            Default timeout for sourcing the {@link ManagedObject} and
	 *            asynchronous operations on the {@link ManagedObject}.
	 * @param managedObjectPool
	 *            {@link ManagedObjectPool}.
	 * @param sourcingAssetManager
	 *            Sourcing {@link AssetManager}.
	 * @param operationsAssetManager
	 *            Operations {@link AssetManager}.
	 * @param isAsynchronous
	 *            Flag indicating if {@link AsynchronousManagedObject}.
	 * @param isCoordinating
	 *            Flag indicating if {@link CoordinatingManagedObject}.
	 * @param recycleWorkName
	 *            Name of the {@link Work} to recycle the {@link ManagedObject}.
	 */
	private RawManagedObjectMetaDataImpl(
			String managedObjectName,
			ManagedObjectSourceConfiguration<H, ?> managedObjectSourceConfiguration,
			ManagedObjectSource<D, H> managedObjectSource,
			ManagedObjectSourceMetaData<D, H> managedObjectSourceMetaData,
			long defaultTimeout, ManagedObjectPool managedObjectPool,
			AssetManager sourcingAssetManager,
			AssetManager operationsAssetManager, boolean isAsynchronous,
			boolean isCoordinating, String recycleWorkName) {
		this.managedObjectName = managedObjectName;
		this.managedObjectSourceConfiguration = managedObjectSourceConfiguration;
		this.managedObjectSource = managedObjectSource;
		this.managedObjectSourceMetaData = managedObjectSourceMetaData;
		this.defaultTimeout = defaultTimeout;
		this.managedObjectPool = managedObjectPool;
		this.sourcingAssetManager = sourcingAssetManager;
		this.operationsAssetManager = operationsAssetManager;
		this.isAsynchronous = isAsynchronous;
		this.isCoordinating = isCoordinating;
		this.recycleWorkName = recycleWorkName;
	}

	/*
	 * ==================== RawManagedObjectMetaDataFactory ==================
	 */

	@Override
	public <d extends Enum<d>, h extends Enum<h>, MS extends ManagedObjectSource<d, h>> RawManagedObjectMetaData<d, h> constructRawManagedObjectMetaData(
			ManagedObjectSourceConfiguration<h, MS> configuration,
			OfficeFloorIssues issues, AssetManagerFactory assetManagerFactory,
			OfficeFloorConfiguration officeFloorConfiguration) {

		// Obtain the managed object source name
		String managedObjectSourceName = configuration
				.getManagedObjectSourceName();
		if (ConstructUtil.isBlank(managedObjectSourceName)) {
			issues.addIssue(AssetType.OFFICE_FLOOR, OfficeFloor.class
					.getSimpleName(), "ManagedObject added without a name");
			return null; // can not carry on
		}

		// Obtain the managed object source
		Class<MS> managedObjectSourceClass = configuration
				.getManagedObjectSourceClass();
		if (managedObjectSourceClass == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"No ManagedObjectSource class provided");
			return null; // can not carry on
		}

		// Instantiate the managed object source
		MS managedObjectSource = ConstructUtil.newInstance(
				managedObjectSourceClass, ManagedObjectSource.class,
				"Managed Object Source '" + managedObjectSourceName + "'",
				AssetType.MANAGED_OBJECT, managedObjectSourceName, issues);
		if (managedObjectSource == null) {
			return null; // can not carry on
		}

		// Create the resource locator
		ResourceLocator resourceLocator = new ClassLoaderResourceLocator();

		// Obtain the properties to initialise the managed object source
		Properties properties = configuration.getProperties();

		// Obtain the managed object builder
		ManagedObjectBuilder<h> managedObjectBuilder = configuration
				.getBuilder();

		// Obtain the managing office for the managed object source
		String managingOfficeName = configuration.getManagingOfficeName();
		if (ConstructUtil.isBlank(managingOfficeName)) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"No managing office specified");
			return null; // can not carry on
		}
		OfficeBuilder managingOfficeBuilder = null;
		for (OfficeConfiguration officeConfiguration : officeFloorConfiguration
				.getOfficeConfiguration()) {
			if (managingOfficeName.equals(officeConfiguration.getOfficeName())) {
				managingOfficeBuilder = officeConfiguration.getBuilder();
			}
		}
		if (managingOfficeBuilder == null) {
			issues
					.addIssue(AssetType.MANAGED_OBJECT,
							managedObjectSourceName,
							"Can not find managing office '"
									+ managingOfficeName + "'");
			return null; // can not carry on
		}

		// Create the context for the managed object source
		ManagedObjectSourceContextImpl<h> context = new ManagedObjectSourceContextImpl<h>(
				managedObjectSourceName, properties, resourceLocator,
				managedObjectBuilder, managingOfficeBuilder);

		try {
			// Initialise the managed object source
			managedObjectSource.init(context);

		} catch (ManagedObjectSourceUnknownPropertyError ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Property '" + ex.getUnkonwnPropertyName()
							+ "' must be specified");
			return null; // can not carry on

		} catch (Throwable ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Failed to initialise "
							+ managedObjectSourceClass.getName(), ex);
			return null; // can not carry on
		}

		// Flag initialising over
		context.flagInitOver();

		// Obtain the meta-data
		ManagedObjectSourceMetaData<d, h> metaData = managedObjectSource
				.getMetaData();
		if (metaData == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Must provide meta-data");
			return null; // can not carry on
		}

		// Create the sourcing asset manager
		AssetManager sourcingAssetManager = assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						managedObjectSourceName, "sourcing", issues);
		if (sourcingAssetManager == null) {
			return null; // can not carry on
		}

		// Obtain managed object type to determine details
		Class<?> managedObjectClass = metaData.getManagedObjectClass();
		if (managedObjectClass == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"No managed object class provided");
			return null; // can not carry on
		}

		// Determine if asynchronous
		boolean isManagedObjectAsynchronous = AsynchronousManagedObject.class
				.isAssignableFrom(managedObjectClass);
		AssetManager operationsAssetManager = null;
		if (isManagedObjectAsynchronous) {
			// Asynchronous so provide operations manager
			operationsAssetManager = assetManagerFactory.createAssetManager(
					AssetType.MANAGED_OBJECT, managedObjectName, "operations",
					issues);
		}

		// Determine if coordinating
		boolean isManagedObjectCoordinating = CoordinatingManagedObject.class
				.isAssignableFrom(managedObjectClass);

		// Obtain the default timeout
		long defaultTimeout = configuration.getDefaultTimeout();
		if (defaultTimeout < 0) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Must not have negative default timeout");
			return null; // can not carry on
		}

		// Obtain the managed object pool
		ManagedObjectPool managedObjectPool = configuration
				.getManagedObjectPool();

		// Obtain the recycle work name
		String recycleWorkName = context.getRecycleWorkName();

		// Return the create raw managed object meta data
		return new RawManagedObjectMetaDataImpl<d, h>(managedObjectSourceName,
				configuration, managedObjectSource, metaData, defaultTimeout,
				managedObjectPool, sourcingAssetManager,
				operationsAssetManager, isManagedObjectAsynchronous,
				isManagedObjectCoordinating, recycleWorkName);
	}

	/*
	 * ==================== RawManagedObjectMetaDataFactory ==================
	 */

	@Override
	public String getManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public ManagedObjectSourceConfiguration<H, ?> getManagedObjectSourceConfiguration() {
		return this.managedObjectSourceConfiguration;
	}

	@Override
	public ManagedObjectSource<D, H> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public ManagedObjectSourceMetaData<D, H> getManagedObjectSourceMetaData() {
		return this.managedObjectSourceMetaData;
	}

	@Override
	public long getDefaultTimeout() {
		return this.defaultTimeout;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.managedObjectPool;
	}

	@Override
	public AssetManager getSourcingAssetManager() {
		return this.sourcingAssetManager;
	}

	@Override
	public AssetManager getOperationsAssetManager() {
		return this.operationsAssetManager;
	}

	@Override
	public boolean isAsynchronous() {
		return this.isAsynchronous;
	}

	@Override
	public boolean isCoordinating() {
		return this.isCoordinating;
	}

	@Override
	public String getRecycleWorkName() {
		return this.recycleWorkName;
	}

}
