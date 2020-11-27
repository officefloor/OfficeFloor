/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.office;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

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
	 * Default {@link OfficeManager}.
	 */
	private final OfficeManager defaultOfficeManager;

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock monitorClock;

	/**
	 * {@link Timer} for the {@link Office}.
	 */
	private final Timer timer;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * {@link Executor} to break the thread stack execution chain.
	 */
	private final Executor breakChainExecutor;

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
	 * Initiate.
	 * 
	 * @param officeName                     Name of the {@link Office}.
	 * @param officeManagerHirer             {@link OfficeManagerHirer}.
	 * @param defaultOfficeManager           Default {@link OfficeManager}.
	 * @param monitorClock                   {@link MonitorClock}.
	 * @param timer                          {@link Timer} for the {@link Office}.
	 * @param functionLoop                   {@link FunctionLoop}.
	 * @param breakChainExecutor             {@link Executor} to break the thread
	 *                                       stack execution chain.
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
	public OfficeMetaDataImpl(String officeName, OfficeManagerHirer officeManagerHirer,
			OfficeManager defaultOfficeManager, MonitorClock monitorClock, Timer timer, FunctionLoop functionLoop,
			Executor breakChainExecutor, ThreadLocalAwareExecutor threadLocalAwareExecutor, Executive executive,
			ManagedExecutionFactory managedExecutionFactory, ManagedFunctionMetaData<?, ?>[] functionMetaDatas,
			ManagedFunctionLocator functionLocator, ProcessMetaData processMetaData,
			ManagedFunctionMetaData<?, ?> stateKeepAliveFunctionMetaData,
			Map<String, ManagedFunctionMetaData<?, ?>> loadObjectMetaDatas, OfficeStartupFunction[] startupFunctions,
			Profiler profiler) {
		this.officeName = officeName;
		this.officeManagerHirer = officeManagerHirer;
		this.defaultOfficeManager = defaultOfficeManager;
		this.monitorClock = monitorClock;
		this.timer = timer;
		this.functionLoop = functionLoop;
		this.breakChainExecutor = breakChainExecutor;
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
	public StateManager createStateManager() {

		// Create main thread for scope of managed object state
		ThreadState threadState = this.createMainThread(null, null, null, null, -1);

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

		// Obtain the process manager
		ProcessManager processManager = function.getThreadState().getProcessState().getProcessManager();

		// Trigger the process
		if (delay > 0) {

			// Delay execution of the process
			this.timer.schedule(new TimerTask() {
				@Override
				public void run() {

					// Easy access to office meta-data
					OfficeMetaDataImpl officeMetaData = OfficeMetaDataImpl.this;

					// Must execute on another thread (not hold up timer thread)
					officeMetaData.breakChainExecutor.execute(() -> {

						// Execute the process
						officeMetaData.executeFunction(function);
					});
				}
			}, delay);

		} else {
			// Execute the process immediately on current thread
			this.executeFunction(function);
		}

		// Return the process manager
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
	 * @return Initial {@link FunctionState} to be executed for the
	 *         {@link ProcessState}.
	 * @return Main {@link ThreadState} for a new {@link ProcessState}.
	 */
	private ThreadState createMainThread(FlowCallback callback, ThreadState callbackThreadState,
			ManagedObject inputManagedObject, ManagedObjectMetaData<?> inputManagedObjectMetaData,
			int processBoundIndexForInputManagedObject) {

		// Create the Process State (based on whether have managed object)
		ProcessState processState;
		if (inputManagedObject == null) {
			// Create Process without an Input Managed Object
			processState = new ProcessStateImpl(this.processMetaData, this, callback, callbackThreadState,
					this.threadLocalAwareExecutor, this.profiler);
		} else {
			// Create Process with the Input Managed Object
			processState = new ProcessStateImpl(this.processMetaData, this, callback, callbackThreadState,
					this.threadLocalAwareExecutor, this.profiler, inputManagedObject, inputManagedObjectMetaData,
					processBoundIndexForInputManagedObject);
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
				inputManagedObjectMetaData, processBoundIndexForInputManagedObject);

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
