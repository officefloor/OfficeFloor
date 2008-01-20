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
import java.util.Map;

import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.structure.TaskMetaData;

/**
 * Registry of {@link net.officefloor.frame.impl.RawTaskMetaData}.
 * 
 * @author Daniel
 */
public class RawTaskRegistry {

	/**
	 * Creates the registry of {@link TaskMetaData} for the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @param workConfig
	 *            {@link WorkConfiguration}.
	 * @param officeResources
	 *            Resources of the
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @param workAdminRegistry
	 *            Registry of the {@link RawWorkAdministratorMetaData}.
	 * @return Registry of {@link TaskMetaData} for the
	 *         {@link net.officefloor.frame.api.execute.Work}.
	 * @throws Exception
	 *             If fails.
	 */
	public static RawTaskRegistry createTaskRegistry(
			WorkConfiguration<?> workConfig,
			RawOfficeResourceRegistry officeResources,
			RawWorkManagedObjectRegistry workMoRegistry,
			RawWorkAdministratorRegistry workAdminRegistry) throws Exception {

		// Create the Task registry
		Map<String, RawTaskMetaData> taskRegistry = new HashMap<String, RawTaskMetaData>();
		for (TaskConfiguration<?, ?, ?, ?> taskConfiguration : workConfig
				.getTaskConfiguration()) {
			taskRegistry.put(taskConfiguration.getTaskName(), RawTaskMetaData
					.createRawTaskMetaData(taskConfiguration, officeResources,
							workMoRegistry, workAdminRegistry));
		}

		// Return Task registry
		return new RawTaskRegistry(taskRegistry);
	}

	/**
	 * Registry of the {@link RawTaskMetaData}.
	 */
	protected final Map<String, RawTaskMetaData> taskRegistry;

	/**
	 * Initiate.
	 * 
	 * @param taskRegistry
	 *            Registry of the {@link RawTaskMetaData}.
	 */
	private RawTaskRegistry(Map<String, RawTaskMetaData> taskRegistry) {
		this.taskRegistry = taskRegistry;
	}

	/**
	 * Obtains the {@link RawTaskMetaData} by its name.
	 * 
	 * @param taskName
	 *            Name of {@link net.officefloor.frame.api.execute.Task}.
	 * @return {@link RawTaskMetaData} by its name.
	 */
	public RawTaskMetaData getRawTaskMetaData(String taskName) {
		return this.taskRegistry.get(taskName);
	}

	/**
	 * Return the listing of {@link net.officefloor.frame.api.execute.Task}
	 * names.
	 * 
	 * @return Listing of {@link net.officefloor.frame.api.execute.Task} names.
	 */
	public String[] getTaskNames() {
		return this.taskRegistry.keySet().toArray(new String[0]);
	}

}
