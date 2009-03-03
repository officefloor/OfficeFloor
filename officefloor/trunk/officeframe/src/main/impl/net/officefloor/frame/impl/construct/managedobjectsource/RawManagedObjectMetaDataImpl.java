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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.asset.AssetManagerFactory;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedobject.HandlerContextImpl;
import net.officefloor.frame.internal.configuration.HandlerConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
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
				null, null, null, false, false, null, null, null);
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
	 * Name of the {@link Office} managing this {@link ManagedObject}.
	 */
	private final String managingOfficeName;

	/**
	 * Name to bind the {@link ManagedObject} within the {@link ProcessState} of
	 * the managing {@link Office}.
	 */
	private final String managingOfficeProcessBoundManagedObjectName;

	/**
	 * Name of the {@link Work} to recycle the {@link ManagedObject}.
	 */
	private final String recycleWorkName;

	/**
	 * {@link Handler} instances for the {@link ManagedObjectSource}.
	 */
	private Map<H, Handler<?>> handlers;

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
	 * @param managingOfficeName
	 *            Name of the {@link Office} managing this.
	 * @param managingOfficeProcessBoundManagedObjectName
	 *            Name to bind the {@link ManagedObject} within the
	 *            {@link ProcessState} of the managing {@link Office}.
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
			boolean isCoordinating, String managingOfficeName,
			String managingOfficeProcessBoundManagedObjectName,
			String recycleWorkName) {
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
		this.managingOfficeName = managingOfficeName;
		this.managingOfficeProcessBoundManagedObjectName = managingOfficeProcessBoundManagedObjectName;
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
		ManagingOfficeConfiguration managingOfficeConfiguration = configuration
				.getManagingOfficeConfiguration();
		if (managingOfficeConfiguration == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"No managing office configuration");
			return null; // can not carry on
		}
		String managingOfficeName = managingOfficeConfiguration.getOfficeName();
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

		// Determine if the managed object requires managing
		Class<h> handlerClass = metaData.getHandlerKeys();
		String requiredManagingOfficeName = null;
		String requiredManagingOfficeProcessBoundManagedObjectName = null;
		if ((handlerClass != null)
				&& (handlerClass.getEnumConstants().length > 0)) {
			// Has handlers, so requires to be managed
			requiredManagingOfficeName = managingOfficeName;
			requiredManagingOfficeProcessBoundManagedObjectName = managingOfficeConfiguration
					.getProcessBoundManagedObjectName();
			if (ConstructUtil
					.isBlank(requiredManagingOfficeProcessBoundManagedObjectName)) {
				issues
						.addIssue(
								AssetType.MANAGED_OBJECT,
								managedObjectSourceName,
								"Managed Object Source requires managing and did not provide process bound name within Office");
				return null; // can not carry on
			}
		}

		// Obtain the recycle work name
		String recycleWorkName = context.getRecycleWorkName();

		// Return the created raw managed object meta data
		return new RawManagedObjectMetaDataImpl<d, h>(managedObjectSourceName,
				configuration, managedObjectSource, metaData, defaultTimeout,
				managedObjectPool, sourcingAssetManager,
				operationsAssetManager, isManagedObjectAsynchronous,
				isManagedObjectCoordinating, requiredManagingOfficeName,
				requiredManagingOfficeProcessBoundManagedObjectName,
				recycleWorkName);
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
	public String getManagingOfficeName() {
		return this.managingOfficeName;
	}

	@Override
	public String getManagingOfficeProcessBoundManagedObjectName() {
		return this.managingOfficeProcessBoundManagedObjectName;
	}

	@Override
	public String getRecycleWorkName() {
		return this.recycleWorkName;
	}

	@Override
	public void manageByOffice(OfficeMetaData officeMetaData,
			int indexOfManagedObjectInProcessState, OfficeFloorIssues issues) {

		// Obtain the handler configurations
		HandlerConfiguration<H, ?>[] handlerConfigurations = this.managedObjectSourceConfiguration
				.getHandlerConfiguration();

		// Obtain the handler class providing the keys
		Class<H> handlerKeys = this.managedObjectSourceMetaData
				.getHandlerKeys();
		if (handlerKeys == null) {
			// Ensure no handlers configured
			if (handlerConfigurations.length > 0) {
				issues
						.addIssue(
								AssetType.MANAGED_OBJECT,
								this.managedObjectName,
								"Managed Object Source meta-data specifies no handlers but handlers configured for it");
			}

			// No handlers
			this.handlers = Collections.emptyMap();
			return;
		}

		// Obtain the configuration for the handlers
		Map<H, HandlerConfiguration<H, ?>> handlerConfigurationMap = new EnumMap<H, HandlerConfiguration<H, ?>>(
				handlerKeys);
		for (HandlerConfiguration<H, ?> handlerConfiguration : handlerConfigurations) {

			// Obtain the handler key
			H handlerKey = handlerConfiguration.getHandlerKey();
			if (handlerKey == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						this.managedObjectName, "Handler Key not provided");
				continue; // must have key
			}
			if (!handlerKeys.isInstance(handlerKey)) {
				issues
						.addIssue(
								AssetType.MANAGED_OBJECT,
								this.managedObjectName,
								"Handler key "
										+ handlerKey
										+ " is not of type specified by Managed Object Source meta-data");
				continue; // must be of right type
			}

			// Load the hander configuration
			handlerConfigurationMap.put(handlerKey, handlerConfiguration);
		}

		// Create the handlers
		Map<H, Handler<?>> handlers = new EnumMap<H, Handler<?>>(handlerKeys);
		for (H handlerKey : handlerKeys.getEnumConstants()) {

			// Obtain the handler configuration
			HandlerConfiguration<H, ?> configuration = handlerConfigurationMap
					.get(handlerKey);
			if (configuration == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						this.managedObjectName,
						"No handler configured for key " + handlerKey);
				continue; // no configuration, no handler
			}

			// Create the Handler
			Handler<?> handler = this.createHandler(configuration,
					officeMetaData, indexOfManagedObjectInProcessState, issues);
			if (handler == null) {
				continue; // could not create handler
			}

			// Register the handler
			handlers.put(handlerKey, handler);
		}

		// Specify the handlers
		this.handlers = handlers;
	}

	/**
	 * Create the {@link Handler}.
	 * 
	 * @param configuration
	 *            {@link HandlerConfiguration}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param indexOfManagedObjectInProcessState
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ProcessState} of the {@link Office}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link Handler} or <code>null</code> if could not create.
	 */
	private <F extends Enum<F>> Handler<F> createHandler(
			HandlerConfiguration<H, F> configuration,
			OfficeMetaData officeMetaData,
			int indexOfManagedObjectInProcessState, OfficeFloorIssues issues) {

		// Obtain the handler factory
		HandlerFactory<F> handlerFactory = configuration.getHandlerFactory();
		if (handlerFactory == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, this.managedObjectName,
					"Handler Factory must be provided for handler key "
							+ configuration.getHandlerKey());
			return null; // must have factory
		}

		// Create the handler
		Handler<F> handler = handlerFactory.createHandler();
		if (handler == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, this.managedObjectName,
					"Handler Factory must create a Handler");
			return null; // must create handler
		}

		// TODO obtain the process links
		FlowMetaData<?>[] processLinks = null;

		// Create the handler context
		HandlerContext<F> context = new HandlerContextImpl<F>(
				indexOfManagedObjectInProcessState, processLinks,
				officeMetaData);

		// Provide the handler its context
		try {
			handler.setHandlerContext(context);
		} catch (Throwable ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, this.managedObjectName,
					"Failed to provide Handler Context to Handler of key "
							+ configuration.getHandlerKey(), ex);
		}

		// Return the handler
		return handler;
	}

	@Override
	public Map<H, Handler<?>> getHandlers() {
		return this.handlers;
	}

}