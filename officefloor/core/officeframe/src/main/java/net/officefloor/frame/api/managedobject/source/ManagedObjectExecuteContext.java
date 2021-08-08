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

package net.officefloor.frame.api.managedobject.source;

import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Context that the {@link ManagedObject} is to execute within.
 * <p>
 * In invoking processes the following should be taken into account:
 * <ol>
 * <li>The {@link Flow} (process) will be instigated in a new
 * {@link ProcessState} which for example will cause new {@link ManagedObject}
 * dependencies to be instantiated.</li>
 * <li>The {@link ManagedObject} passed to the invocation will go through a full
 * life-cycle so be careful passing in an existing initialised
 * {@link ManagedObject}. For example the {@link AsynchronousContext} instance
 * will be overwritten which will likely cause live-lock as the
 * {@link AsynchronousContext#complete(net.officefloor.frame.api.managedobject.AsynchronousOperation)}
 * will notify on the wrong {@link ManagedObjectContainer}.</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecuteContext<F extends Enum<F>> {

	/**
	 * Obtains the {@link Logger}.
	 * 
	 * @return {@link Logger}.
	 */
	Logger getLogger();

	/**
	 * Obtains an {@link ExecutionStrategy}.
	 * 
	 * @param executionStrategyIndex Index of the {@link ExecutionStrategy}.
	 * @return {@link ThreadFactory} instances for the {@link ExecutionStrategy}.
	 */
	ThreadFactory[] getExecutionStrategy(int executionStrategyIndex);

	/**
	 * Invokes a start up {@link Flow}.
	 * 
	 * @param key           Key identifying the {@link Flow} to instigate.
	 * @param parameter     Parameter to first {@link ManagedFunction} of the
	 *                      {@link Flow}.
	 * @param managedObject {@link ManagedObject} for the {@link ProcessState} of
	 *                      the {@link Flow}.
	 * @param callback      {@link FlowCallback} on completion of the {@link Flow}.
	 * @return {@link ManagedObjectStartupProcess}.
	 * @throws IllegalArgumentException If
	 *                                  <ul>
	 *                                  <li>unknown {@link Flow} key</li>
	 *                                  <li>parameter is incorrect type</li>
	 *                                  <li>no {@link ManagedObject} is
	 *                                  supplied</li>
	 *                                  </ul>
	 */
	ManagedObjectStartupProcess invokeStartupProcess(F key, Object parameter, ManagedObject managedObject,
			FlowCallback callback) throws IllegalArgumentException;

	/**
	 * Invokes a start up {@link Flow}.
	 * 
	 * @param flowIndex     Index identifying the {@link Flow} to instigate.
	 * @param parameter     Parameter to first {@link ManagedFunction} of the
	 *                      {@link Flow}.
	 * @param managedObject {@link ManagedObject} for the {@link ProcessState} of
	 *                      the {@link Flow}.
	 * @param callback      {@link FlowCallback} on completion of the {@link Flow}.
	 * @return {@link ManagedObjectStartupProcess}.
	 * @throws IllegalArgumentException If
	 *                                  <ul>
	 *                                  <li>unknown {@link Flow} key</li>
	 *                                  <li>parameter is incorrect type</li>
	 *                                  <li>no {@link ManagedObject} is
	 *                                  supplied</li>
	 *                                  </ul>
	 */
	ManagedObjectStartupProcess invokeStartupProcess(int flowIndex, Object parameter, ManagedObject managedObject,
			FlowCallback callback) throws IllegalArgumentException;

	/**
	 * Adds a {@link ManagedObjectService}.
	 * 
	 * @param service {@link ManagedObjectService}.
	 */
	void addService(ManagedObjectService<F> service);

}
