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
