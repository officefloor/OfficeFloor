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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.NoInitialTaskException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessTicker;
import net.officefloor.frame.internal.structure.TaskMetaData;
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
	 * {@link ProcessTicker}.
	 */
	private final ProcessTicker processTicker;

	/**
	 * Initiate.
	 * 
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param processTicker
	 *            {@link ProcessTicker}.
	 */
	public WorkManagerImpl(WorkMetaData<?> workMetaData,
			OfficeMetaData officeMetaData, ProcessTicker processTicker) {
		this.workMetaData = workMetaData;
		this.officeMetaData = officeMetaData;
		this.processTicker = processTicker;
	}

	/*
	 * =============== WorkManager ================================
	 */

	@Override
	public Class<?> getWorkParameterType() throws NoInitialTaskException {

		// Obtain the Initial Flow meta-data
		FlowMetaData<?> flowMetaData = this.getInitialFlowMetaData();

		// Return the parameter type for the initial task
		return flowMetaData.getInitialTaskMetaData().getParameterType();
	}

	@Override
	public ProcessFuture invokeWork(Object parameter)
			throws NoInitialTaskException, InvalidParameterTypeException {

		// Obtain the Initial Flow meta-data
		FlowMetaData<?> flowMetaData = this.getInitialFlowMetaData();

		// Invoke the process for the work
		ProcessFuture future = OfficeMetaDataImpl.invokeProcess(
				this.officeMetaData, flowMetaData, parameter,
				this.processTicker);

		// Indicate when process of work complete
		return future;
	}

	@Override
	public String[] getTaskNames() {

		// Create the listing of task names
		TaskMetaData<?, ?, ?>[] taskMetaData = this.workMetaData
				.getTaskMetaData();
		String[] taskNames = new String[taskMetaData.length];
		for (int i = 0; i < taskNames.length; i++) {
			taskNames[i] = taskMetaData[i].getTaskName();
		}

		// Return the task names
		return taskNames;
	}

	@Override
	public TaskManager getTaskManager(String taskName)
			throws UnknownTaskException {

		// Obtain the task meta-data for the task
		for (TaskMetaData<?, ?, ?> taskMetaData : this.workMetaData
				.getTaskMetaData()) {
			if (taskMetaData.getTaskName().equals(taskName)) {
				// Have the task meta-data, so return a task manager for it
				return new TaskManagerImpl(taskMetaData, this.officeMetaData,
						this.processTicker);
			}
		}

		// Unknown task if at this point
		throw new UnknownTaskException(taskName);
	}

	/**
	 * Obtains the initial {@link FlowMetaData}.
	 * 
	 * @return Initial {@link FlowMetaData}.
	 * @throws NoInitialTaskException
	 *             If no initial {@link Task}.
	 */
	private FlowMetaData<?> getInitialFlowMetaData()
			throws NoInitialTaskException {

		// Obtain the Initial Flow meta-data
		FlowMetaData<?> flowMetaData = this.workMetaData
				.getInitialFlowMetaData();

		// Ensure there is an initial task for the work
		if (flowMetaData == null) {
			throw new NoInitialTaskException("No initial task for work '"
					+ this.workMetaData.getWorkName() + "'");
		}

		// Returns the Initial Flow meta-data
		return flowMetaData;
	}

}