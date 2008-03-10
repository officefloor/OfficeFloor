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
package net.officefloor.frame.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.impl.execute.ManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ResourceLocator;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Raw meta-data of the
 * {@link net.officefloor.frame.internal.structure.ManagedObjectMetaData}.
 * 
 * @author Daniel
 */
public class RawManagedObjectMetaData {

	/**
	 * Name of the {@link net.officefloor.frame.api.execute.Work} to clean up
	 * the {@link ManagedObject}.
	 */
	public static final String MANAGED_OBJECT_CLEAN_UP_WORK_NAME = "managedobjectcleanup";

	/**
	 * Creates a {@link RawManagedObjectMetaData}.
	 * 
	 * @param mosConfig
	 *            {@link ManagedObjectSourceConfiguration}.
	 * @param resourceLocator
	 *            {@link ResourceLocator}.
	 * @param rawAssetRegistry
	 *            {@link RawAssetManagerRegistry}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @param officeFrame
	 *            {@link OfficeFrame}.
	 * @return {@link RawManagedObjectMetaData}.
	 * @throws Exception
	 *             If fails to create meta-data.
	 */
	public static RawManagedObjectMetaData createRawManagedObjectMetaData(
			ManagedObjectSourceConfiguration mosConfig,
			ResourceLocator resourceLocator,
			RawAssetManagerRegistry rawAssetRegistry,
			OfficeBuilder officeBuilder, OfficeFrame officeFrame)
			throws Exception {

		// Obtain the managed object name
		String managedObjectName = mosConfig.getManagedObjectName();

		// Create the instance of the managed object source
		ManagedObjectSource<?, ?> managedObjectSource;
		try {
			managedObjectSource = mosConfig.getManagedObjectSourceClass()
					.newInstance();
		} catch (InstantiationException ex) {
			throw new ConfigurationException(ex.getClass().getName() + ": "
					+ ex.getMessage());
		} catch (IllegalAccessException ex) {
			throw new ConfigurationException(ex.getClass().getName() + ": "
					+ ex.getMessage());
		}

		// Obtain the managed object builder
		ManagedObjectBuilder managedObjectBuilder = (ManagedObjectBuilder) mosConfig;

		// Create the context for the Managed Object Source
		ManagedObjectSourceContextImpl context = new ManagedObjectSourceContextImpl(
				managedObjectName, mosConfig.getProperties(), resourceLocator,
				managedObjectBuilder, officeBuilder, officeFrame);

		// Initialise the Managed Object Source
		managedObjectSource.init(context);

		// Flag initialise complete
		context.flagInitOver();

		// Create the Sourcing Manager
		AssetManager sourcingManager = rawAssetRegistry
				.createAssetManager("Source Managed Object - "
						+ managedObjectName);

		// Obtain the default timeout for asynchronous operations
		long defaultTimeout = mosConfig.getDefaultTimeout();

		// Register the raw Managed Object meta-data
		return new RawManagedObjectMetaData(managedObjectName, mosConfig,
				managedObjectSource, sourcingManager, defaultTimeout, mosConfig
						.getManagedObjectPool(), context.getRecycleWorkName(),
				rawAssetRegistry);
	}

	/**
	 * Name of the {@link ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * {@link ManagedObjectSourceConfiguration}.
	 */
	private final ManagedObjectSourceConfiguration mosConfig;

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, ?> managedObjectSource;

	/**
	 * Default timeout for asynchronous operations on the {@link ManagedObject}.
	 */
	private final long timeout;

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool;

	/**
	 * Sourcing {@link AssetManager}.
	 */
	private final AssetManager sourcingManager;

	/**
	 * Name of the {@link net.officefloor.frame.api.execute.Work} to recycle the
	 * {@link ManagedObject}.
	 */
	private final String recycleWorkName;

	/**
	 * Listing of {@link ManagedObjectMetaData} for this
	 * {@link RawManagedObjectMetaData}.
	 */
	private final List<ManagedObjectMetaDataImpl<?>> moMetaData = new LinkedList<ManagedObjectMetaDataImpl<?>>();

	/**
	 * {@link RawAssetManagerRegistry}.
	 */
	private final RawAssetManagerRegistry rawAssetRegistry;

	/**
	 * Initiate detail.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param mosConfig
	 *            {@link ManagedObjectSourceConfiguration}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @param sourcingManager
	 *            Sourcing {@link AssetManager}.
	 * @param timeout
	 *            Default timeout for asynchronous operations on the
	 *            {@link ManagedObject}.
	 * @param recycleWorkName
	 *            Name of the {@link net.officefloor.frame.api.execute.Work} to
	 *            recycle the {@link ManagedObject}.
	 * @param rawAssetRegistry
	 *            {@link RawAssetManagerRegistry}.
	 */
	private RawManagedObjectMetaData(String managedObjectName,
			ManagedObjectSourceConfiguration mosConfig,
			ManagedObjectSource<?, ?> managedObjectSource,
			AssetManager sourcingManager, long timeout,
			ManagedObjectPool managedObjectPool, String recycleWorkName,
			RawAssetManagerRegistry rawAssetRegistry) {
		// Store state
		this.managedObjectName = managedObjectName;
		this.mosConfig = mosConfig;
		this.managedObjectSource = managedObjectSource;
		this.sourcingManager = sourcingManager;
		this.timeout = timeout;
		this.managedObjectPool = managedObjectPool;
		this.recycleWorkName = recycleWorkName;
		this.rawAssetRegistry = rawAssetRegistry;
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
	 * Obtains the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	public ManagedObjectSource<?, ?> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	/**
	 * Obtains the default timeout for the {@link ManagedObject} asynchronous
	 * operations.
	 * 
	 * @return Default timeout for the {@link ManagedObject} asynchronous
	 *         operations.
	 */
	public long getDefaultTimeout() {
		return this.timeout;
	}

	/**
	 * Obtains the {@link ManagedObjectPool}.
	 * 
	 * @return {@link ManagedObjectPool} or <code>null</code> if not pooled.
	 */
	public ManagedObjectPool getManagedObjectPool() {
		return this.managedObjectPool;
	}

	/**
	 * Obtains the name of the {@link net.officefloor.frame.api.execute.Work} to
	 * recycle the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link net.officefloor.frame.api.execute.Work} to
	 *         recycle the {@link ManagedObject}.
	 */
	public String getRecycleWorkName() {
		return this.recycleWorkName;
	}

	/**
	 * Obtains the listing of {@link ManagedObjectMetaData} for this
	 * {@link RawManagedObjectMetaData}.
	 * 
	 * @return Listing of {@link ManagedObjectMetaData} for this
	 *         {@link RawManagedObjectMetaData}.
	 */
	public List<ManagedObjectMetaDataImpl<?>> getManagedObjectMetaData() {
		return this.moMetaData;
	}

	/**
	 * Obtains the {@link ManagedObjectSourceConfiguration} for this
	 * {@link RawManagedObjectMetaData}.
	 * 
	 * @return {@link ManagedObjectSourceConfiguration} for this
	 *         {@link RawManagedObjectMetaData}.
	 */
	public ManagedObjectSourceConfiguration getManagedObjectSourceConfiguration() {
		return this.mosConfig;
	}

	/**
	 * Creates a {@link ManagedObjectMetaData} for this
	 * {@link RawManagedObjectMetaData}.
	 * 
	 * @param timeout
	 *            Timeout of an asynchronous operation by the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            being managed.
	 * @param dependencyMapping
	 *            Mappings for dependencies of this
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	public <D extends Enum<D>> ManagedObjectMetaData<?> createManagedObjectMetaData(
			long timeout, Map<D, Integer> dependencyMapping)
			throws ConfigurationException {

		// Obtain the class of the Managed Object
		Class<? extends ManagedObject> managedObjectClass = this.managedObjectSource
				.getMetaData().getManagedObjectClass();
		if (managedObjectClass == null) {
			throw new ConfigurationException(
					"Managed Object '"
							+ this.managedObjectName
							+ "' is not providing the Managed Object Class within its meta-data");
		}

		// Determine if managed object is asynchronous
		boolean isAsynchronous = AsynchronousManagedObject.class
				.isAssignableFrom(managedObjectClass);

		// Create the Operations Manager (if asynchronous)
		AssetManager operationsManager = null;
		if (isAsynchronous) {
			// Asynchronous thus requires Operations Manager
			operationsManager = this.rawAssetRegistry
					.createAssetManager("Operations on Managed Object - "
							+ this.managedObjectName);
		}

		// Determine if co-ordinating managed object
		boolean isCoordinating = CoordinatingManagedObject.class
				.isAssignableFrom(managedObjectClass);

		// Create the managed object meta-data
		ManagedObjectMetaDataImpl<D> metaData = new ManagedObjectMetaDataImpl<D>(
				this.managedObjectSource, this.managedObjectPool,
				this.sourcingManager, isAsynchronous, operationsManager,
				isCoordinating, dependencyMapping, timeout);

		// Register the meta-data
		this.moMetaData.add(metaData);

		// Return the meta-data
		return metaData;
	}

}
