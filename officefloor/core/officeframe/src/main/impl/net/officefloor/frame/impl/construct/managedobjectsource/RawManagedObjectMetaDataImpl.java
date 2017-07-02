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

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.impl.execute.pool.ManagedObjectPoolContextImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectPoolConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
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
public class RawManagedObjectMetaDataImpl<D extends Enum<D>, F extends Enum<F>>
		implements RawManagedObjectMetaDataFactory, RawManagedObjectMetaData<D, F> {

	/**
	 * Obtains the {@link RawManagedObjectMetaDataFactory}.
	 * 
	 * @return {@link RawManagedObjectMetaDataFactory}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static RawManagedObjectMetaDataFactory getFactory() {
		return new RawManagedObjectMetaDataImpl(null, null, null, null, -1, null, null, null, false, false, false,
				false, null);
	}

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
	private final ManagedObjectSource<D, F> managedObjectSource;

	/**
	 * {@link ManagedObjectSourceMetaData} for the {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSourceMetaData<D, F> managedObjectSourceMetaData;

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
	private final RawManagingOfficeMetaDataImpl<F> rawManagingOfficeMetaData;

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
	private RawManagedObjectMetaDataImpl(String managedObjectName,
			ManagedObjectSourceConfiguration<F, ?> managedObjectSourceConfiguration,
			ManagedObjectSource<D, F> managedObjectSource,
			ManagedObjectSourceMetaData<D, F> managedObjectSourceMetaData, long timeout,
			ManagedObjectPool managedObjectPool, ThreadCompletionListener[] threadCompletionListeners,
			Class<?> objectType, boolean isProcessAware, boolean isNameAware, boolean isAsynchronous,
			boolean isCoordinating, RawManagingOfficeMetaDataImpl<F> rawManagingOfficeMetaData) {
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

	/*
	 * ==================== RawManagedObjectMetaDataFactory ==================
	 */

	@Override
	public <d extends Enum<d>, h extends Enum<h>, MS extends ManagedObjectSource<d, h>> RawManagedObjectMetaData<d, h> constructRawManagedObjectMetaData(
			ManagedObjectSourceConfiguration<h, MS> configuration, SourceContext sourceContext,
			OfficeFloorIssues issues, OfficeFloorConfiguration officeFloorConfiguration) {

		// Obtain the managed object source name
		String managedObjectSourceName = configuration.getManagedObjectSourceName();
		if (ConstructUtil.isBlank(managedObjectSourceName)) {
			issues.addIssue(AssetType.OFFICE_FLOOR, OfficeFloor.class.getSimpleName(),
					"ManagedObject added without a name");
			return null; // can not carry on
		}

		// Attempt to obtain the managed object source
		MS managedObjectSource = configuration.getManagedObjectSource();
		if (managedObjectSource == null) {
			// No instance, so by managed object source class
			Class<MS> managedObjectSourceClass = configuration.getManagedObjectSourceClass();
			if (managedObjectSourceClass == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						"No ManagedObjectSource class provided");
				return null; // can not carry on
			}

			// Instantiate the managed object source
			managedObjectSource = ConstructUtil.newInstance(managedObjectSourceClass, ManagedObjectSource.class,
					"Managed Object Source '" + managedObjectSourceName + "'", AssetType.MANAGED_OBJECT,
					managedObjectSourceName, issues);
			if (managedObjectSource == null) {
				return null; // can not carry on
			}
		}

		// Obtain the properties to initialise the managed object source
		SourceProperties properties = configuration.getProperties();

		// Obtain the managing office for the managed object source
		ManagingOfficeConfiguration<h> managingOfficeConfiguration = configuration.getManagingOfficeConfiguration();
		if (managingOfficeConfiguration == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName, "No managing office configuration");
			return null; // can not carry on
		}
		String officeName = managingOfficeConfiguration.getOfficeName();
		if (ConstructUtil.isBlank(officeName)) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName, "No managing office specified");
			return null; // can not carry on
		}
		OfficeBuilder officeBuilder = null;
		for (OfficeConfiguration officeConfiguration : officeFloorConfiguration.getOfficeConfiguration()) {
			if (officeName.equals(officeConfiguration.getOfficeName())) {
				officeBuilder = officeConfiguration.getBuilder();
			}
		}
		if (officeBuilder == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Can not find managing office '" + officeName + "'");
			return null; // can not carry on
		}

		// Obtain the managing office builder
		ManagingOfficeBuilder<h> managingOfficeBuilder = managingOfficeConfiguration.getBuilder();

		// Create the context for the managed object source
		ManagedObjectSourceContextImpl<h> context = new ManagedObjectSourceContextImpl<h>(false,
				managedObjectSourceName, managingOfficeConfiguration, properties, sourceContext, managingOfficeBuilder,
				officeBuilder);

		try {
			// Initialise the managed object source
			managedObjectSource.init(context);

		} catch (UnknownPropertyError ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Property '" + ex.getUnknownPropertyName() + "' must be specified");
			return null; // can not carry on

		} catch (UnknownClassError ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Can not load class '" + ex.getUnknownClassName() + "'");
			return null; // can not carry on

		} catch (UnknownResourceError ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Can not obtain resource at location '" + ex.getUnknownResourceLocation() + "'");
			return null; // can not carry on

		} catch (Throwable ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Failed to initialise " + managedObjectSource.getClass().getName(), ex);
			return null; // can not carry on
		}

		// Flag initialising over
		context.flagInitOver();

		// Obtain the meta-data
		ManagedObjectSourceMetaData<d, h> metaData = managedObjectSource.getMetaData();
		if (metaData == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName, "Must provide meta-data");
			return null; // can not carry on
		}

		// Obtain the object type
		Class<?> objectType = metaData.getObjectClass();
		if (objectType == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName, "No object type provided");
			return null; // can not carry on
		}

		// Obtain managed object type to determine details
		Class<?> managedObjectClass = metaData.getManagedObjectClass();
		if (managedObjectClass == null) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName, "No managed object class provided");
			return null; // can not carry on
		}

		// Determine if process aware, name aware, asynchronous, coordinating
		boolean isManagedObjectProcessAware = ProcessAwareManagedObject.class.isAssignableFrom(managedObjectClass);
		boolean isManagedObjectNameAware = NameAwareManagedObject.class.isAssignableFrom(managedObjectClass);
		boolean isManagedObjectAsynchronous = AsynchronousManagedObject.class.isAssignableFrom(managedObjectClass);
		boolean isManagedObjectCoordinating = CoordinatingManagedObject.class.isAssignableFrom(managedObjectClass);

		// Obtain the timeout
		long timeout = configuration.getTimeout();
		if (timeout < 0) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName, "Must not have negative timeout");
			return null; // can not carry on
		}
		if ((isManagedObjectAsynchronous) && (timeout <= 0)) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Non-zero timeout must be provided for " + AsynchronousManagedObject.class.getSimpleName());
			return null; // can not carry on
		}

		// Obtain the flow meta-data
		ManagedObjectFlowMetaData<h>[] flowMetaDatas = metaData.getFlowMetaData();

		// Requires input configuration if requires flows
		InputManagedObjectConfiguration<?> inputConfiguration = null;
		if (RawManagingOfficeMetaDataImpl.isRequireFlows(flowMetaDatas)) {
			// Requires flows, so must have input configuration
			inputConfiguration = managingOfficeConfiguration.getInputManagedObjectConfiguration();
			if (inputConfiguration == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						"Must provide Input configuration as Managed Object Source requires flows");
				return null; // can not carry on
			}
		}

		// Obtain managed object pool and possible thread completion listeners
		ManagedObjectPool managedObjectPool = null;
		ThreadCompletionListener[] threadCompletionListeners = null;
		ManagedObjectPoolConfiguration managedObjectPoolConfiguration = configuration
				.getManagedObjectPoolConfiguration();
		if (managedObjectPoolConfiguration != null) {

			// Create the managed object pool for the managed object source
			ManagedObjectPoolFactory poolFactory = managedObjectPoolConfiguration.getManagedObjectPoolFactory();
			ManagedObjectPoolContext poolContext = new ManagedObjectPoolContextImpl(managedObjectSource);
			managedObjectPool = poolFactory.createManagedObjectPool(poolContext);

			// Create the thread completion listeners
			ThreadCompletionListenerFactory[] threadCompletionListenerFactories = managedObjectPoolConfiguration
					.getThreadCompletionListenerFactories();
			threadCompletionListeners = new ThreadCompletionListener[threadCompletionListenerFactories.length];
			for (int i = 0; i < threadCompletionListeners.length; i++) {
				threadCompletionListeners[i] = threadCompletionListenerFactories[i]
						.createThreadCompletionListener(managedObjectPool);
			}
		}

		// Obtain the recycle function name
		String recycleFunctionName = context.getRecycleFunctionName();

		// Create the raw managing office meta-data
		RawManagingOfficeMetaDataImpl<h> rawManagingOfficeMetaData = new RawManagingOfficeMetaDataImpl<h>(officeName,
				recycleFunctionName, inputConfiguration, flowMetaDatas, managingOfficeConfiguration);

		// Created raw managed object meta-data
		RawManagedObjectMetaDataImpl<d, h> rawMoMetaData = new RawManagedObjectMetaDataImpl<d, h>(
				managedObjectSourceName, configuration, managedObjectSource, metaData, timeout, managedObjectPool,
				threadCompletionListeners, objectType, isManagedObjectProcessAware, isManagedObjectNameAware,
				isManagedObjectAsynchronous, isManagedObjectCoordinating, rawManagingOfficeMetaData);

		// Make raw managed object available to the raw managing office
		rawManagingOfficeMetaData.setRawManagedObjectMetaData(rawMoMetaData);

		// Return the raw managed object meta-data
		return rawMoMetaData;
	}

	/*
	 * ==================== RawManagedObjectMetaData ===========================
	 */

	@Override
	public String getManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public ManagedObjectSourceConfiguration<F, ?> getManagedObjectSourceConfiguration() {
		return this.managedObjectSourceConfiguration;
	}

	@Override
	public ManagedObjectSource<D, F> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public ManagedObjectSourceMetaData<D, F> getManagedObjectSourceMetaData() {
		return this.managedObjectSourceMetaData;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.managedObjectPool;
	}

	@Override
	public ThreadCompletionListener[] getThreadCompletionListeners() {
		return this.threadCompletionListeners;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public RawManagingOfficeMetaData<F> getRawManagingOfficeMetaData() {
		return this.rawManagingOfficeMetaData;
	}

	@Override
	public ManagedObjectMetaData<D> createManagedObjectMetaData(AssetType assetType, String assetName,
			RawBoundManagedObjectMetaData boundMetaData, int instanceIndex,
			RawBoundManagedObjectInstanceMetaData<D> boundInstanceMetaData, ManagedObjectIndex[] dependencyMappings,
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
		ManagedObjectMetaDataImpl<D> moMetaData = new ManagedObjectMetaDataImpl<D>(boundName, this.objectType,
				instanceIndex, this.managedObjectSource, this.managedObjectPool, this.isProcessAware, this.isNameAware,
				sourcingAssetManager, this.isAsynchronous, operationsAssetManager, this.isCoordinating,
				dependencyMappings, this.timeout, governanceMetaData);

		// Have the managed object managed by its managing office
		this.rawManagingOfficeMetaData.manageManagedObject(moMetaData);

		// Return the managed object meta-data
		return moMetaData;
	}

}