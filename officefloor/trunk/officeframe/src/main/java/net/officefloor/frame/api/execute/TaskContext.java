/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.api.execute;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Context in which the {@link Task} is done.
 * 
 * @param P
 *            Type for the parameter to the {@link Task}.
 * @param W
 *            Specific {@link Work}.
 * @param M
 *            Type providing the keys to the {@link ManagedObject} instances.
 * @param F
 *            Type providing the keys to the {@link Flow} instances.
 * 
 * @author Daniel
 */
public interface TaskContext<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>> {

	/**
	 * <p>
	 * Obtains the parameter for the {@link Task}.
	 * <p>
	 * Parameter the {@link Task} was invoked with.
	 * 
	 * @return Parameter for the {@link Task}.
	 */
	P getParameter();

	/**
	 * Obtains the {@link Work} of the {@link Task} being executed.
	 * 
	 * @return {@link Work} of the {@link Task} being executed.
	 */
	W getWork();

	/**
	 * <p>
	 * Obtains the lock for the process containing the thread executing this
	 * {@link Task}.
	 * <p>
	 * This enables different threads of the process to co-ordinate.
	 * 
	 * @return Process level lock.
	 */
	Object getProcessLock();

	/**
	 * <p>
	 * Obtains the object of the specified
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @param key
	 *            Key identifying the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @return Object of the specified
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	Object getObject(M key);

	/**
	 * <p>
	 * Similar to {@link #getObject(M)} except that allows dynamically obtaining
	 * the objects of the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances.
	 * 
	 * @param managedObjectIndex
	 *            Index identifying the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @return Object of the specified
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	Object getObject(int managedObjectIndex);

	/**
	 * <p>
	 * Instigates a flow to be run.
	 * <p>
	 * The returned {@link FlowFuture} may not complete while the current
	 * {@link Task} is still executing. However it is available to register with
	 * the {@link Work} for later checking by other {@link Task} instances.
	 * 
	 * @param key
	 *            Key identifying the flow to instigate.
	 * @param parameter
	 *            Parameter that will be available from the
	 *            {@link TaskContext#getParameter()} of the first {@link Task}
	 *            of the flow to be run.
	 * @return {@link FlowFuture} to indicate when the instigated flow has
	 *         completed.
	 */
	FlowFuture doFlow(F key, Object parameter);

	/**
	 * <p>
	 * Similar to {@link #doFlow(F, Object)} except that allows dynamic
	 * instigation of flows.
	 * <p>
	 * In other words, an {@link Enum} is not required to define the possible
	 * flows available.
	 * 
	 * @param flowIndex
	 *            Index identifying the flow to instigate.
	 * @param parameter
	 *            Parameter that will be available from the
	 *            {@link TaskContext#getParameter()} of the first {@link Task}
	 *            of the flow to be run.
	 * @return {@link FlowFuture} to indicate when the instigated flow has
	 *         completed.
	 */
	FlowFuture doFlow(int flowIndex, Object parameter);

	/**
	 * <p>
	 * Stops this {@link Task} from proceding to the next {@link Task} in its
	 * {@link net.officefloor.frame.internal.structure.Flow} until the
	 * {@link net.officefloor.frame.internal.structure.Flow} of the input
	 * {@link FlowFuture} is complete.
	 * 
	 * @param flowFuture
	 *            {@link FlowFuture} of the {@link Task} that must complete.
	 */
	void join(FlowFuture flowFuture);

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
