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

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.FlowMetaDataImpl;
import net.officefloor.frame.impl.execute.WorkMetaDataImpl;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * Raw meta-data for the {@link net.officefloor.frame.api.execute.Work}.
 * 
 * @author Daniel
 */
public class RawWorkMetaData {

	/**
	 * Current {@link Work} Id.
	 */
	protected static int currentWorkId = 1;

	/**
	 * Creates a {@link RawWorkMetaData}.
	 * 
	 * @param workConfig
	 *            {@link WorkConfiguration}.
	 * @param officeResources
	 *            Resources of the
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @param rawAdminRegistry
	 *            {@link RawAdministratorRegistry}.
	 * @return {@link RawWorkMetaData}.
	 * @throws Exception
	 *             If fails to create registry.
	 */
	@SuppressWarnings("unchecked")
	public static RawWorkMetaData createRawWorkMetaData(
			WorkConfiguration<?> workConfig,
			RawOfficeResourceRegistry officeResources,
			RawAdministratorRegistry rawAdminRegistry,
			RawAssetManagerRegistry rawAssetRegistry) throws Exception {

		// Obtain the work name
		String workName = workConfig.getWorkName();

		// Obtain the Work factory
		WorkFactory<?> workFactory = workConfig.getWorkFactory();
		if (workFactory == null) {
			throw new NullPointerException("Work configuration '" + workName
					+ "' must provide a " + WorkFactory.class.getName()
					+ " implementation");
		}

		// Create the registry of the Work Managed Object meta-data
		RawWorkManagedObjectRegistry wmoRegistry = RawWorkManagedObjectRegistry
				.createWorkManagedObjectRegistry(workConfig, officeResources);

		// Create the registry of the Work Administrator meta-data
		RawWorkAdministratorRegistry wadminRegistry = RawWorkAdministratorRegistry
				.createWorkAdministratorRegistry(workConfig, officeResources,
						rawAdminRegistry, wmoRegistry);

		// Create the registry of the Task meta-data for the Work
		RawTaskRegistry taskRegistry = RawTaskRegistry.createTaskRegistry(
				workConfig, officeResources, wmoRegistry, wadminRegistry);

		// Obtain the initial Task meta-data
		String initialTaskName = workConfig.getInitialTaskName();
		RawTaskMetaData initialRawTaskMetaData = taskRegistry
				.getRawTaskMetaData(initialTaskName);
		if (initialRawTaskMetaData == null) {
			throw new ConfigurationException("Unknown Task '" + initialTaskName
					+ "' for initial Task of Work '" + workName + "'");
		}
		TaskMetaData<?, ?, ?, ?> initialTaskMetaData = initialRawTaskMetaData
				.getTaskMetaData();

		// Create the Flow Manager for the initial flow of the work
		AssetManager flowManager = rawAssetRegistry
				.createAssetManager("Initial Work Flow - " + workName);

		// Create the initial Flow meta-data (invoke always asynchronously)
		FlowMetaData<?> initialFlowMetaData = new FlowMetaDataImpl(
				FlowInstigationStrategyEnum.ASYNCHRONOUS, initialTaskMetaData,
				flowManager);

		// Return the raw work meta-data
		return new RawWorkMetaData(new WorkMetaDataImpl(currentWorkId++,
				workFactory, wmoRegistry.getWorkManagedObjectListing(),
				wadminRegistry.getWorkAdministratorListing(),
				initialFlowMetaData), workConfig, taskRegistry);
	}

	/**
	 * {@link WorkMetaData}.
	 */
	protected final WorkMetaData<?> workMetaData;

	/**
	 * {@link WorkConfiguration} for this {@link RawWorkMetaData}.
	 */
	protected final WorkConfiguration<?> workConfig;

	/**
	 * Registry of {@link TaskMetaData}.
	 */
	protected final RawTaskRegistry taskRegistry;

	/**
	 * Initiate.
	 * 
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @param workConfig
	 *            {@link WorkConfiguration}.
	 * @param taskRegistry
	 *            Registry of {@link TaskMetaData}.
	 */
	private RawWorkMetaData(WorkMetaData<?> workMetaData,
			WorkConfiguration<?> workConfig, RawTaskRegistry taskRegistry) {
		this.workMetaData = workMetaData;
		this.workConfig = workConfig;
		this.taskRegistry = taskRegistry;
	}

	/**
	 * Obtains the {@link WorkMetaData} for this {@link RawWorkMetaData}.
	 * 
	 * @return {@link WorkMetaData} for this {@link RawWorkMetaData}.
	 */
	public WorkMetaData<?> getWorkMetaData() {
		return this.workMetaData;
	}

	/**
	 * Obtains the {@link RawTaskRegistry} for this {@link Work}.
	 * 
	 * @return {@link RawTaskRegistry}.
	 */
	public RawTaskRegistry getTaskRegistry() {
		return this.taskRegistry;
	}

}
