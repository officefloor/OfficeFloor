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

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectDependencyConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Structure to link {@link ManagedObjectMetaData} to its name.
 * 
 * @author Daniel
 */
public class RawWorkManagedObjectMetaData<D extends Enum<D>> {

	/**
	 * Creates a {@link net.officefloor.frame.api.execute.Work} bound
	 * {@link RawWorkManagedObjectMetaData}.
	 * 
	 * @param workManagedObjectConfig
	 *            {@link net.officefloor.frame.api.execute.Work}
	 *            {@link ManagedObjectConfiguration}.
	 * @param officeResources
	 *            {@link RawOfficeResourceRegistry}.
	 * @return {@link RawWorkManagedObjectMetaData} bound to
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 * @throws ConfigurationException
	 *             If fails configuration.
	 */
	@SuppressWarnings("unchecked")
	public static RawWorkManagedObjectMetaData createWorkBound(
			ManagedObjectConfiguration workManagedObjectConfig,
			RawOfficeResourceRegistry officeResources)
			throws ConfigurationException {

		// Obtain the raw meta-data for the managed object
		RawManagedObjectMetaData rawMetaData = officeResources
				.getRawManagedObjectMetaData(workManagedObjectConfig
						.getManagedObjectId());
		if (rawMetaData == null) {
			throw new ConfigurationException("Unknown raw managed object '"
					+ workManagedObjectConfig.getManagedObjectId() + "'");
		}

		// Obtain timeout for asynchronous operations
		long timeout = workManagedObjectConfig.getTimeout();

		// Obtain the dependencies for the managed object
		Class<?> dependencyListingEnum = rawMetaData.getManagedObjectSource()
				.getMetaData().getDependencyKeys();

		// Obtain the type defining the keys for dependencies
		Map<Enum, Integer> dependencyMapping = null;
		if (dependencyListingEnum != null) {

			// Have dependencies thus create dependency mapping
			dependencyMapping = new EnumMap(dependencyListingEnum);

			// Dependencies will be loaded later
		}

		// Work scoped meta-data
		ManagedObjectMetaData managedObjectMetaData = rawMetaData
				.createManagedObjectMetaData(timeout, dependencyMapping);

		// Add to listing of work bound
		return new RawWorkManagedObjectMetaData(managedObjectMetaData,
				workManagedObjectConfig, dependencyMapping);
	}

	/**
	 * Creates the {@link net.officefloor.frame.internal.structure.ProcessState}
	 * bound {@link RawWorkManagedObjectMetaData}.
	 * 
	 * @param linkedManagedObjectConfig
	 *            {@link LinkedManagedObjectSourceConfiguration} for linking the
	 *            {@link ManagedObject} to the
	 *            {@link net.officefloor.frame.api.execute.Work}.
	 * @param processMoRegistry
	 *            {@link RawProcessManagedObjectRegistry}.
	 * @return {@link RawWorkAdministratorMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public static RawWorkManagedObjectMetaData<?> createProcessBound(
			LinkedManagedObjectSourceConfiguration linkedManagedObjectConfig,
			RawProcessManagedObjectRegistry processMoRegistry)
			throws ConfigurationException {

		// Obtain the Raw Process Managed Object meta-data
		String managedObjectId = linkedManagedObjectConfig.getManagedObjectId();
		RawProcessManagedObjectMetaData rawMetaData = processMoRegistry
				.getRawProcessManagedObjectMetaData(managedObjectId);
		if (rawMetaData == null) {
			throw new ConfigurationException("Unknown process managed object '"
					+ managedObjectId + "' for work");
		}

		// Create the meta-data for the process managed object
		ManagedObjectMetaData<?> managedObjectMetaData = new ManagedObjectMetaDataImpl(
				rawMetaData.getProcessIndex());

		// Add to listing of process bound
		return new RawWorkManagedObjectMetaData(managedObjectMetaData,
				linkedManagedObjectConfig, rawMetaData);
	}

	/**
	 * Creates the {@link net.officefloor.frame.internal.structure.ProcessState}
	 * bound {@link RawWorkManagedObjectMetaData} linked in by a dependency.
	 * 
	 * @param processManagedObjectName
	 *            Name of
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}
	 *            bound {@link ManagedObject}.
	 * @param processMoRegistry
	 *            {@link RawProcessManagedObjectRegistry}.
	 * @return {@link RawWorkAdministratorMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public static RawWorkManagedObjectMetaData<?> createProcessBound(
			String processManagedObjectName,
			RawProcessManagedObjectRegistry processMoRegistry)
			throws ConfigurationException {

		// Obtain the Raw Process Managed Object meta-data
		RawProcessManagedObjectMetaData rawMetaData = processMoRegistry
				.getRawProcessManagedObjectMetaData(processManagedObjectName);
		if (rawMetaData == null) {
			throw new ConfigurationException("Unknown process managed object '"
					+ processManagedObjectName + "' for work");
		}

		// Create the meta-data for the process managed object
		ManagedObjectMetaData<?> managedObjectMetaData = new ManagedObjectMetaDataImpl(
				rawMetaData.getProcessIndex());

		// Add to listing of process bound
		return new RawWorkManagedObjectMetaData(managedObjectMetaData,
				rawMetaData);
	}

	/**
	 * Flag indicating if
	 * {@link net.officefloor.frame.internal.structure.ProcessState} bound.
	 */
	private final boolean isProcessBound;

	/**
	 * {@link ManagedObjectConfiguration}.
	 */
	private final ManagedObjectConfiguration workManagedObjectConfig;

	/**
	 * {@link ManagedObjectMetaData}.
	 */
	private final ManagedObjectMetaData<D> metaData;

	/**
	 * Dependency mappings for the {@link ManagedObject}.
	 */
	private final Map<D, Integer> dependencyMapping;

	/**
	 * {@link LinkedManagedObjectSourceConfiguration}.
	 */
	private final LinkedManagedObjectSourceConfiguration processManagedObjectConfig;

	/**
	 * {@link RawProcessManagedObjectMetaData}.
	 */
	private final RawProcessManagedObjectMetaData processMoMetaData;

	/**
	 * Indexes of all the required dependency work indexes.
	 */
	private int[] dependencyWorkIndexes;

	/**
	 * Initiate {@link net.officefloor.frame.api.execute.Work} bound.
	 * 
	 * @param workManagedObjectConfig
	 *            {@link ManagedObjectConfiguration}.
	 * @param metaData
	 *            {@link ManagedObjectMetaData}.
	 * @param dependencyMapping
	 *            Dependency mappings for the {@link ManagedObject}.
	 */
	private RawWorkManagedObjectMetaData(ManagedObjectMetaData<D> metaData,
			ManagedObjectConfiguration workManagedObjectConfig,
			Map<D, Integer> dependencyMapping) {
		this.isProcessBound = false;
		this.workManagedObjectConfig = workManagedObjectConfig;
		this.metaData = metaData;
		this.dependencyMapping = dependencyMapping;
		this.processManagedObjectConfig = null;
		this.processMoMetaData = null;
	}

	/**
	 * Initiate {@link net.officefloor.frame.internal.structure.ProcessState}
	 * bound.
	 * 
	 * @param metaData
	 *            {@link ManagedObjectMetaData}.
	 * @param workManagedObjectConfig
	 *            {@link LinkedManagedObjectSourceConfiguration}.
	 */
	private RawWorkManagedObjectMetaData(ManagedObjectMetaData<D> metaData,
			LinkedManagedObjectSourceConfiguration processManagedObjectConfig,
			RawProcessManagedObjectMetaData processMoMetaData) {
		this.isProcessBound = true;
		this.workManagedObjectConfig = null;
		this.metaData = metaData;
		this.dependencyMapping = null;
		this.processManagedObjectConfig = processManagedObjectConfig;
		this.processMoMetaData = processMoMetaData;
	}

	/**
	 * Initiate {@link net.officefloor.frame.internal.structure.ProcessState}
	 * bound.
	 * 
	 * @param metaData
	 *            {@link ManagedObjectMetaData}.
	 * @param workManagedObjectConfig
	 *            {@link LinkedManagedObjectSourceConfiguration}.
	 */
	private RawWorkManagedObjectMetaData(ManagedObjectMetaData<D> metaData,
			RawProcessManagedObjectMetaData processMoMetaData) {
		this.isProcessBound = true;
		this.workManagedObjectConfig = null;
		this.metaData = metaData;
		this.dependencyMapping = null;
		this.processManagedObjectConfig = null;
		this.processMoMetaData = processMoMetaData;
	}

	/**
	 * Loads the dependencies for this {@link ManagedObject}.
	 * 
	 * @param workMoIndexes
	 *            Translation of {@link net.officefloor.frame.api.execute.Work}
	 *            {@link ManagedObject} name to its
	 *            {@link net.officefloor.frame.api.execute.Work} index.
	 * @param processMoIndexes
	 *            Translation of
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}
	 *            {@link ManagedObject} name to its
	 *            {@link net.officefloor.frame.api.execute.Work} index.
	 * @param rawMoMetaData
	 *            Listing of {@link RawWorkManagedObjectMetaData} for the
	 *            {@link net.officefloor.frame.api.execute.Work}.
	 */
	@SuppressWarnings("unchecked")
	public void loadDependencies(Map<String, Integer> workMoIndexes,
			Map<String, Integer> processMoIndexes,
			RawWorkManagedObjectRegistry workMoRegistry)
			throws ConfigurationException {

		// Load the dependency mappings to work bound managed object
		if (!this.isProcessBound()) {
			// Ensure has dependencies
			if (this.dependencyMapping != null) {

				// Load dependencies
				for (ManagedObjectDependencyConfiguration dependencyConfig : this.workManagedObjectConfig
						.getDependencyConfiguration()) {

					// Obtain the index of the managed object
					int workIndex = workMoRegistry
							.getIndexByWorkManagedObjectName(dependencyConfig
									.getManagedObjectName());

					// Register the mapping
					this.dependencyMapping.put((D) dependencyConfig
							.getDependencyKey(), new Integer(workIndex));
				}
			}
		}

		// Obtain this managed object index
		int moIndex = workMoRegistry.getIndex(this);

		// Create the listing of all dependencies for this managed object
		Set<Integer> dependencies = new HashSet<Integer>();
		this.loadDependencies(dependencies, moIndex, workMoIndexes,
				processMoIndexes, workMoRegistry);
		Integer[] dependencyListing = dependencies.toArray(new Integer[0]);
		this.dependencyWorkIndexes = new int[dependencyListing.length];
		for (int i = 0; i < this.dependencyWorkIndexes.length; i++) {
			this.dependencyWorkIndexes[i] = dependencyListing[i].intValue();
		}
	}

	/**
	 * Load the dependencies.
	 * 
	 * @param dependencies
	 *            {@link Set} of dependency indexes.
	 * @param moIndex
	 *            Index of the {@link ManagedObject}.
	 * @param workMoIndexes
	 *            Translation of {@link net.officefloor.frame.api.execute.Work}
	 *            {@link ManagedObject} name to its index.
	 * @param processMoIndexes
	 *            Translation of
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}
	 *            {@link ManagedObject} name to its index.
	 * @param workMoRegistry
	 *            {@link RawWorkManagedObjectRegistry}.
	 * @throws ConfigurationException
	 *             If fails configuration.
	 */
	private void loadDependencies(Set<Integer> dependencies, int moIndex,
			Map<String, Integer> workMoIndexes,
			Map<String, Integer> processMoIndexes,
			RawWorkManagedObjectRegistry workMoRegistry)
			throws ConfigurationException {

		// Do not continue if already contains dependency
		if (dependencies.contains(new Integer(moIndex))) {
			return;
		}

		// Add the dependency
		dependencies.add(new Integer(moIndex));

		// Obtain the raw work managed object meta-data
		RawWorkManagedObjectMetaData<?> rawMoMetaData = workMoRegistry
				.getRawWorkManagedObjectMetaData()[moIndex];

		// Handle based on bounding
		if (rawMoMetaData.isProcessBound()) {
			// Process bound managed object
			for (String dependencyName : rawMoMetaData
					.getRawProcessManagedObjectMetaData().getDependencyIds()) {

				// Obtain the index for the dependency
				int dependencyIndex = workMoRegistry
						.getIndexByProcessManagedObjectName(dependencyName);

				// Load the dependency
				this.loadDependencies(dependencies, dependencyIndex,
						workMoIndexes, processMoIndexes, workMoRegistry);
			}

		} else {
			// Work bound managed object
			for (ManagedObjectDependencyConfiguration<?> dependencyConfig : rawMoMetaData
					.getWorkManagedObjectConfiguration()
					.getDependencyConfiguration()) {

				// Obtain the index for the dependency
				int dependencyIndex = workMoRegistry
						.getIndexByWorkManagedObjectName(dependencyConfig
								.getManagedObjectName());

				// Load the dependency
				this.loadDependencies(dependencies, dependencyIndex,
						workMoIndexes, processMoIndexes, workMoRegistry);
			}
		}
	}

	/**
	 * Returns <code>true</code> if this {@link ManagedObject} is
	 * {@link net.officefloor.frame.internal.structure.ProcessState} bound.
	 * 
	 * @return <code>true</code> if this {@link ManagedObject} is
	 *         {@link net.officefloor.frame.internal.structure.ProcessState}
	 *         bound.
	 */
	public boolean isProcessBound() {
		return this.isProcessBound;
	}

	/**
	 * Obtains the {@link ManagedObjectConfiguration} for the
	 * {@link net.officefloor.frame.api.execute.Work} bound
	 * {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectConfiguration} for the
	 *         {@link net.officefloor.frame.api.execute.Work} bound
	 *         {@link ManagedObject}.
	 */
	public ManagedObjectConfiguration getWorkManagedObjectConfiguration() {
		return this.workManagedObjectConfig;
	}

	/**
	 * Obtains the {@link LinkedManagedObjectSourceConfiguration} to link the
	 * {@link net.officefloor.frame.internal.structure.ProcessState} bound
	 * {@link ManagedObject} to the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @return {@link LinkedManagedObjectSourceConfiguration} to link the
	 *         {@link net.officefloor.frame.internal.structure.ProcessState}
	 *         bound {@link ManagedObject} to the
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 */
	public LinkedManagedObjectSourceConfiguration getProcessManagedObjectConfiguration() {
		return this.processManagedObjectConfig;
	}

	/**
	 * Obtains the {@link RawProcessManagedObjectMetaData}.
	 * 
	 * @return {@link RawProcessManagedObjectMetaData}.
	 */
	public RawProcessManagedObjectMetaData getRawProcessManagedObjectMetaData() {
		return this.processMoMetaData;
	}

	/**
	 * Obtains the {@link ManagedObjectMetaData}.
	 * 
	 * @return {@link ManagedObjectMetaData}.
	 */
	public ManagedObjectMetaData<D> getManagedObjectMetaData() {
		return this.metaData;
	}

	/**
	 * Obtains the {@link net.officefloor.frame.api.execute.Work} dependency
	 * indexes for this {@link ManagedObject}.
	 * 
	 * @return {@link net.officefloor.frame.api.execute.Work} dependency indexes
	 *         for this {@link ManagedObject}.
	 */
	public int[] getDependencyWorkIndexes() {
		return this.dependencyWorkIndexes;
	}

}
