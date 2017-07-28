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

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedExecution;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;

/**
 * {@link FunctionManager} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionManagerImpl implements FunctionManager {

	/**
	 * {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionMetaData<?, ?> functionMetaData;

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link FlowMetaData} for this {@link FunctionManager}.
	 */
	private final FlowMetaData flowMetaData = new FunctionManagerFlowMetaData();

	/**
	 * Initiate.
	 * 
	 * @param functionMetaData
	 *            {@link ManagedFunctionMetaData}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 */
	public FunctionManagerImpl(ManagedFunctionMetaData<?, ?> functionMetaData, OfficeMetaData officeMetaData) {
		this.functionMetaData = functionMetaData;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * ===================== FunctionManager ============================
	 */

	@Override
	public Object getDifferentiator() {
		return this.functionMetaData.getDifferentiator();
	}

	@Override
	public Class<?> getParameterType() {
		return this.functionMetaData.getParameterType();
	}

	@Override
	public void invokeProcess(Object parameter, FlowCallback callback) throws InvalidParameterTypeException {

		// Create the managed execution
		ManagedExecution<InvalidParameterTypeException> execution = this.officeMetaData.getManagedExecutionFactory()
				.createManagedExecution(() -> {
					// Invoke the process for the function
					this.officeMetaData.invokeProcess(this.flowMetaData, parameter, 0, callback, null, null, null, -1);
				});

		// Execute
		execution.execute();
	}

	/**
	 * {@link FlowMetaData} for invoking a {@link ManagedFunction} by this
	 * {@link FunctionManager}.
	 */
	private class FunctionManagerFlowMetaData implements FlowMetaData {

		@Override
		public ManagedFunctionMetaData<?, ?> getInitialFunctionMetaData() {
			return FunctionManagerImpl.this.functionMetaData;
		}

		@Override
		public boolean isSpawnThreadState() {
			return false; // already in new process
		}
	}

}