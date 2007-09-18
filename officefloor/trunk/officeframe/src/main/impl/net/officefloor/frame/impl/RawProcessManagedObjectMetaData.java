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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;

/**
 * Raw {@link net.officefloor.frame.internal.structure.ManagedObjectMetaData}
 * for a {@link net.officefloor.frame.internal.structure.ProcessState} bound
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
 * 
 * @author Daniel
 */
public class RawProcessManagedObjectMetaData {

	/**
	 * Creates the {@link RawProcessManagedObjectMetaData}.
	 * 
	 * @param moConfig
	 *            {@link ManagedObjectConfiguration}.
	 * @param processManagedObjectIndex
	 *            Index for the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            within the
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}.
	 * @param managedObjects
	 *            Registry of
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            instances for the
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @return {@link RawProcessManagedObjectMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public static <D extends Enum<D>> RawProcessManagedObjectMetaData createRawProcessManagedObjectMetaData(
			ManagedObjectConfiguration moConfig, int processManagedObjectIndex,
			Map<String, RawManagedObjectMetaData> managedObjects)
			throws ConfigurationException {

		// Obtain the Id of the managed object
		String managedObjectId = moConfig.getManagedObjectId();

		// Obtain the raw meta-data for the managed object
		RawManagedObjectMetaData rawMetaData = managedObjects
				.get(managedObjectId);
		if (rawMetaData == null) {
			throw new ConfigurationException("Unknown managed object '"
					+ managedObjectId + "' with the Office Floor");
		}

		// Obtain the dependency keys for the managed object
		Class<D> dependencyKeys = rawMetaData.getManagedObjectSource()
				.getMetaData().getDependencyKeys();

		// Create the dependency mappings
		Map<D, Integer> dependencyMapping;
		String[] dependencyIds;
		if (dependencyKeys == null) {
			// No dependencies
			dependencyMapping = Collections.EMPTY_MAP;
			dependencyIds = new String[0];

		} else {
			// Create the dependency mappings
			dependencyMapping = new EnumMap<D, Integer>(dependencyKeys);

			// Create the listing of dependencies
			List<String> dependencyListing = new LinkedList<String>();
			for (ManagedObjectDependencyConfiguration dependencyConfig : moConfig
					.getDependencyConfiguration()) {

				// Obtain the dependency managed object id
				String dependencyId = dependencyConfig.getManagedObjectName();

				// Add the dependency managed object id
				dependencyListing.add(dependencyId);
			}
			dependencyIds = dependencyListing.toArray(new String[0]);
		}

		// Create the managed object meta-data
		ManagedObjectMetaData moMetaData = rawMetaData
				.createManagedObjectMetaData(moConfig.getTimeout(),
						dependencyMapping);

		// Create the raw managed object meta-data
		return new RawProcessManagedObjectMetaData(moConfig, moMetaData,
				processManagedObjectIndex, dependencyIds, dependencyMapping);
	}

	/**
	 * {@link ManagedObjectConfiguration}.
	 */
	private final ManagedObjectConfiguration moConfig;

	/**
	 * {@link ManagedObjectMetaData}.
	 */
	private final ManagedObjectMetaData metaData;

	/**
	 * Index of the {@link ManagedObjectMetaData} within the
	 * {@link net.officefloor.frame.internal.structure.ProcessState}.
	 */
	private final int index;

	/**
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} dependency
	 * Ids.
	 */
	private final String[] dependencyIds;

	/**
	 * Dependency map for the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private final Map dependencyMap;

	/**
	 * Initiate.
	 * 
	 * @param moConfig
	 *            {@link ManagedObjectConfiguration}.
	 * @param metaData
	 *            {@link ManagedObjectMetaData}.
	 * @param index
	 *            Index of the {@link ManagedObjectMetaData} within the
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}.
	 * @param dependencyIds
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            dependency Ids.
	 * @param dependencyMap
	 *            Dependency map for the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private RawProcessManagedObjectMetaData(
			ManagedObjectConfiguration moConfig,
			ManagedObjectMetaData metaData, int index, String[] dependencyIds,
			Map dependencyMap) {
		this.moConfig = moConfig;
		this.metaData = metaData;
		this.index = index;
		this.dependencyIds = dependencyIds;
		this.dependencyMap = dependencyMap;
	}

	/**
	 * Obtains the {@link ManagedObjectConfiguration}.
	 * 
	 * @return {@link ManagedObjectConfiguration}.
	 */
	public ManagedObjectConfiguration getManagedObjectConfiguration() {
		return this.moConfig;
	}

	/**
	 * Obtains the {@link ManagedObjectMetaData}.
	 * 
	 * @return {@link ManagedObjectMetaData}.
	 */
	public ManagedObjectMetaData getManagedObjectMetaData() {
		return this.metaData;
	}

	/**
	 * Obtains the index of the {@link ManagedObjectMetaData} within the
	 * {@link net.officefloor.frame.internal.structure.ProcessState}.
	 * 
	 * @return Index of the {@link ManagedObjectMetaData} within the
	 *         {@link net.officefloor.frame.internal.structure.ProcessState}.
	 */
	public int getProcessIndex() {
		return this.index;
	}

	/**
	 * Obtains the {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 * dependency Ids.
	 * 
	 * @return {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *         dependency Ids.
	 */
	public String[] getDependencyIds() {
		return this.dependencyIds;
	}

	/**
	 * Loads the dependencies for the {@link RawProcessManagedObjectMetaData}.
	 * 
	 * @param moConfig
	 *            {@link ManagedObjectConfiguration}.
	 * @param moMetaData
	 *            {@link RawProcessManagedObjectMetaData}.
	 * @param moRegistry
	 *            {@link RawProcessManagedObjectRegistry}.
	 */
	@SuppressWarnings("unchecked")
	public void loadDependencyMappings(
			RawProcessManagedObjectRegistry moRegistry) {

		// Load the dependencies
		for (ManagedObjectDependencyConfiguration moDependencyConfig : this.moConfig
				.getDependencyConfiguration()) {

			// Obtain the raw process meta data for dependency
			RawProcessManagedObjectMetaData rawProcessMoMetaData = moRegistry
					.getRawProcessManagedObjectMetaData(moDependencyConfig
							.getManagedObjectName());

			// Load the dependency mappings for process bound managed object
			this.dependencyMap.put(moDependencyConfig.getDependencyKey(),
					new Integer(rawProcessMoMetaData.getProcessIndex()));
		}
	}

}
