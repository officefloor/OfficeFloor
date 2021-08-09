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

package net.officefloor.frame.internal.structure;

import java.beans.Statement;
import java.util.concurrent.Executor;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.manage.StateManager;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Meta-data for the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeMetaData {

	/**
	 * Obtains the name of the {@link Office}.
	 * 
	 * @return Name of the {@link Office}.
	 */
	String getOfficeName();

	/**
	 * Sets up the default {@link OfficeManager}.
	 * 
	 * @return Default {@link OfficeManager}.
	 */
	OfficeManager setupDefaultOfficeManager();

	/**
	 * Creates a {@link ProcessIdentifier} for a new {@link ProcessState}.
	 * 
	 * @param processState New {@link ProcessState}.
	 * @return New {@link ProcessIdentifier}.
	 */
	ProcessIdentifier createProcessIdentifier(ProcessState processState);

	/**
	 * Obtains the {@link OfficeManager} for the {@link ProcessState}.
	 * 
	 * @param processIdentifier {@link ProcessIdentifier} of the
	 *                          {@link ProcessState}.
	 * @return {@link OfficeManager} of the {@link Office}.
	 */
	OfficeManager getOfficeManager(ProcessIdentifier processIdentifier);

	/**
	 * Obtains the {@link Executor} for the {@link ProcessState}.
	 * 
	 * @param processIdentifier {@link ProcessIdentifier} of the
	 *                          {@link ProcessState}.
	 * @return {@link Executor} for the {@link ProcessState}.
	 */
	Executor getExecutor(ProcessIdentifier processIdentifier);

	/**
	 * Obtains the {@link MonitorClock} for the {@link Office}.
	 * 
	 * @return {@link MonitorClock} for the {@link Office}.
	 */
	MonitorClock getMonitorClock();

	/**
	 * Obtains the {@link ProcessMetaData} for processes within this {@link Office}.
	 * 
	 * @return {@link ProcessMetaData} for processes within this {@link Office}.
	 */
	ProcessMetaData getProcessMetaData();

	/**
	 * Obtains the {@link FunctionLoop} for the {@link Office}.
	 * 
	 * @return {@link FunctionLoop} for the {@link Office}.
	 */
	FunctionLoop getFunctionLoop();

	/**
	 * Obtains the {@link ManagedFunctionMetaData} of the {@link ManagedFunction}
	 * that may be done within this {@link Office}.
	 * 
	 * @return {@link ManagedFunctionMetaData} instances of this {@link Office}.
	 */
	ManagedFunctionMetaData<?, ?>[] getManagedFunctionMetaData();

	/**
	 * Obtains the {@link ManagedFunctionLocator}.
	 * 
	 * @return {@link ManagedFunctionLocator}.
	 */
	ManagedFunctionLocator getManagedFunctionLocator();

	/**
	 * Obtains the {@link OfficeStartupFunction} instances for this {@link Office}.
	 * 
	 * @return {@link OfficeStartupFunction} instances for this {@link Office}.
	 */
	OfficeStartupFunction[] getStartupFunctions();

	/**
	 * Obtains the {@link Executive} for this {@link Office}.
	 * 
	 * @return {@link Executive} for this {@link Office}.
	 */
	Executive getExecutive();

	/**
	 * Obtains the {@link ManagedExecutionFactory} for this {@link Office}.
	 * 
	 * @return {@link ManagedExecutionFactory} for this {@link Office}.
	 */
	ManagedExecutionFactory getManagedExecutionFactory();

	/**
	 * Creates a new {@link ProcessState}.
	 * 
	 * @param flowMetaData        {@link FlowMetaData} of the starting
	 *                            {@link FunctionState} for the
	 *                            {@link ProcessState}.
	 * @param parameter           Parameter to the starting {@link FunctionState}.
	 * @param callback            Optional {@link FlowCallback} of the
	 *                            {@link ProcessState}. May be <code>null</code>.
	 * @param callbackThreadState Optional {@link ThreadState} for the
	 *                            {@link FlowCallback}. May be <code>null</code>.
	 * @return {@link FunctionState} to start processing the {@link ProcessState}.
	 */
	FunctionState createProcess(FlowMetaData flowMetaData, Object parameter, FlowCallback callback,
			ThreadState callbackThreadState);

	/**
	 * Invokes a {@link ProcessState}.
	 * 
	 * @param flowMetaData                           {@link FlowMetaData} of the
	 *                                               starting {@link FunctionState}
	 *                                               for the {@link ProcessState}.
	 * @param parameter                              Parameter to the starting
	 *                                               {@link FunctionState}.
	 * @param delay                                  Millisecond delay in invoking
	 *                                               the {@link ProcessState}. 0 (or
	 *                                               negative value) will invoke
	 *                                               immediately on the current
	 *                                               {@link Thread}.
	 * @param callback                               Optional {@link FlowCallback}
	 *                                               of the {@link ProcessState}.
	 *                                               May be <code>null</code>.
	 * @param callbackThreadState                    Optional {@link ThreadState}
	 *                                               for the {@link FlowCallback}.
	 *                                               May be <code>null</code>.
	 * @param inputManagedObject                     {@link ManagedObject} that
	 *                                               possibly invoked the new
	 *                                               {@link ProcessState}. This may
	 *                                               be <code>null</code> and if so
	 *                                               the remaining parameters will
	 *                                               be ignored.
	 * @param inputManagedObjectMetaData             {@link ManagedObjectMetaData}
	 *                                               for the {@link ManagedObject}
	 *                                               that invoked the new
	 *                                               {@link ProcessState}. Should
	 *                                               the {@link ManagedObject} be
	 *                                               provided this must then also be
	 *                                               provided.
	 * @param processBoundIndexForInputManagedObject Index of the
	 *                                               {@link ManagedObject} within
	 *                                               the {@link ProcessState}.
	 *                                               Ignored if
	 *                                               {@link ManagedObject} passed in
	 *                                               is <code>null</code>.
	 * @return {@link ProcessManager} for the invoked {@link ProcessState}.
	 * @throws InvalidParameterTypeException Should the type of parameter be invalid
	 *                                       for the initial
	 *                                       {@link ManagedFunction}.
	 */
	ProcessManager invokeProcess(FlowMetaData flowMetaData, Object parameter, long delay, FlowCallback callback,
			ThreadState callbackThreadState, ManagedObject inputManagedObject,
			ManagedObjectMetaData<?> inputManagedObjectMetaData, int processBoundIndexForInputManagedObject)
			throws InvalidParameterTypeException;

	/**
	 * Creates a {@link StateManager}.
	 * 
	 * @return {@link Statement}.
	 */
	StateManager createStateManager();

}
