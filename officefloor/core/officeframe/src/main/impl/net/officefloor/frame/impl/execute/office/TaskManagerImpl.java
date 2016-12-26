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

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;

/**
 * {@link FunctionManager} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskManagerImpl implements FunctionManager {

	/**
	 * {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionMetaData<?, ?, ?> taskMetaData;

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link FlowMetaData} for this {@link FunctionManager}.
	 */
	private final FlowMetaData<?> flowMetaData = new TaskManagerFlowMetaData();

	/**
	 * Initiate.
	 * 
	 * @param taskMetaData
	 *            {@link ManagedFunctionMetaData}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 */
	public TaskManagerImpl(ManagedFunctionMetaData<?, ?, ?> taskMetaData, OfficeMetaData officeMetaData) {
		this.taskMetaData = taskMetaData;
		this.officeMetaData = officeMetaData;
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
	public void invokeFunction(Object parameter, ProcessCompletionListener completionListener)
			throws InvalidParameterTypeException {
		// Invoke the process for the task
		OfficeMetaDataImpl.invokeProcess(this.officeMetaData, this.flowMetaData, parameter, completionListener);
	}

	/**
	 * {@link FlowMetaData} for invoking a {@link ManagedFunction} by this
	 * {@link FunctionManager}.
	 */
	@SuppressWarnings("rawtypes")
	private class TaskManagerFlowMetaData implements FlowMetaData {

		@Override
		public ManagedFunctionMetaData<?, ?, ?> getInitialTaskMetaData() {
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