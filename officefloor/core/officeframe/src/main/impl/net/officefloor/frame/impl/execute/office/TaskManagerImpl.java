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
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessTicker;
import net.officefloor.frame.internal.structure.TaskMetaData;

/**
 * {@link TaskManager} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskManagerImpl implements TaskManager {

	/**
	 * {@link TaskMetaData}.
	 */
	private final TaskMetaData<?, ?, ?> taskMetaData;

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link FlowMetaData} for this {@link TaskManager}.
	 */
	private final FlowMetaData<?> flowMetaData = new TaskManagerFlowMetaData();

	/**
	 * {@link ProcessTicker}.
	 */
	private final ProcessTicker processTicker;

	/**
	 * Initiate.
	 * 
	 * @param taskMetaData
	 *            {@link TaskMetaData}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param processTicker
	 *            {@link ProcessTicker}.
	 */
	public TaskManagerImpl(TaskMetaData<?, ?, ?> taskMetaData,
			OfficeMetaData officeMetaData, ProcessTicker processTicker) {
		this.taskMetaData = taskMetaData;
		this.officeMetaData = officeMetaData;
		this.processTicker = processTicker;
	}

	/*
	 * ===================== TaskManager ============================
	 */

	@Override
	public Object getDifferentiator() {
		return this.taskMetaData.getDifferentiator();
	}

	@Override
	public Class<?> getParameterType() {
		return this.taskMetaData.getParameterType();
	}

	@Override
	public ProcessFuture invokeTask(Object parameter)
			throws InvalidParameterTypeException {

		// Invoke the process for the task
		ProcessFuture future = OfficeMetaDataImpl.invokeProcess(
				this.officeMetaData, this.flowMetaData, parameter,
				this.processTicker);

		// Indicate when process of task complete
		return future;
	}

	/**
	 * {@link FlowMetaData} for invoking a {@link Task} by this
	 * {@link TaskManager}.
	 */
	@SuppressWarnings("rawtypes")
	private class TaskManagerFlowMetaData implements FlowMetaData {

		@Override
		public TaskMetaData<?, ?, ?> getInitialTaskMetaData() {
			return TaskManagerImpl.this.taskMetaData;
		}

		@Override
		public FlowInstigationStrategyEnum getInstigationStrategy() {
			// Always sequential for invoking
			return FlowInstigationStrategyEnum.SEQUENTIAL;
		}

		@Override
		public AssetManager getFlowManager() {
			// No AssetManager required as can not join on initial thread
			return null;
		}
	}

}