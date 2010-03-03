/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.api.execute;

import net.officefloor.frame.api.escalate.FlowJoinTimedOutEscalation;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Context in which the {@link Task} is done.
 * 
 * @param W
 *            Specific {@link Work}.
 * @param D
 *            Type providing the keys for the dependencies. Dependencies may
 *            either be:
 *            <ol>
 *            <li>{@link Object} of a {@link ManagedObject}</li>
 *            <li>Parameter for the {@link Task}</li>
 *            </ol>
 * @param F
 *            Type providing the keys to the possible {@link Flow} instances
 *            instigated by this {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskContext<W extends Work, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Obtains the {@link Work} of the {@link Task}.
	 * 
	 * @return {@link Work} of the {@link Task}.
	 */
	W getWork();

	/**
	 * <p>
	 * Obtains the lock for the {@link ProcessState} containing the
	 * {@link ThreadState} executing the {@link Task}.
	 * <p>
	 * This enables different {@link ThreadState} instances of a
	 * {@link ProcessState} to have critical sections.
	 * 
	 * @return {@link ProcessState} lock.
	 */
	Object getProcessLock();

	/**
	 * Obtains the dependency object.
	 * 
	 * @param key
	 *            Key identifying the dependency.
	 * @return Dependency object.
	 */
	Object getObject(D key);

	/**
	 * <p>
	 * Similar to {@link #getObject(D)} except allows dynamically obtaining the
	 * dependencies.
	 * <p>
	 * In other words, an {@link Enum} is not required to define the possible
	 * dependencies available.
	 * 
	 * @param dependencyIndex
	 *            Index identifying the dependency.
	 * @return Dependency object.
	 */
	Object getObject(int dependencyIndex);

	/**
	 * <p>
	 * Instigates a {@link Flow} to be run.
	 * <p>
	 * The returned {@link FlowFuture} may not complete while the current
	 * {@link Task} is executing. However it is available to register for later
	 * checking by other {@link Task} instances.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter for the first {@link Task} of the {@link Flow}
	 *            instigated.
	 * @return {@link FlowFuture} to indicate when the instigated {@link Flow}
	 *         has completed.
	 */
	FlowFuture doFlow(F key, Object parameter);

	/**
	 * <p>
	 * Similar to {@link #doFlow(F, Object)} except that allows dynamic
	 * instigation of flows.
	 * <p>
	 * In other words, an {@link Enum} is not required to define the possible
	 * {@link Flow} instances available.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter that will be available for the first {@link Task} of
	 *            the {@link Flow} to be run.
	 * @return {@link FlowFuture} to indicate when the instigated {@link Flow}
	 *         has completed.
	 */
	FlowFuture doFlow(int flowIndex, Object parameter);

	/**
	 * <p>
	 * Stops this {@link Task} from proceeding to the next {@link Task} in its
	 * {@link Flow} until the {@link Flow} of the input {@link FlowFuture} is
	 * complete.
	 * <p>
	 * Should the same {@link Flow} be joined on twice, only the first join is
	 * registered - second join of same {@link Flow} will be ignored.
	 * 
	 * @param flowFuture
	 *            {@link FlowFuture} of the {@link Flow} that must complete.
	 * @param timeout
	 *            The maximum time to wait in milliseconds for the {@link Flow}
	 *            to complete. Should the {@link Flow} not complete in this
	 *            timeout, a {@link FlowJoinTimedOutEscalation} is escalated
	 *            from this {@link Task}.
	 * @param token
	 *            Should a {@link FlowJoinTimedOutEscalation} be escalated the
	 *            token will be added to the escalated
	 *            {@link FlowJoinTimedOutEscalation} to aid in identifying which
	 *            {@link Flow} join timed out. May be <code>null</code>.
	 * @throws IllegalStateException
	 *             If a {@link ProcessState} or unknown {@link FlowFuture}. Only
	 *             {@link FlowFuture} instances returned from the
	 *             <code>doFlow</code> methods are valid input.
	 */
	void join(FlowFuture flowFuture, long timeout, Object token)
			throws IllegalArgumentException;

	/**
	 * <p>
	 * Indicates whether the {@link Task} has completed. Setting this to
	 * <code>false</code> has the {@link Task#doTask(TaskContext)} method
	 * invoked again after the {@link Task} has returned control.
	 * <p>
	 * By default on invoking {@link Task#doTask(TaskContext)} this is set to
	 * <code>true</code>. Therefore only use if the {@link Task} has not yet
	 * complete.
	 * 
	 * @param isComplete
	 *            <code>false</code> will have the
	 *            {@link Task#doTask(TaskContext)} invoked again.
	 */
	void setComplete(boolean isComplete);

}