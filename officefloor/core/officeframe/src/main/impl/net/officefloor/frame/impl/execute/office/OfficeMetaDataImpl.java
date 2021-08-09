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

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveOfficeContext;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.manage.StateManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.process.ProcessStateImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.OfficeManagerHirer;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupFunction;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link OfficeMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeMetaDataImpl implements OfficeMetaData {

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link OfficeManagerHirer}.
	 */
	private final OfficeManagerHirer officeManagerHirer;

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock monitorClock;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * {@link ManagedFunctionMetaData} of the {@link ManagedFunction} that can be
	 * executed within the {@link Office}.
	 */
	private final ManagedFunctionMetaData<?, ?>[] functionMetaDatas;

	/**
	 * {@link ManagedFunctionLocator}.
	 */
	private final ManagedFunctionLocator functionLocator;

	/**
	 * {@link ProcessMetaData} of the {@link ProcessState} instances created within
	 * this {@link Office}.
	 */
	private final ProcessMetaData processMetaData;

	/**
	 * {@link ManagedFunctionMetaData} of the {@link ManagedFunction} that keeps the
	 * {@link StateManager} active.
	 */
	private final ManagedFunctionMetaData<?, ?> stateKeepAliveFunctionMetaData;

	/**
	 * Load object {@link ManagedFunctionMetaData} by {@link ManagedObject} bound
	 * name.
	 */
	private final Map<String, ManagedFunctionMetaData<?, ?>> loadObjectMetaDatas;

	/**
	 * {@link OfficeStartupFunction} instances.
	 */
	private final OfficeStartupFunction[] startupFunctions;

	/**
	 * {@link ThreadLocalAwareExecutor}.s
	 */
	private final ThreadLocalAwareExecutor threadLocalAwareExecutor;

	/**
	 * {@link Executive}.
	 */
	private final Executive executive;

	/**
	 * {@link ManagedExecutionFactory}.
	 */
	private final ManagedExecutionFactory managedExecutionFactory;

	/**
	 * {@link Profiler}.
	 */
	private final Profiler profiler;

	/**
	 * Default {@link OfficeManager}.
	 */
	private OfficeManager defaultOfficeManager;

	/**
	 * Initiate.
	 * 
	 * @param officeName                     Name of the {@link Office}.
	 * @param officeManagerHirer             {@link OfficeManagerHirer}.
	 * @param monitorClock                   {@link MonitorClock}.
	 * @param functionLoop                   {@link FunctionLoop}.
	 * @param threadLocalAwareExecutor       {@link ThreadLocalAwareExecutor}.
	 * @param executive                      {@link Executive}.
	 * @param managedExecutionFactory        {@link ManagedExecutionFactory}.
	 * @param functionMetaDatas              {@link ManagedFunctionMetaData} of the
	 *                                       {@link ManagedFunction} that can be
	 *                                       executed within the {@link Office}.
	 * @param functionLocator                {@link ManagedFunctionLocator}.
	 * @param processMetaData                {@link ProcessMetaData} of the
	 *                                       {@link ProcessState} instances created
	 *                                       within this {@link Office}.
	 * @param stateKeepAliveFunctionMetaData {@link ManagedFunctionMetaData} of the
	 *                                       {@link ManagedFunction} that keeps the
	 *                                       {@link StateManager} active.
	 * @param loadObjectMetaDatas            Load object
	 *                                       {@link ManagedFunctionMetaData} by
	 *                                       {@link ManagedObject} bound name.
	 * @param startupFunctions               {@link OfficeStartupFunction}
	 *                                       instances.
	 * @param profiler                       {@link Profiler}.
	 */
	public OfficeMetaDataImpl(String officeName, OfficeManagerHirer officeManagerHirer, MonitorClock monitorClock,
			FunctionLoop functionLoop, ThreadLocalAwareExecutor threadLocalAwareExecutor, Executive executive,
			ManagedExecutionFactory managedExecutionFactory, ManagedFunctionMetaData<?, ?>[] functionMetaDatas,
			ManagedFunctionLocator functionLocator, ProcessMetaData processMetaData,
			ManagedFunctionMetaData<?, ?> stateKeepAliveFunctionMetaData,
			Map<String, ManagedFunctionMetaData<?, ?>> loadObjectMetaDatas, OfficeStartupFunction[] startupFunctions,
			Profiler profiler) {
		this.officeName = officeName;
		this.officeManagerHirer = officeManagerHirer;
		this.monitorClock = monitorClock;
		this.functionLoop = functionLoop;
		this.threadLocalAwareExecutor = threadLocalAwareExecutor;
		this.executive = executive;
		this.managedExecutionFactory = managedExecutionFactory;
		this.functionMetaDatas = functionMetaDatas;
		this.functionLocator = functionLocator;
		this.processMetaData = processMetaData;
		this.stateKeepAliveFunctionMetaData = stateKeepAliveFunctionMetaData;
		this.loadObjectMetaDatas = loadObjectMetaDatas;
		this.startupFunctions = startupFunctions;
		this.profiler = profiler;
	}

	/*
	 * ==================== OfficeMetaData ==============================
	 */

	@Override
	public String getOfficeName() {
		return this.officeName;
	}

	@Override
	public ProcessIdentifier createProcessIdentifier(ProcessState processState) {
		return this.executive.createProcessIdentifier(new ExecutiveOfficeContext() {

			@Override
			public String getOfficeName() {
				return OfficeMetaDataImpl.this.officeName;
			}

			@Override
			public OfficeManager hireOfficeManager() {
				return OfficeMetaDataImpl.this.officeManagerHirer.hireOfficeManager(processState);
			}
		});
	}

	@Override
	public OfficeManager getOfficeManager(ProcessIdentifier processIdentifier) {
		return this.executive.getOfficeManager(processIdentifier, this.defaultOfficeManager);
	}

	@Override
	public Executor getExecutor(ProcessIdentifier processIdentifier) {
		return this.executive.createExecutor(processIdentifier);
	}

	@Override
	public MonitorClock getMonitorClock() {
		return this.monitorClock;
	}

	@Override
	public FunctionLoop getFunctionLoop() {
		return this.functionLoop;
	}

	@Override
	public ProcessMetaData getProcessMetaData() {
		return this.processMetaData;
	}

	@Override
	public ManagedFunctionMetaData<?, ?>[] getManagedFunctionMetaData() {
		return this.functionMetaDatas;
	}

	@Override
	public ManagedFunctionLocator getManagedFunctionLocator() {
		return this.functionLocator;
	}

	@Override
	public OfficeStartupFunction[] getStartupFunctions() {
		return this.startupFunctions;
	}

	@Override
	public Executive getExecutive() {
		return this.executive;
	}

	@Override
	public ManagedExecutionFactory getManagedExecutionFactory() {
		return this.managedExecutionFactory;
	}

	@Override
	public FunctionState createProcess(FlowMetaData flowMetaData, Object parameter, FlowCallback callback,
			ThreadState callbackThreadState) {
		return this.createProcess(flowMetaData, parameter, callback, callbackThreadState, null, null, -1);
	}

	@Override
	public OfficeManager setupDefaultOfficeManager() {

		// Undertake creation of default office manager
		this.createMainThread(null, null, null, null, -1, (processState) -> {
			this.defaultOfficeManager = this.officeManagerHirer.hireOfficeManager(processState);
		});

		// Return the default office manager
		return this.defaultOfficeManager;
	}

	@Override
	public StateManager createStateManager() {

		// Create main thread for scope of managed object state
		ThreadState threadState = this.createMainThread(null, null, null, null, -1, null);

		// Create function that keeps thread scope active
		FunctionState functionState = threadState.createFlow(null, null).createManagedFunction(null,
				this.stateKeepAliveFunctionMetaData, true, null);
		Runnable cleanUpState = () -> this.executeFunction(functionState);

		// Create and return the state manager
		return new StateManagerImpl(this.loadObjectMetaDatas, threadState, this.monitorClock, this::executeFunction,
				cleanUpState);
	}

	@Override
	public ProcessManager invokeProcess(FlowMetaData flowMetaData, Object parameter, long delay, FlowCallback callback,
			ThreadState callbackThreadState, ManagedObject inputManagedObject,
			ManagedObjectMetaData<?> inputManagedObjectMetaData, int processBoundIndexForInputManagedObject)
			throws InvalidParameterTypeException {

		// Obtain the initial function meta-data
		ManagedFunctionMetaData<?, ?> initialFunctionMetaData = flowMetaData.getInitialFunctionMetaData();

		// Ensure correct parameter type
		if (parameter != null) {
			Class<?> functionParameterType = initialFunctionMetaData.getParameterType();
			if (functionParameterType != null) {
				Class<?> inputParameterType = parameter.getClass();
				if (!functionParameterType.isAssignableFrom(inputParameterType)) {
					throw new InvalidParameterTypeException("Invalid parameter type (input="
							+ inputParameterType.getName() + ", required=" + functionParameterType.getName() + ")");
				}
			}
		}

		// Create the process
		final FunctionState function = this.createProcess(flowMetaData, parameter, callback, callbackThreadState,
				inputManagedObject, inputManagedObjectMetaData, processBoundIndexForInputManagedObject);

		// Obtain the process state
		ProcessState processState = function.getThreadState().getProcessState();

		// Trigger the process
		if (delay > 0) {

			// Delay execution of the process
			ProcessIdentifier processIdentifier = processState.getProcessIdentifier();
			this.executive.schedule(processIdentifier, delay, () -> this.executeFunction(function));

		} else {
			// Execute the process immediately on current thread
			this.executeFunction(function);
		}

		// Return the process manager
		ProcessManager processManager = processState.getProcessManager();
		return processManager;
	}

	/**
	 * Creates the main {@link ThreadState} for a new {@link ProcessState}.
	 * 
	 * @param callback                               Optional {@link FlowCallback}
	 *                                               to invoke on completion of the
	 *                                               {@link ProcessState}. May be
	 *                                               <code>null</code>.
	 * @param callbackThreadState                    Optional {@link ThreadState} to
	 *                                               invoked the
	 *                                               {@link FlowCallback} within.
	 *                                               May be <code>null</code>.
	 * @param inputManagedObject                     Input {@link ManagedObject}.
	 *                                               May be <code>null</code> if no
	 *                                               input {@link ManagedObject}.
	 * @param inputManagedObjectMetaData             {@link ManagedObjectMetaData}
	 *                                               to the input
	 *                                               {@link ManagedObject}.
	 * @param processBoundIndexForInputManagedObject Index of the input
	 *                                               {@link ManagedObject} within
	 *                                               the {@link ProcessState}.
	 * @param initialSetup                           Initial setup with
	 *                                               {@link ProcessState} before the
	 *                                               {@link ProcessState}
	 *                                               initialises.
	 * @return Main {@link ThreadState} for a new {@link ProcessState}.
	 */
	private ThreadState createMainThread(FlowCallback callback, ThreadState callbackThreadState,
			ManagedObject inputManagedObject, ManagedObjectMetaData<?> inputManagedObjectMetaData,
			int processBoundIndexForInputManagedObject, Consumer<ProcessState> initialSetup) {

		// Create the Process State (based on whether have managed object)
		ProcessState processState;
		if (inputManagedObject == null) {
			// Create Process without an Input Managed Object
			processState = new ProcessStateImpl(this.processMetaData, this, callback, callbackThreadState,
					this.threadLocalAwareExecutor, this.profiler, initialSetup);
		} else {
			// Create Process with the Input Managed Object
			processState = new ProcessStateImpl(this.processMetaData, this, callback, callbackThreadState,
					this.threadLocalAwareExecutor, this.profiler, inputManagedObject, inputManagedObjectMetaData,
					processBoundIndexForInputManagedObject, initialSetup);
		}

		// Create the main thread
		ThreadState threadState = processState.getMainThreadState();

		// Return the main thread
		return threadState;
	}

	/**
	 * Creates a new {@link ProcessState}.
	 * 
	 * @param flowMetaData                           {@link FlowMetaData} for the
	 *                                               initial
	 *                                               {@link ManagedFunction}.
	 * @param parameter                              Parameter for the initial
	 *                                               {@link ManagedFunction}.
	 * @param callback                               Optional {@link FlowCallback}
	 *                                               to invoke on completion of the
	 *                                               {@link ProcessState}. May be
	 *                                               <code>null</code>.
	 * @param callbackThreadState                    Optional {@link ThreadState} to
	 *                                               invoked the
	 *                                               {@link FlowCallback} within.
	 *                                               May be <code>null</code>.
	 * @param inputManagedObject                     Input {@link ManagedObject}.
	 *                                               May be <code>null</code> if no
	 *                                               input {@link ManagedObject}.
	 * @param inputManagedObjectMetaData             {@link ManagedObjectMetaData}
	 *                                               to the input
	 *                                               {@link ManagedObject}.
	 * @param processBoundIndexForInputManagedObject Index of the input
	 *                                               {@link ManagedObject} within
	 *                                               the {@link ProcessState}.
	 * @return Initial {@link FunctionState} to be executed for the
	 *         {@link ProcessState}.
	 */
	private FunctionState createProcess(FlowMetaData flowMetaData, Object parameter, FlowCallback callback,
			ThreadState callbackThreadState, ManagedObject inputManagedObject,
			ManagedObjectMetaData<?> inputManagedObjectMetaData, int processBoundIndexForInputManagedObject) {

		// Create the main thread
		ThreadState threadState = this.createMainThread(callback, callbackThreadState, inputManagedObject,
				inputManagedObjectMetaData, processBoundIndexForInputManagedObject, null);

		// Create the flow
		Flow flow = threadState.createFlow(null, null);

		// Obtain the function meta-data
		ManagedFunctionMetaData<?, ?> functionMetaData = flowMetaData.getInitialFunctionMetaData();

		// Create the initial function of the process
		FunctionState function = flow.createManagedFunction(parameter, functionMetaData, true, null);

		// Ensure register main thread profiler
		FunctionState registerThreadProfiler = function.getThreadState().registerThreadProfiler();
		function = Promise.then(registerThreadProfiler, function);

		// Return the function
		return function;
	}

	/**
	 * Executes the {@link FunctionState}.
	 * 
	 * @param function {@link FunctionState} to execute.
	 */
	private void executeFunction(FunctionState function) {

		// Execute the function allowing possible thread local awareness
		if (this.threadLocalAwareExecutor != null) {
			this.threadLocalAwareExecutor.runInContext(function, this.functionLoop);
		} else {
			this.functionLoop.executeFunction(function);
		}
	}

}
