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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;

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
			Map<String, RawManagedObjectMetaData> managedObjects)
			throws ConfigurationException {

		// TODO remove
		System.out.println("OfficeFloor managed object Ids:");
		for (String key : managedObjects.keySet()) {
			System.out.println("    '" + key + "'");
		}

		// Create the listing of process managed objects
		int currentIndex = 0;
		Map<String, RawProcessManagedObjectMetaData> processManagedObjectRegistry = new HashMap<String, RawProcessManagedObjectMetaData>();
		List<ManagedObjectMetaData> processManagedObjectList = new LinkedList<ManagedObjectMetaData>();
		for (ManagedObjectConfiguration moConfig : officeConfiguration
				.getManagedObjectConfiguration()) {

			// Create the raw process managed object meta-data
			RawProcessManagedObjectMetaData rawProcessMoMetaData = RawProcessManagedObjectMetaData
					.createRawProcessManagedObjectMetaData(moConfig,
							currentIndex++, managedObjects);

			// Register the managed object
			processManagedObjectRegistry.put(moConfig.getManagedObjectName(),
					rawProcessMoMetaData);
			processManagedObjectList.add(rawProcessMoMetaData
					.getManagedObjectMetaData());
		}

		// Return the process managed object meta-data
		RawProcessManagedObjectRegistry rawProcessMoRegistry = new RawProcessManagedObjectRegistry(
				processManagedObjectList.toArray(new ManagedObjectMetaData[0]),
				processManagedObjectRegistry);

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
	private final ManagedObjectMetaData[] metaData;

	/**
	 * Registry of {@link RawProcessManagedObjectMetaData} by their names.
	 */
	private final Map<String, RawProcessManagedObjectMetaData> registry;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}
	 *            {@link ManagedObjectMetaData}.
	 * @param registry
	 *            Registry of {@link RawProcessManagedObjectMetaData} by their
	 *            names.
	 */
	private RawProcessManagedObjectRegistry(ManagedObjectMetaData[] metaData,
			Map<String, RawProcessManagedObjectMetaData> registry) {
		this.metaData = metaData;
		this.registry = registry;
	}

	/**
	 * Obtains the {@link net.officefloor.frame.internal.structure.ProcessState}
	 * {@link ManagedObjectMetaData}.
	 * 
	 * @return {@link net.officefloor.frame.internal.structure.ProcessState}
	 *         {@link ManagedObjectMetaData}.
	 */
	public ManagedObjectMetaData[] getManagedObjectMetaData() {
		return this.metaData;
	}

	/**
	 * Obtains the {@link RawProcessManagedObjectMetaData} for the specified
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @param managedObjectName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @return {@link RawProcessManagedObjectMetaData} for the specified
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	public RawProcessManagedObjectMetaData getRawProcessManagedObjectMetaData(
			String managedObjectName) {
		return this.registry.get(managedObjectName);
	}

}
