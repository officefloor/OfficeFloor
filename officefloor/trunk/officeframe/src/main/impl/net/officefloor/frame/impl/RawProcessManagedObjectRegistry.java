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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Registry of the {@link net.officefloor.frame.internal.structure.ProcessState}
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances.
 * 
 * @author Daniel
 */
public class RawProcessManagedObjectRegistry {

	/**
	 * Obtains the {@link ManagedObjectMetaData} for the
	 * {@link net.officefloor.frame.internal.structure.ProcessState}.
	 * 
	 * @return {@link ManagedObjectMetaData} for the
	 *         {@link net.officefloor.frame.internal.structure.ProcessState}.
	 */
	@SuppressWarnings("unchecked")
	public static RawProcessManagedObjectRegistry createProcessStateManagedObjectRegistry(
			OfficeConfiguration officeConfiguration,
			Map<String, RawManagedObjectMetaData> managedObjects,
			RawManagedObjectMetaData[] officeManagedObjects)
			throws ConfigurationException {

		// Create the listing of process managed objects
		int currentIndex = 0;
		Map<String, RawProcessManagedObjectMetaData> officeManagedObjectRegistry = new HashMap<String, RawProcessManagedObjectMetaData>();
		Map<RawManagedObjectMetaData, RawProcessManagedObjectMetaData> processManagedObjectRegistry = new HashMap<RawManagedObjectMetaData, RawProcessManagedObjectMetaData>();
		List<ManagedObjectMetaData> processManagedObjectList = new LinkedList<ManagedObjectMetaData>();

		// Track the managed objects used by the office
		Set<RawManagedObjectMetaData> addedMos = new HashSet<RawManagedObjectMetaData>();

		// Add Managed Objects used by the Office
		for (ManagedObjectConfiguration moConfig : officeConfiguration
				.getManagedObjectConfiguration()) {

			// Obtains the managed object source id
			String mosId = moConfig.getManagedObjectId();

			// Obtain the corresponding raw managed object meta-data
			RawManagedObjectMetaData rawMoMetaData = managedObjects.get(mosId);
			if (rawMoMetaData == null) {
				throw new ConfigurationException(
						"Can not find raw managed object meta-data for the process managed object "
								+ mosId);
			}

			// Create the raw process managed object meta-data
			RawProcessManagedObjectMetaData rawProcessMoMetaData = RawProcessManagedObjectMetaData
					.createRawProcessManagedObjectMetaData(moConfig,
							currentIndex++, managedObjects);

			// Indicate added
			addedMos.add(rawMoMetaData);
			
			// Register the managed object
			officeManagedObjectRegistry.put(moConfig.getManagedObjectName(),
					rawProcessMoMetaData);
			processManagedObjectRegistry.put(rawMoMetaData,
					rawProcessMoMetaData);
			processManagedObjectList.add(rawProcessMoMetaData
					.getManagedObjectMetaData());
		}

		// Add Managed Objects invoking tasks within the Office
		for (RawManagedObjectMetaData rawMoMetaData : officeManagedObjects) {

			// Ignore if already provided to office as process managed object
			if (addedMos.contains(rawMoMetaData)) {
				continue;
			}

			// Do not include additionally if does not have handlers
			if (rawMoMetaData.getManagedObjectSource().getMetaData()
					.getHandlerKeys() == null) {
				continue;
			}
			
			// Create the raw process managed object meta-data
			RawProcessManagedObjectMetaData rawProcessMoMetaData = RawProcessManagedObjectMetaData
					.createRawProcessManagedObjectMetaData(rawMoMetaData,
							currentIndex++);

			// Register the managed object
			processManagedObjectRegistry.put(rawMoMetaData,
					rawProcessMoMetaData);
			processManagedObjectList.add(rawProcessMoMetaData
					.getManagedObjectMetaData());
		}

		// Return the process managed object meta-data
		RawProcessManagedObjectRegistry rawProcessMoRegistry = new RawProcessManagedObjectRegistry(
				processManagedObjectList.toArray(new ManagedObjectMetaData[0]),
				officeManagedObjectRegistry, processManagedObjectRegistry);

		// Load dependencies indexes (all must be process bound)
		for (RawProcessManagedObjectMetaData rawProcessMoMetaData : processManagedObjectRegistry
				.values()) {
			rawProcessMoMetaData.loadDependencyMappings(rawProcessMoRegistry);
		}

		// Return the registry
		return rawProcessMoRegistry;
	}

	/**
	 * {@link net.officefloor.frame.internal.structure.ProcessState}
	 * {@link ManagedObjectMetaData}.
	 */
	private final ManagedObjectMetaData<?>[] metaData;

	/**
	 * Registry of {@link RawProcessManagedObjectMetaData} by the name of it
	 * within the {@link Office}.
	 */
	private final Map<String, RawProcessManagedObjectMetaData> officeRegistry;

	/**
	 * Registry of {@link RawProcessManagedObjectMetaData} by its
	 * {@link RawManagedObjectMetaData}.
	 */
	private final Map<RawManagedObjectMetaData, RawProcessManagedObjectMetaData> rawRegistry;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}
	 *            {@link ManagedObjectMetaData}.
	 * @param officeRegistry
	 *            Registry of {@link RawProcessManagedObjectMetaData} by the
	 *            name of it within the {@link Office}.
	 * @param rawRegistry
	 *            Registry of {@link RawProcessManagedObjectMetaData} by its
	 *            {@link RawManagedObjectMetaData}.
	 */
	private RawProcessManagedObjectRegistry(
			ManagedObjectMetaData<?>[] metaData,
			Map<String, RawProcessManagedObjectMetaData> officeRegistry,
			Map<RawManagedObjectMetaData, RawProcessManagedObjectMetaData> rawRegistry) {
		this.metaData = metaData;
		this.officeRegistry = officeRegistry;
		this.rawRegistry = rawRegistry;
	}

	/**
	 * Obtains the {@link net.officefloor.frame.internal.structure.ProcessState}
	 * {@link ManagedObjectMetaData}.
	 * 
	 * @return {@link net.officefloor.frame.internal.structure.ProcessState}
	 *         {@link ManagedObjectMetaData}.
	 */
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.metaData;
	}

	/**
	 * Obtains the {@link RawProcessManagedObjectMetaData} for the specified
	 * {@link RawManagedObjectMetaData}.
	 * 
	 * @param rawMoMetaData
	 *            {@link RawManagedObjectMetaData} of the
	 *            {@link RawProcessManagedObjectMetaData}.
	 * @return {@link RawProcessManagedObjectMetaData} for the specified
	 *         {@link ManagedObject}.
	 */
	public RawProcessManagedObjectMetaData getRawProcessManagedObjectMetaData(
			RawManagedObjectMetaData rawMoMetaData) {
		return this.rawRegistry.get(rawMoMetaData);
	}

	/**
	 * Obtains the {@link RawProcessManagedObjectMetaData} by the name of it
	 * within the {@link Office}.
	 * 
	 * @param officeManagedObjectName
	 *            Name of the {@link RawManagedObjectMetaData} within the
	 *            {@link Office}.
	 * @return {@link RawProcessManagedObjectMetaData}.
	 */
	public RawProcessManagedObjectMetaData getRawProcessManagedObjectMetaData(
			String officeManagedObjectName) {
		return this.officeRegistry.get(officeManagedObjectName);
	}

}
