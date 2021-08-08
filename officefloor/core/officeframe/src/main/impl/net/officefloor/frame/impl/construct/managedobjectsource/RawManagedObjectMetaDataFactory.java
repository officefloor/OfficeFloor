/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.construct.managedobjectsource;

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.function.ManagedObjectFunctionEnhancer;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecutionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.OfficeFloorIssueTarget;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.pool.ManagedObjectPoolContextImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionInvocation;
import net.officefloor.frame.internal.configuration.ManagedObjectPoolConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectServiceReady;

/**
 * Factory for the creation of {@link RawManagedObjectMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagedObjectMetaDataFactory {

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext;

	/**
	 * {@link OfficeFloorConfiguration}.
	 */
	private final OfficeFloorConfiguration officeFloorConfiguration;

	/**
	 * Instantiate.
	 * 
	 * @param sourceContext            {@link SourceContext}.
	 * @param officeFloorConfiguration {@link OfficeFloorConfiguration}.
	 */
	public RawManagedObjectMetaDataFactory(SourceContext sourceContext,
			OfficeFloorConfiguration officeFloorConfiguration) {
		this.sourceContext = sourceContext;
		this.officeFloorConfiguration = officeFloorConfiguration;
	}

	/**
	 * Creates the {@link RawManagedObjectMetaData}.
	 * 
	 * @param <d>             Dependency key type.
	 * @param <h>             {@link Flow} key type.
	 * @param <MS>            {@link ManagedObjectSource} type.
	 * @param configuration   {@link ManagedObjectSourceConfiguration}.
	 * @param startupNotify   Object to notify on start up completion.
	 * @param officeFloorName Name of the {@link OfficeFloor}.
	 * @param issues          {@link OfficeFloorIssues}.
	 * @return {@link RawManagedObjectMetaData} or <code>null</code> if issue.
	 */
	public <d extends Enum<d>, h extends Enum<h>, MS extends ManagedObjectSource<d, h>> RawManagedObjectMetaData<d, h> constructRawManagedObjectMetaData(
			ManagedObjectSourceConfiguration<h, MS> configuration, Object startupNotify, String officeFloorName,
			OfficeFloorIssues issues) {

		// Obtain the managed object source name
		String managedObjectSourceName = configuration.getManagedObjectSourceName();
		if (ConstructUtil.isBlank(managedObjectSourceName)) {
			issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName, "ManagedObject added without a name");
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
		for (OfficeConfiguration officeConfiguration : this.officeFloorConfiguration.getOfficeConfiguration()) {
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

		// Obtain the additional profiles
		String[] additionalProfiles = configuration.getAdditionalProfiles();

		// Create the context for the managed object source
		ManagedObjectSourceContextImpl<h> context = new ManagedObjectSourceContextImpl<h>(managedObjectSourceName,
				false, managedObjectSourceName, managingOfficeConfiguration, additionalProfiles, properties,
				this.sourceContext, managingOfficeBuilder, officeBuilder, startupNotify);

		// Initialise the managed object source and obtain meta-data
		ManagedObjectSourceMetaData<d, h> metaData;
		try {
			// Initialise the managed object source
			metaData = managedObjectSource.init(context);

		} catch (AbstractSourceError ex) {
			ex.addIssue(new OfficeFloorIssueTarget(issues, AssetType.MANAGED_OBJECT, managedObjectSourceName));
			return null; // can not carry on

		} catch (Throwable ex) {
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Failed to initialise " + managedObjectSource.getClass().getName(), ex);
			return null; // can not carry on
		}

		// Obtain the function enhancers
		ManagedObjectFunctionEnhancer[] enhancers = configuration.getManagedObjectFunctionEnhancers();

		// Flag initialising over
		String[] contextIssues = context.flagInitOver(enhancers);
		if (contextIssues.length > 0) {
			for (String contextIssue : contextIssues) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName, contextIssue);
			}
			return null; // can not carry on
		}

		// Ensure have meta-data
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

		// Determine if context aware, asynchronous, coordinating
		boolean isManagedObjectContextAware = ContextAwareManagedObject.class.isAssignableFrom(managedObjectClass);
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
		if (RawManagingOfficeMetaData.isRequireFlows(flowMetaDatas)) {
			// Requires flows, so must have input configuration
			inputConfiguration = managingOfficeConfiguration.getInputManagedObjectConfiguration();
			if (inputConfiguration == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						"Must provide Input configuration as Managed Object Source requires flows");
				return null; // can not carry on
			}
		} else if (managingOfficeConfiguration.getInputManagedObjectConfiguration() != null) {
			// No flows, so should not be input
			issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
					"Configured as input managed object without flows");
			return null; // can not carry on
		}

		// Obtain managed object pool and possible thread completion listeners
		ManagedObjectPool managedObjectPool = null;
		ThreadCompletionListener[] threadCompletionListeners = null;
		ManagedObjectPoolConfiguration managedObjectPoolConfiguration = configuration
				.getManagedObjectPoolConfiguration();
		if (managedObjectPoolConfiguration == null) {
			// Determine if default managed object pool configured
			managedObjectPoolConfiguration = context.getDefaultManagedObjectPoolConfiguration();
		}
		if (managedObjectPoolConfiguration != null) {

			// Create the managed object pool for the managed object source
			ManagedObjectPoolFactory poolFactory = managedObjectPoolConfiguration.getManagedObjectPoolFactory();
			ManagedObjectPoolContext poolContext = new ManagedObjectPoolContextImpl(managedObjectSource);
			try {
				managedObjectPool = poolFactory.createManagedObjectPool(poolContext);
			} catch (Throwable ex) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						"Failed to create " + ManagedObjectPool.class.getSimpleName(), ex);
				return null; // can not carry on
			}

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

		// Obtain the execution meta-data
		ManagedObjectExecutionMetaData[] executionMetaDatas = metaData.getExecutionMetaData();

		// Obtain the start up functions
		ManagedFunctionInvocation[] startupFunctions = context.getStartupFunctions();

		// Obtain the service readiness
		ManagedObjectServiceReady[] serviceReadiness = context.getServiceReadiness();

		// Create the raw managing office meta-data
		RawManagingOfficeMetaData<h> rawManagingOfficeMetaData = new RawManagingOfficeMetaData<h>(officeName,
				recycleFunctionName, inputConfiguration, flowMetaDatas, executionMetaDatas, managingOfficeConfiguration,
				startupFunctions);

		// Created raw managed object meta-data
		RawManagedObjectMetaData<d, h> rawMoMetaData = new RawManagedObjectMetaData<d, h>(managedObjectSourceName,
				configuration, managedObjectSource, metaData, timeout, managedObjectPool, serviceReadiness,
				threadCompletionListeners, objectType, isManagedObjectContextAware, isManagedObjectAsynchronous,
				isManagedObjectCoordinating, rawManagingOfficeMetaData);

		// Make raw managed object available to the raw managing office
		rawManagingOfficeMetaData.setRawManagedObjectMetaData(rawMoMetaData);

		// Return the raw managed object meta-data
		return rawMoMetaData;
	}

}
