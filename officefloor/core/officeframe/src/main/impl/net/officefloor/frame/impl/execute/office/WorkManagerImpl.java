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
package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * Implementation of the {@link WorkManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkManagerImpl implements WorkManager {

	/**
	 * {@link WorkMetaData}.
	 */
	private final WorkMetaData<?> workMetaData;

	/**
	 * {@link OfficeMetaData} of the {@link Office} where this {@link Work} is
	 * being executed.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * Initiate.
	 * 
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 */
	public WorkManagerImpl(WorkMetaData<?> workMetaData, OfficeMetaData officeMetaData) {
		this.workMetaData = workMetaData;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * =============== WorkManager ================================
	 */

	@Override
	public String[] getTaskNames() {

		// Create the listing of task names
		ManagedFunctionMetaData<?, ?, ?>[] taskMetaData = this.workMetaData.getTaskMetaData();
		String[] taskNames = new String[taskMetaData.length];
		for (int i = 0; i < taskNames.length; i++) {
			taskNames[i] = taskMetaData[i].getFunctionName();
		}

		// Return the task names
		return taskNames;
	}

	@Override
	public TaskManager getTaskManager(String taskName) throws UnknownTaskException {

		// Obtain the task meta-data for the task
		for (ManagedFunctionMetaData<?, ?, ?> taskMetaData : this.workMetaData.getTaskMetaData()) {
			if (taskMetaData.getFunctionName().equals(taskName)) {
				// Have the task meta-data, so return a task manager for it
				return new TaskManagerImpl(taskMetaData, this.officeMetaData);
			}
		}

		// Unknown task if at this point
		throw new UnknownTaskException(taskName);
	}

}