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

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.frame.impl.construct.managedobjectsource.ClassLoaderResourceLocator;
import net.officefloor.frame.impl.execute.flow.FlowMetaDataImpl;
import net.officefloor.frame.impl.execute.managedobject.HandlerContextImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.impl.execute.office.OfficeImpl;
import net.officefloor.frame.impl.execute.officefloor.ManagedObjectExecuteContextImpl;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.HandlerConfiguration;
import net.officefloor.frame.internal.configuration.HandlerFlowConfiguration;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Registry of the {@link net.officefloor.frame.impl.RawManagedObjectMetaData}.
 * 
 * @author Daniel
 */
public class RawManagedObjectRegistry {

	/**
	 * Creates the registry of {@link RawManagedObjectMetaData} instances by
	 * their respective names.
	 * 
	 * @param officeConfiguration
	 *            Configuration of the Office.
	 * @param rawAssetRegistry
	 *            {@link RawAssetManagerRegistry}.
	 * @param officeBuilder
	 *            Builder of the Office.
	 * @param officeFrame
	 *            {@link OfficeFrame}.
	 * @return Registry of {@link RawManagedObjectMetaData} instances by their
	 *         respective names.
	 * @throws Exception
	 *             If fails.
	 */
	public static RawManagedObjectRegistry createRawManagedObjectMetaDataRegistry(
			OfficeFloorConfiguration officeFloorConfiguration,
			RawAssetManagerRegistry rawAssetRegistry, OfficeFrame officeFrame)
			throws Exception {

		// Create the Resource Locator
		ClassLoaderResourceLocator resourceLocator = new ClassLoaderResourceLocator();

		// Create the registry of offices
		Map<String, OfficeBuilder> offices = new HashMap<String, OfficeBuilder>();
		for (OfficeConfiguration officeConfig : officeFloorConfiguration
				.getOfficeConfiguration()) {
			// Obtain the Office name
			String officeName = officeConfig.getOfficeName();

			// Register the Office Builder
			offices.put(officeName, (OfficeBuilder) officeConfig);
		}

		// Create the registry of raw Managed Object meta-data
		Map<String, RawManagedObjectMetaData> mosdRepository = new HashMap<String, RawManagedObjectMetaData>();
		for (ManagedObjectSourceConfiguration<?, ?> mosConfig : officeFloorConfiguration
				.getManagedObjectSourceConfiguration()) {

			// Obtain the Office Builder for the managing Office
			String managingOfficeName = mosConfig.getManagingOfficeName();
			OfficeBuilder officeBuilder = offices.get(managingOfficeName);

			// Create the raw managed object
			mosdRepository.put(mosConfig.getManagedObjectSourceName(),
					RawManagedObjectMetaData.createRawManagedObjectMetaData(
							mosConfig, resourceLocator, rawAssetRegistry,
							officeBuilder, officeFrame));
		}

		// Return the registry
		return new RawManagedObjectRegistry(mosdRepository);
	}

	/**
	 * Registry of the {@link RawManagedObjectMetaData}.
	 */
	protected final Map<String, RawManagedObjectMetaData> rawMosRegistry;

	/**
	 * Initiate.
	 * 
	 */
	private RawManagedObjectRegistry(
			Map<String, RawManagedObjectMetaData> rawMosRegistry) {
		this.rawMosRegistry = rawMosRegistry;
	}

	/**
	 * Obtains the {@link RawManagedObjectMetaData} by its name.
	 * 
	 * @param name
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @return {@link RawManagedObjectMetaData} for the input name or
	 *         <code>null</code> if none by name.
	 */
	public RawManagedObjectMetaData getRawManagedObjectMetaData(String name) {
		return this.rawMosRegistry.get(name);
	}

	/**
	 * Obtains the listing of all {@link RawManagedObjectMetaData} instances.
	 * 
	 * @return Listing of all {@link RawManagedObjectMetaData} instances.
	 */
	public RawManagedObjectMetaData[] getAllRawManagedObjectMetaData() {
		return this.rawMosRegistry.values().toArray(
				new RawManagedObjectMetaData[0]);
	}

	/**
	 * Loads the remaining state of the
	 * {@link net.officefloor.frame.internal.structure.ManagedObjectMetaData}.
	 * 
	 * @param workRegistry
	 *            Registry of the {@link WorkMetaData}.
	 * @param office
	 *            {@link OfficeImpl}.
	 * @throws Exception
	 *             If fails to load remaining state.
	 */
	@SuppressWarnings("unchecked")
	public void loadRemainingManagedObjectState(
			Map<String, RawOfficeMetaData> offices) throws Exception {

		// Iterate over the raw managed objects
		for (RawManagedObjectMetaData rmo : this.rawMosRegistry.values()) {

			// Obtain the office managing the managed object
			String managingOfficeName = rmo
					.getManagedObjectSourceConfiguration()
					.getManagingOfficeName();
			if (managingOfficeName == null) {
				throw new ConfigurationException(
						"Must specify the managing office for managed object "
								+ rmo.getManagedObjectName());
			}
			RawOfficeMetaData rawOfficeMetaData = offices
					.get(managingOfficeName);

			// Obtain the Office
			OfficeImpl office = rawOfficeMetaData.getOffice();

			// Obtain the raw work registry of the Office
			RawWorkRegistry rawWorkRegistry = rawOfficeMetaData
					.getRawWorkRegistry();

			// Obtain the recycle flow meta-data
			FlowMetaData<?> recycleFlowMetaData = null;
			String recycleWorkName = rmo.getRecycleWorkName();
			if (recycleWorkName != null) {
				// Obtain the recycle work
				WorkMetaData<?> recycleWorkMetaData = rawWorkRegistry
						.getRawWorkMetaData(recycleWorkName).getWorkMetaData();

				// Obtain the recycle flow
				recycleFlowMetaData = recycleWorkMetaData
						.getInitialFlowMetaData();
			}

			// Obtain the managed object source configuration
			ManagedObjectSourceConfiguration mosConfig = rmo
					.getManagedObjectSourceConfiguration();

			// Obtain the handler keys
			Class<?> handlerKeys = rmo.getManagedObjectSource().getMetaData()
					.getHandlerKeys();

			// Create the map of handlers
			Map<Enum, Handler> handlers;
			if (handlerKeys == null) {
				// No handlers
				handlers = Collections.EMPTY_MAP;
			} else {

				// Obtain the raw process managed object registry
				RawProcessManagedObjectRegistry rawProcessMoRegistry = rawOfficeMetaData
						.getRawOfficeResourceRegistry()
						.getRawProcessManagedObjectRegistry();

				// Create registry of managed object links
				Map<String, String> moNameTrans = new HashMap<String, String>();
				for (LinkedManagedObjectSourceConfiguration linkedMoConfig : rawOfficeMetaData
						.getOfficeConfiguration().getRegisteredManagedObjects()) {
					moNameTrans.put(linkedMoConfig.getManagedObjectId(),
							linkedMoConfig.getManagedObjectName());
				}

				// Obtain the process managed object meta-data
				RawProcessManagedObjectMetaData processMoMetaData = rawProcessMoRegistry
						.getRawProcessManagedObjectMetaData(rmo);
				if (processMoMetaData == null) {
					throw new ConfigurationException("Managed Object '"
							+ rmo.getManagedObjectName()
							+ "' not found as process bound for office "
							+ rawOfficeMetaData.getOfficeName());
				}
				int processMoIndex = processMoMetaData.getProcessIndex();

				// Create the list of handlers
				handlers = new EnumMap(handlerKeys);

				// Create the registry of handler configurations
				EnumMap handlerConfigs = new EnumMap(handlerKeys);
				for (HandlerConfiguration handlerConfig : mosConfig
						.getHandlerConfiguration()) {
					handlerConfigs.put(handlerConfig.getHandlerKey(),
							handlerConfig);
				}

				// Create the registry of handlers
				for (Object key : handlerKeys.getEnumConstants()) {

					// Obtain the handle configuration
					HandlerConfiguration handlerConfig = (HandlerConfiguration) handlerConfigs
							.get(key);
					if (handlerConfig == null) {
						throw new ConfigurationException("No handler for key "
								+ key + " of managed object "
								+ rmo.getManagedObjectName() + " of office "
								+ rawOfficeMetaData.getOfficeName());
					}

					// Create the process links
					HandlerFlowConfiguration<?>[] processLinkConfig = handlerConfig
							.getLinkedProcessConfiguration();
					FlowMetaData<?>[] processLinks = new FlowMetaData[processLinkConfig.length];
					for (int i = 0; i < processLinks.length; i++) {

						// No flow manager required as can not access its future

						// Ensure task node reference provided
						TaskNodeReference taskNodeRef = processLinkConfig[i]
								.getTaskNodeReference();
						if ((taskNodeRef == null)
								|| (taskNodeRef.getWorkName() == null)
								|| (taskNodeRef.getTaskName() == null)) {
							throw new ConfigurationException(
									"Task not linked in for process link "
											+ processLinkConfig[i]
													.getFlowName()
											+ " for handler " + key
											+ " of managed object "
											+ rmo.getManagedObjectName()
											+ " of office "
											+ rawOfficeMetaData.getOfficeName());
						}

						// Create the current flow meta-data
						FlowMetaData<?> processLink = new FlowMetaDataImpl(
								FlowInstigationStrategyEnum.ASYNCHRONOUS,
								rawWorkRegistry.getTaskMetaData(taskNodeRef),
								null);

						// Load the process link
						processLinks[i] = processLink;
					}

					// Create the handler context
					HandlerContext handlerContext = new HandlerContextImpl(
							processMoIndex, processLinks, office);

					// Create the handler
					HandlerFactory<?> handlerFactory = handlerConfig
							.getHandlerFactory();
					if (handlerFactory == null) {
						throw new ConfigurationException("No "
								+ HandlerFactory.class.getSimpleName()
								+ " for handler " + key + " of managed object "
								+ rmo.getManagedObjectName() + " of office "
								+ rawOfficeMetaData.getOfficeName());
					}
					Handler<?> handler = handlerFactory.createHandler();

					// Specify context for the handler
					handler.setHandlerContext(handlerContext);

					// Register with handlers
					handlers.put((Enum) key, handler);
				}
			}

			// Create the execute context
			ManagedObjectExecuteContextImpl executeContext = new ManagedObjectExecuteContextImpl(
					handlers);

			// Start the managed object
			rmo.getManagedObjectSource().start(executeContext);

			// Flag the start method over
			executeContext.flagStartOver();

			// Iterate over the meta-data of the managed objects
			for (ManagedObjectMetaDataImpl moMetaData : rmo
					.getManagedObjectMetaData()) {
				// Load the remaining state to the managed object
				moMetaData.loadRemainingState(office, recycleFlowMetaData);
			}
		}
	}

	/**
	 * Creates the registry of {@link ManagedObjectSource} by their names.
	 * 
	 * @return Registry of {@link ManagedObjectSource} by their names.
	 */
	public Map<String, ManagedObjectSource<?, ?>> createManagedObjectSourceRegistry() {
		// Create the registry of Managed Object Sources
		Map<String, ManagedObjectSource<?, ?>> managedObjectSources = new HashMap<String, ManagedObjectSource<?, ?>>();
		for (String managedObjectId : this.rawMosRegistry.keySet()) {
			managedObjectSources.put(managedObjectId, this.rawMosRegistry.get(
					managedObjectId).getManagedObjectSource());
		}

		// Return the registry
		return managedObjectSources;
	}
}
