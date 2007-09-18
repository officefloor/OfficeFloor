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

import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Registry of the
 * {@link net.officefloor.frame.impl.RawWorkManagedObjectMetaData}.
 * 
 * @author Daniel
 */
public class RawWorkManagedObjectRegistry {

	/**
	 * Obtains the {@link ManagedObjectMetaData} registry for the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @param <D>
	 *            Keys of the dependency mappings for the {@link ManagedObject}
	 *            instances for the
	 *            {@link net.officefloor.frame.api.execute.Work}.
	 * @param workConfig
	 *            Configuration of the
	 *            {@link net.officefloor.frame.api.execute.Work}.
	 * @param processMoRegistry
	 *            {@link RawProcessManagedObjectRegistry} for the
	 *            {@link net.officefloor.frame.api.manage.Office} containing
	 *            this {@link net.officefloor.frame.api.execute.Work}.
	 * @param officeResources
	 *            Resources of the
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @return {@link ManagedObjectMetaData} registry for the
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 * @throws ConfigurationException
	 *             Indicates invalid configuration.
	 */
	@SuppressWarnings("unchecked")
	public static <D extends Enum<D>> RawWorkManagedObjectRegistry createWorkManagedObjectRegistry(
			WorkConfiguration workConfig,
			RawOfficeResourceRegistry officeResources)
			throws ConfigurationException {

		// Create the listing of work bound managed objects
		List<RawWorkManagedObjectMetaData> workBoundRequiredListing = new LinkedList<RawWorkManagedObjectMetaData>();
		for (ManagedObjectConfiguration workManagedObjectConfig : workConfig
				.getManagedObjectConfiguration()) {

			// Create the work bound managed object
			RawWorkManagedObjectMetaData workMoMetaData = RawWorkManagedObjectMetaData
					.createWorkBound(workManagedObjectConfig, officeResources);

			// Add to listing of work bound
			workBoundRequiredListing.add(workMoMetaData);
		}

		// Obtain the process managed object registry
		RawProcessManagedObjectRegistry processMoRegistry = officeResources
				.getRawProcessManagedObjectRegistry();

		// Create the listing of process bound managed objects
		Set<String> processDependencies = new HashSet<String>();
		Set<String> processLinked = new HashSet<String>();
		List<RawWorkManagedObjectMetaData> processBoundRequiredListing = new LinkedList<RawWorkManagedObjectMetaData>();
		for (LinkedManagedObjectConfiguration moConfig : workConfig
				.getProcessManagedObjectConfiguration()) {

			// Create the process bound managed object meta-data
			RawWorkManagedObjectMetaData rawMoMetaData = RawWorkManagedObjectMetaData
					.createProcessBound(moConfig, processMoRegistry);

			// Track linked process managed object
			String managedObjectId = moConfig.getManagedObjectId();
			processLinked.add(managedObjectId);

			// Obtain the process meta-data for the managed object
			RawProcessManagedObjectMetaData processMoMetaData = rawMoMetaData
					.getRawProcessManagedObjectMetaData();

			// Track all dependencies
			String[] dependencyIds = processMoMetaData.getDependencyIds();
			for (String dependencyId : dependencyIds) {
				processDependencies.add(dependencyId);
			}

			// Add to listing of process bound
			processBoundRequiredListing.add(rawMoMetaData);
		}

		// Include dependency process bound managed objects not linked
		boolean isAllLinked = false;
		while (!isAllLinked) {
			// Flag all linked initially
			isAllLinked = true;

			// Iterate over all dependencies ensuring all loaded
			for (String dependencyId : processDependencies) {
				if (!processLinked.contains(dependencyId)) {

					// Not all linked yet
					isAllLinked = false;

					// Create the managed object meta-data
					RawWorkManagedObjectMetaData rawMoMetaData = RawWorkManagedObjectMetaData
							.createProcessBound(dependencyId, processMoRegistry);

					// Obtain the Raw Process Managed Object meta-data
					RawProcessManagedObjectMetaData processMoMetaData = rawMoMetaData
							.getRawProcessManagedObjectMetaData();

					// Track linked process managed object
					processLinked.add(dependencyId);

					// Track all dependencies
					String[] dependencyIds = processMoMetaData
							.getDependencyIds();
					for (String dependencyDependencyId : dependencyIds) {
						processDependencies.add(dependencyDependencyId);
					}

					// Add to listing of process bound
					processBoundRequiredListing.add(rawMoMetaData);
				}
			}
		}

		// Create the listing of managed object meta-data and compile indexes
		ManagedObjectMetaData[] moMetaData = new ManagedObjectMetaData[workBoundRequiredListing
				.size()
				+ processBoundRequiredListing.size()];
		RawWorkManagedObjectMetaData[] rawMoMetaData = new RawWorkManagedObjectMetaData[moMetaData.length];
		Map<String, Integer> workMoIndexes = new HashMap<String, Integer>();
		Map<String, Integer> processMoIndexes = new HashMap<String, Integer>();
		int index = 0;

		// Load the work bound managed objects first
		for (RawWorkManagedObjectMetaData rawMetaData : workBoundRequiredListing) {
			// Load managed object details
			rawMoMetaData[index] = rawMetaData;
			moMetaData[index] = rawMetaData.getManagedObjectMetaData();
			workMoIndexes.put(rawMetaData.getWorkManagedObjectConfiguration()
					.getManagedObjectName(), new Integer(index));

			// Increment index
			index++;
		}

		// Load process bound managed objects second
		for (RawWorkManagedObjectMetaData rawMetaData : processBoundRequiredListing) {
			// Load managed object details
			rawMoMetaData[index] = rawMetaData;
			moMetaData[index] = rawMetaData.getManagedObjectMetaData();

			// Register work index only if linked
			if (rawMetaData.getProcessManagedObjectConfiguration() != null) {
				workMoIndexes.put(rawMetaData
						.getProcessManagedObjectConfiguration()
						.getManagedObjectName(), new Integer(index));
			}

			// Register process managed object
			String processManagedObjctName = rawMetaData
					.getProcessManagedObjectConfiguration()
					.getManagedObjectName();
			processMoIndexes.put(processManagedObjctName, new Integer(index));

			// Increment index
			index++;
		}

		// Create the Work Managed Object meta-data
		RawWorkManagedObjectRegistry workMoRegistry = new RawWorkManagedObjectRegistry(
				workConfig.getWorkName(), moMetaData, rawMoMetaData,
				workMoIndexes, processMoIndexes);

		// Load the dependencies
		for (RawWorkManagedObjectMetaData rawMetaData : rawMoMetaData) {
			rawMetaData.loadDependencies(workMoIndexes, processMoIndexes,
					workMoRegistry);
		}

		// Return the Work Managed Object meta-data registry
		return workMoRegistry;
	}

	/**
	 * Name of the {@link net.officefloor.frame.api.execute.Work} this
	 * represents the {@link ManagedObject} instances.
	 */
	protected final String workName;

	/**
	 * {@link ManagedObjectMetaData} for the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 */
	protected final ManagedObjectMetaData[] moMetaData;

	/**
	 * {@link RawWorkManagedObjectMetaData} for the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 */
	protected final RawWorkManagedObjectMetaData[] rawMoMetaData;

	/**
	 * Indexes of the {@link ManagedObject} instances bound directly to the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 */
	protected final Map<String, Integer> workMoIndexes;

	/**
	 * Indexes of the {@link ManagedObject} instances bound to the
	 * {@link net.officefloor.frame.internal.structure.ProcessState} for the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 */
	protected final Map<String, Integer> processMoIndexes;

	/**
	 * Initiate.
	 * 
	 * @param workName
	 *            Name of the {@link net.officefloor.frame.api.execute.Work}.
	 * @param moMetaData
	 *            {@link ManagedObjectMetaData} for the
	 *            {@link net.officefloor.frame.api.execute.Work}.
	 * @param rawMoMetaData
	 *            {@link RawWorkManagedObjectMetaData} for the
	 *            {@link net.officefloor.frame.api.execute.Work}.
	 * @param workMoIndexes
	 *            Indexes of the {@link ManagedObject} instances bound directly
	 *            to the {@link net.officefloor.frame.api.execute.Work}.
	 * @param processMoIndexes
	 *            Indexes of the {@link ManagedObject} instances bound to the
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}
	 *            for the {@link net.officefloor.frame.api.execute.Work}.
	 */
	private RawWorkManagedObjectRegistry(String workName,
			ManagedObjectMetaData[] moMetaData,
			RawWorkManagedObjectMetaData[] rawMoMetaData,
			Map<String, Integer> workMoIndexes,
			Map<String, Integer> processMoIndexes) {
		this.workName = workName;
		this.moMetaData = moMetaData;
		this.rawMoMetaData = rawMoMetaData;
		this.workMoIndexes = workMoIndexes;
		this.processMoIndexes = processMoIndexes;
	}

	/**
	 * Creates the listing of {@link ManagedObjectMetaData} for the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @return Listing of {@link ManagedObjectMetaData} for the
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 */
	public ManagedObjectMetaData[] getWorkManagedObjectListing() {
		return this.moMetaData;
	}

	/**
	 * Obtains the {@link RawWorkManagedObjectMetaData} instances.
	 * 
	 * @return {@link RawWorkManagedObjectMetaData} instances.
	 */
	public RawWorkManagedObjectMetaData[] getRawWorkManagedObjectMetaData() {
		return this.rawMoMetaData;
	}

	/**
	 * Obtains the index of the input {@link RawWorkManagedObjectMetaData}.
	 * 
	 * @param moMetaData
	 *            {@link RawWorkManagedObjectMetaData}.
	 * @return Index of the input {@link RawWorkManagedObjectMetaData}.
	 * @throws ConfigurationException
	 *             If not of this registry.
	 */
	public int getIndex(RawWorkManagedObjectMetaData moMetaData)
			throws ConfigurationException {

		// Return the matching index
		for (int i = 0; i < this.rawMoMetaData.length; i++) {
			if (this.rawMoMetaData[i] == moMetaData) {
				return i;
			}
		}

		// Not matching
		throw new ConfigurationException("Unknown managed object for work");
	}

	/**
	 * Obtains the index of the {@link ManagedObject} on the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @param workManagedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @return index of the {@link ManagedObject} on the
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 * @throws ConfigurationException
	 *             If unknown {@link ManagedObject} name.
	 */
	public int getIndexByWorkManagedObjectName(String workManagedObjectName)
			throws ConfigurationException {

		// Obtain the index
		Integer index = this.workMoIndexes.get(workManagedObjectName);

		// Ensure known work managed object
		if (index == null) {
			throw new ConfigurationException("Unknown work managed object '"
					+ workManagedObjectName + "' for work '" + this.workName
					+ "'");
		}

		// Return the index
		return index.intValue();
	}

	/**
	 * Obtains the index of the {@link ManagedObject} on the
	 * {@link net.officefloor.frame.api.execute.Work} by its
	 * {@link net.officefloor.frame.internal.structure.ProcessState} bound name.
	 * 
	 * @param processManagedObjectName
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}
	 *            bound name.
	 * @return Index of the {@link ManagedObject} on the
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 * @throws ConfigurationException
	 *             If unknown {@link ManagedObject} name.
	 */
	public int getIndexByProcessManagedObjectName(
			String processManagedObjectName) throws ConfigurationException {

		// Obtain the index
		Integer index = this.processMoIndexes.get(processManagedObjectName);

		// Ensure known work managed object
		if (index == null) {
			throw new ConfigurationException("Unknown process managed object '"
					+ processManagedObjectName + "' for work '" + this.workName
					+ "'");
		}

		// Return the index
		return index.intValue();
	}

}
