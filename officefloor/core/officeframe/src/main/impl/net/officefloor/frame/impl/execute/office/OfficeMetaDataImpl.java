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

import java.util.Timer;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.api.team.source.ProcessContextListener;
import net.officefloor.frame.impl.execute.process.ProcessStateImpl;
import net.officefloor.frame.impl.execute.profile.ProcessProfilerImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeClock;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupFunction;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link OfficeMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeMetaDataImpl implements OfficeMetaData {

	/**
	 * <p>
	 * Convenience method to invoke a {@link Process}.
	 * <p>
	 * This is used by the {@link WorkManagerImpl} and
	 * {@link FunctionManagerImpl} for invoking a {@link ProcessState}.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter.
	 * @param callback
	 *            Optional {@link FlowCallback}. May be <code>null</code>.
	 * @throws InvalidParameterTypeException
	 *             Should the parameter type be incorrect the
	 *             {@link ManagedFunction}.
	 */
	public static void invokeProcess(OfficeMetaData officeMetaData, FlowMetaData flowMetaData, Object parameter,
			FlowCallback callback) throws InvalidParameterTypeException {

		// Ensure correct parameter type
		if (parameter != null) {
			Class<?> taskParameterType = flowMetaData.getInitialFunctionMetaData().getParameterType();
			if (taskParameterType != null) {
				Class<?> inputParameterType = parameter.getClass();
				if (!taskParameterType.isAssignableFrom(inputParameterType)) {
					throw new InvalidParameterTypeException("Invalid parameter type (input="
							+ inputParameterType.getName() + ", required=" + taskParameterType.getName() + ")");
				}
			}
		}

		// Create the function within a new process
		ManagedFunctionContainer function = officeMetaData.createProcess(flowMetaData, parameter, callback, null);

		// Execute the function (will delegate as required)
		FunctionLoop functionLoop = officeMetaData.getFunctionLoop();
		functionLoop.executeFunction(function);
	}

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link OfficeManager}.
	 */
	private final OfficeManager officeManager;

	/**
	 * {@link OfficeClock}.
	 */
	private final OfficeClock officeClock;

	/**
	 * {@link Timer} for the {@link Office}.
	 */
	private final Timer timer;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * {@link ManagedFunctionMetaData} of the {@link ManagedFunction} that can
	 * be executed within the {@link Office}.
	 */
	private final ManagedFunctionMetaData<?, ?>[] functionMetaDatas;

	/**
	 * {@link ManagedFunctionLocator}.
	 */
	private final ManagedFunctionLocator functionLocator;

	/**
	 * {@link ProcessMetaData} of the {@link ProcessState} instances created
	 * within this {@link Office}.
	 */
	private final ProcessMetaData processMetaData;

	/**
	 * {@link ProcessContextListener} instances.
	 */
	private final ProcessContextListener[] processContextListeners;

	/**
	 * {@link OfficeStartupFunction} instances.
	 */
	private final OfficeStartupFunction[] startupFunctions;

	/**
	 * {@link Profiler}.
	 */
	private final Profiler profiler;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param officeManager
	 *            {@link OfficeManager}.
	 * @param officeClock
	 *            {@link OfficeClock}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 * @param functionMetaDatas
	 *            {@link ManagedFunctionMetaData} of the {@link ManagedFunction}
	 *            that can be executed within the {@link Office}.
	 * @param functionLocator
	 *            {@link ManagedFunctionLocator}.
	 * @param processMetaData
	 *            {@link ProcessMetaData} of the {@link ProcessState} instances
	 *            created within this {@link Office}.
	 * @param processContextListeners
	 *            {@link ProcessContextListener} instances.
	 * @param startupFunctions
	 *            {@link OfficeStartupFunction} instances.
	 * @param profiler
	 *            {@link Profiler}.
	 */
	public OfficeMetaDataImpl(String officeName, OfficeManager officeManager, OfficeClock officeClock, Timer timer,
			FunctionLoop functionLoop, ManagedFunctionMetaData<?, ?>[] functionMetaDatas,
			ManagedFunctionLocator functionLocator, ProcessMetaData processMetaData,
			ProcessContextListener[] processContextListeners, OfficeStartupFunction[] startupFunctions,
			Profiler profiler) {
		this.officeName = officeName;
		this.officeClock = officeClock;
		this.timer = timer;
		this.functionLoop = functionLoop;
		this.officeManager = officeManager;
		this.functionMetaDatas = functionMetaDatas;
		this.functionLocator = functionLocator;
		this.processMetaData = processMetaData;
		this.processContextListeners = processContextListeners;
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
	public OfficeManager getOfficeManager() {
		return this.officeManager;
	}

	@Override
	public OfficeClock getOfficeClock() {
		return this.officeClock;
	}

	@Override
	public Timer getOfficeTimer() {
		return this.timer;
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
	public ManagedFunctionContainer createProcess(FlowMetaData flowMetaData, Object parameter, FlowCallback callback,
			ThreadState callbackThreadState) {
		return this.createProcess(flowMetaData, parameter, callback, callbackThreadState, null, null, -1);
	}

	@Override
	public ManagedFunctionContainer createProcess(FlowMetaData flowMetaData, Object parameter, FlowCallback callback,
			ThreadState callbackThreadState, ManagedObject inputManagedObject,
			ManagedObjectMetaData<?> inputManagedObjectMetaData, int processBoundIndexForInputManagedObject) {

		// Create the process profiler (if profiling)
		ProcessProfiler processProfiler = (this.profiler == null ? null
				: new ProcessProfilerImpl(this.profiler, System.nanoTime()));

		// Create the Process State (based on whether have managed object)
		ProcessState processState;
		if (inputManagedObject == null) {
			// Create Process without an Input Managed Object
			processState = new ProcessStateImpl(this.processMetaData, this.processContextListeners, this, callback,
					callbackThreadState, processProfiler);
		} else {
			// Create Process with the Input Managed Object
			processState = new ProcessStateImpl(this.processMetaData, this.processContextListeners, this, callback,
					callbackThreadState, processProfiler, inputManagedObject, inputManagedObjectMetaData,
					processBoundIndexForInputManagedObject);
		}

		// Create the Flow
		ThreadState threadState = processState.getMainThreadState();
		Flow flow = threadState.createFlow(null);

		// Obtain the function meta-data
		ManagedFunctionMetaData<?, ?> functionMetaData = flowMetaData.getInitialFunctionMetaData();

		// Create the initial function of the process
		ManagedFunctionContainer function = flow.createManagedFunction(parameter, functionMetaData, true, null);

		// Notify of created process context
		Object processIdentifier = processState.getProcessIdentifier();
		for (int i = 0; i < this.processContextListeners.length; i++) {
			this.processContextListeners[i].processCreated(processIdentifier);
		}

		// Return the function
		return function;
	}

}