/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.ProcessManager;
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
	 * @param functionMetaData {@link ManagedFunctionMetaData}.
	 * @param officeMetaData   {@link OfficeMetaData}.
	 */
	public FunctionManagerImpl(ManagedFunctionMetaData<?, ?> functionMetaData, OfficeMetaData officeMetaData) {
		this.functionMetaData = functionMetaData;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * ===================== FunctionManager ============================
	 */

	@Override
	public Object[] getAnnotations() {
		return this.functionMetaData.getAnnotations();
	}

	@Override
	public Class<?> getParameterType() {
		return this.functionMetaData.getParameterType();
	}

	@Override
	public ProcessManager invokeProcess(Object parameter, FlowCallback callback) throws InvalidParameterTypeException {

		// Create the managed execution
		ManagedExecution<InvalidParameterTypeException> execution = this.officeMetaData.getManagedExecutionFactory()
				.createManagedExecution(this.officeMetaData.getExecutive(), () -> {
					// Invoke the process for the function
					return this.officeMetaData.invokeProcess(this.flowMetaData, parameter, 0, callback, null, null,
							null, -1);
				});

		// Execute
		return execution.managedExecute();
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
