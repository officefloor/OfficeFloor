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
package net.officefloor.frame.spi.team;

import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Container that manages the {@link net.officefloor.frame.api.execute.Task}.
 * 
 * @author Daniel
 */
public interface TaskContainer {

	/**
	 * Activates this {@link TaskContainer}.
	 */
	void activateTask();

	/**
	 * <p>
	 * Executes the {@link net.officefloor.frame.api.execute.Task} with the
	 * {@link TaskContainer}.
	 * <p>
	 * The return indicates if the
	 * {@link net.officefloor.frame.api.execute.Task} within the
	 * {@link TaskContainer} has been completed and may be released. Returning
	 * <code>false</code> indicates this method must be executed again (and
	 * possibly again and again) until it returns <code>true</code>.
	 * 
	 * @param executionContext
	 *            Context for execution.
	 * @return <code>true</code> if the
	 *         {@link net.officefloor.frame.api.execute.Task} has completed.
	 */
	boolean doTask(ExecutionContext executionContext);

	/**
	 * Obtains the {@link ThreadState} that this {@link TaskContainer} is bound.
	 * 
	 * @return {@link ThreadState} that this {@link TaskContainer} is bound.
	 */
	ThreadState getThreadState();

	/**
	 * <p>
	 * Specifies the next {@link TaskContainer}. This provides ability to
	 * create a linked list of {@link TaskContainer} instances.
	 * <p>
	 * Note there is no thread-safety guaranteed on this method.
	 * 
	 * @param task
	 *            {@link TaskContainer} that is next in the list to this
	 *            {@link TaskContainer}.
	 * @see #getNextTask()
	 */
	void setNextTask(TaskContainer task);

	/**
	 * <p>
	 * Obtains the next {@link TaskContainer}. This provides ability to create
	 * a linked list of {@link TaskContainer} instances.
	 * <p>
	 * Note there is no thread-safety guaranteed on this method.
	 * 
	 * @return Next {@link TaskContainer} after this in the list.
	 * @see #setNextTask(TaskContainer)
	 */
	TaskContainer getNextTask();

}
