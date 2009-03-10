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
import java.util.HashMap;
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
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.flow.FlowMetaDataImpl;
import net.officefloor.frame.impl.execute.managedobject.HandlerContextImpl;
import net.officefloor.frame.internal.configuration.HandlerConfiguration;
import net.officefloor.frame.internal.configuration.HandlerFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawOfficeManagingManagedObjectMetaData;
import net.officefloor.frame.internal.construct.TaskMetaDataLocator;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
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
	 * {@link RawOfficeManagingManagedObjectMetaData}.
	 */
	private final RawOfficeManagingManagedObjectMetaData rawOfficeManagingManagedObjectMetaData;

	/**
	 * Class providing the {@link Handler} keys for the {@link ManagedObject}.
	 */
	private final Class<H> handlerKeysClass;

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
	 * @param rawOfficeManagingManagedObjectMetaData
	 *            {@link RawOfficeManagingManagedObjectMetaData}.
	 * @param handlerKeysClass
	 *            Class providing the {@link Handler} keys for the
	 *            {@link ManagedObject}.
	 * @param recycleWorkName
	 *            Name of the {@link Work} to recycle the {@link ManagedObject}.
	 */
	private RawManagedObjectMetaDataImpl(
			String managedObjectName,
			ManagedObjectSourceConfiguration<H, ?> managedObjectSourceConfiguration,
			ManagedObjectSource<D, H> managedObjectSource,
			ManagedObjectSourceMetaData<D, H> managedObjectSourceMetaData,
			long defaultTimeout,
			ManagedObjectPool managedObjectPool,
			AssetManager sourcingAssetManager,
			AssetManager operationsAssetManager,
			boolean isAsynchronous,
			boolean isCoordinating,
			RawOfficeManagingManagedObjectMetaData rawOfficeManagingManagedObjectMetaData,
			Class<H> handlerKeysClass, String recycleWorkName) {
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
		this.rawOfficeManagingManagedObjectMetaData = rawOfficeManagingManagedObjectMetaData;
		this.handlerKeysClass = handlerKeysClass;
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
					"Property '" + ex.getUnknownPropertyName()
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
					AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"operations", issues);
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

		// Determine if the managed object requires binding to process of office
		Class<h> handlerKeysClass = metaData.getHandlerKeys();
		String processBoundManagedObjectName = null;
		if ((handlerKeysClass != null)
				&& (handlerKeysClass.getEnumConstants().length > 0)) {
			// Has handlers, so requires to be bound to process of office
			processBoundManagedObjectName = managingOfficeConfiguration
					.getProcessBoundManagedObjectName();
			if (ConstructUtil.isBlank(processBoundManagedObjectName)) {
				issues
						.addIssue(
								AssetType.MANAGED_OBJECT,
								managedObjectSourceName,
								"Must specify the process bound name as Managed Object Source requires handlers");
				return null; // can not carry on
			}
		}
		RawOfficeManagingManagedObjectMetaData rawOfficeManagingManagedObjectMetaData = new RawOfficeManagingManagedObjectMetaDataImpl(
				managingOfficeName, processBoundManagedObjectName);

		// Obtain the managed object pool
		ManagedObjectPool managedObjectPool = configuration
				.getManagedObjectPool();

		// Obtain the recycle work name
		String recycleWorkName = context.getRecycleWorkName();

		// Return the created raw managed object meta data
		return new RawManagedObjectMetaDataImpl<d, h>(managedObjectSourceName,
				configuration, managedObjectSource, metaData, defaultTimeout,
				managedObjectPool, sourcingAssetManager,
				operationsAssetManager, isManagedObjectAsynchronous,
				isManagedObjectCoordinating,
				rawOfficeManagingManagedObjectMetaData, handlerKeysClass,
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
	public RawOfficeManagingManagedObjectMetaData getManagingOfficeMetaData() {
		return this.rawOfficeManagingManagedObjectMetaData;
	}

	@Override
	public String getRecycleWorkName() {
		return this.recycleWorkName;
	}

	@Override
	public void manageByOffice(TaskMetaDataLocator taskLocator,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues) {

		// Obtain the handler configurations
		HandlerConfiguration<H, ?>[] handlerConfigurations = this.managedObjectSourceConfiguration
				.getHandlerConfiguration();

		// Obtain the handler class providing the keys
		if ((this.handlerKeysClass == null)
				|| (this.handlerKeysClass.getEnumConstants().length == 0)) {
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

		// Obtain the office meta-data
		OfficeMetaData officeMetaData = taskLocator.getOfficeMetaData();

		// Obtain map of process managed object name to process bound index
		ManagedObjectMetaData<?>[] moMetaDatas = officeMetaData
				.getProcessMetaData().getManagedObjectMetaData();
		Map<String, Integer> processMoNameToIndex = new HashMap<String, Integer>();
		for (int i = 0; i < moMetaDatas.length; i++) {
			processMoNameToIndex.put(
					moMetaDatas[i].getBoundManagedObjectName(), i);
		}

		// Obtain the index of the managed object in the office
		String processBoundName = this.rawOfficeManagingManagedObjectMetaData
				.getProcessBoundName();
		Integer processBoundIndex = processMoNameToIndex.get(processBoundName);
		if (processBoundIndex == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, this.managedObjectName,
					"Managed Object Source by process bound name '"
							+ processBoundName + "' not managed by Office "
							+ officeMetaData.getOfficeName());

			// No handlers
			this.handlers = Collections.emptyMap();
			return; // managed object not in office
		}

		// Obtain the configuration for the handlers
		Map<H, HandlerConfiguration<H, ?>> handlerConfigurationMap = new EnumMap<H, HandlerConfiguration<H, ?>>(
				this.handlerKeysClass);
		for (HandlerConfiguration<H, ?> handlerConfiguration : handlerConfigurations) {

			// Obtain the handler key
			H handlerKey = handlerConfiguration.getHandlerKey();
			if (handlerKey == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						this.managedObjectName, "Handler Key not provided");
				continue; // must have key
			}
			if (!this.handlerKeysClass.isInstance(handlerKey)) {
				issues
						.addIssue(
								AssetType.MANAGED_OBJECT,
								this.managedObjectName,
								"Handler key "
										+ handlerKey
										+ " is not of type specified by Managed Object Source meta-data ("
										+ this.handlerKeysClass.getName() + ")");
				continue; // must be of right type
			}

			// Load the hander configuration
			handlerConfigurationMap.put(handlerKey, handlerConfiguration);
		}

		// Create the handlers
		Map<H, Handler<?>> handlers = new EnumMap<H, Handler<?>>(
				this.handlerKeysClass);
		for (H handlerKey : this.handlerKeysClass.getEnumConstants()) {

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
			Handler<?> handler = this.createHandler(handlerKey, configuration,
					officeMetaData, taskLocator, assetManagerFactory,
					processBoundIndex.intValue(), issues);
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
	 * @param handlerKey
	 *            Key identifying the {@link Handler}.
	 * @param configuration
	 *            {@link HandlerConfiguration}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param taskLocator
	 *            {@link TaskMetaDataLocator}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param indexOfManagedObjectInProcessState
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ProcessState} of the {@link Office}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link Handler} or <code>null</code> if could not create.
	 */
	private <F extends Enum<F>> Handler<F> createHandler(H handlerKey,
			HandlerConfiguration<H, F> configuration,
			OfficeMetaData officeMetaData, TaskMetaDataLocator taskLocator,
			AssetManagerFactory assetManagerFactory,
			int indexOfManagedObjectInProcessState, OfficeFloorIssues issues) {

		// Obtain the handler factory
		HandlerFactory<F> handlerFactory = configuration.getHandlerFactory();
		if (handlerFactory == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, this.managedObjectName,
					"Handler Factory must be provided for handler key "
							+ handlerKey);
			return null; // must have factory
		}

		// Create the handler
		Handler<F> handler;
		try {
			handler = handlerFactory.createHandler();
		} catch (Throwable ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, this.managedObjectName,
					"Handler Factory failed creating the Handler (handler key "
							+ handlerKey + ")", ex);
			return null; // must create handler
		}
		if (handler == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, this.managedObjectName,
					"Handler Factory must create a Handler (handler key "
							+ handlerKey + ")");
			return null; // must create handler
		}

		// Obtain the process links
		HandlerFlowConfiguration<F>[] flowConfigurations = configuration
				.getLinkedProcessConfiguration();
		FlowMetaData<?>[] processLinks = new FlowMetaData[flowConfigurations.length];
		for (int i = 0; i < processLinks.length; i++) {
			HandlerFlowConfiguration<F> flowConfiguration = flowConfigurations[i];

			// Obtain the flow name
			String flowName = flowConfiguration.getFlowName();
			if (ConstructUtil.isBlank(flowName)) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						this.managedObjectName,
						"No flow name provided for flow " + i + " of handler "
								+ handlerKey);
				return null; // can not link in flow
			}

			// Obtain the flow key
			F flowKey = flowConfiguration.getFlowKey();
			if (flowKey != null) {
				// Ensure the flow key's ordinal value matches index
				if (flowKey.ordinal() != i) {
					issues.addIssue(AssetType.MANAGED_OBJECT,
							this.managedObjectName,
							"Flow keys are out of sync for handler "
									+ handlerKey);
					return null; // can not link in flow
				}
			}

			// Obtain the flow task
			TaskNodeReference flowTaskReference = flowConfiguration
					.getTaskNodeReference();
			if (flowTaskReference == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT,
						this.managedObjectName,
						"No task reference provided on flow " + flowName
								+ " for handler " + handlerKey);
				return null; // can not link in flow
			}

			// Obtain the task meta-data
			TaskMetaData<?, ?, ?, ?> flowTask = ConstructUtil.getTaskMetaData(
					flowTaskReference, taskLocator, issues,
					AssetType.MANAGED_OBJECT, this.managedObjectName, "flow "
							+ flowName + " of handler " + handlerKey, true);
			if (flowTask == null) {
				return null; // can not link in flow
			}

			// Create the asset manager for the flow
			AssetManager flowAssetManager = assetManagerFactory
					.createAssetManager(AssetType.MANAGED_OBJECT,
							this.managedObjectName, "Handler " + handlerKey
									+ " Flow " + i, issues);

			// Create and register the flow meta-data
			FlowMetaData<?> flowMetaData = this.newFlowMetaData(flowTask,
					flowAssetManager);
			processLinks[i] = flowMetaData;
		}

		// Create the handler context
		HandlerContext<F> context = new HandlerContextImpl<F>(
				indexOfManagedObjectInProcessState, processLinks,
				officeMetaData);

		// Provide the handler its context
		try {
			handler.setHandlerContext(context);
		} catch (Throwable ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, this.managedObjectName,
					"Failed to set Handler Context for handler " + handlerKey,
					ex);
			return null; // must successfully set context
		}

		// Return the handler
		return handler;
	}

	/**
	 * Creates a new {@link FlowMetaData} for a {@link Handler} {@link Flow}.
	 * 
	 * @param taskMetaData
	 *            {@link TaskMetaData} for the {@link FlowMetaData}.
	 * @param assetManager
	 *            {@link AssetManager}.
	 * @return {@link FlowMetaData}.
	 */
	private <W extends Work> FlowMetaData<W> newFlowMetaData(
			TaskMetaData<?, W, ?, ?> taskMetaData, AssetManager assetManager) {
		return new FlowMetaDataImpl<W>(
				FlowInstigationStrategyEnum.ASYNCHRONOUS, taskMetaData,
				assetManager);
	}

	@Override
	public Map<H, Handler<?>> getHandlers() {
		return this.handlers;
	}

	/**
	 * {@link RawOfficeManagingManagedObjectMetaData} implementation.
	 */
	private class RawOfficeManagingManagedObjectMetaDataImpl implements
			RawOfficeManagingManagedObjectMetaData {

		/**
		 * Name of the managing {@link Office}.
		 */
		private final String managingOfficeName;

		/**
		 * {@link ProcessState} bound name for the {@link ManagedObject} within
		 * the {@link Office}.
		 */
		private final String processBoundName;

		/**
		 * Initialise.
		 * 
		 * @param managingOfficeName
		 *            Name of the managing {@link Office}.
		 * @param processBoundName
		 *            {@link ProcessState} bound name for the
		 *            {@link ManagedObject} within the {@link Office}.
		 */
		public RawOfficeManagingManagedObjectMetaDataImpl(
				String managingOfficeName, String processBoundName) {
			this.managingOfficeName = managingOfficeName;
			this.processBoundName = processBoundName;
		}

		/*
		 * =========== RawOfficeManagingManagedObjectMetaData =================
		 */

		@Override
		public String getManagingOfficeName() {
			return this.managingOfficeName;
		}

		@Override
		public String getProcessBoundName() {
			return this.processBoundName;
		}

		@Override
		public RawManagedObjectMetaData<?, ?> getRawManagedObjectMetaData() {
			return RawManagedObjectMetaDataImpl.this;
		}
	}

}