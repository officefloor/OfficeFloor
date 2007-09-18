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
package net.officefloor.frame.impl;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.execute.WorkContext;

/**
 * Mock {@link net.officefloor.frame.api.execute.Task}.
 * 
 * @author Daniel
 */
public abstract class AbstractMockTask<P extends Object> implements Work,
		WorkFactory<Work>, Task<P, Work, Indexed, Indexed>,
		TaskFactory<P, Work, Indexed, Indexed> {

	/**
	 * {@link WorkContext}.
	 */
	private WorkContext workContext;

	/**
	 * {@link TaskContext}.
	 */
	private TaskContext<P, Work, Indexed, Indexed> taskContext;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkFactory#createWork()
	 */
	public final Work createWork() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Work#setWorkContext(net.officefloor.frame.api.execute.WorkContext)
	 */
	public final void setWorkContext(WorkContext context) throws Exception {
		this.workContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskFactory#createTask(W)
	 */
	public final Task<P, Work, Indexed, Indexed> createTask(Work work) {
		return (Task<P, Work, Indexed, Indexed>) work;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
	 */
	public final Object doTask(TaskContext<P, Work, Indexed, Indexed> context)
			throws Exception {

		// Specify the task context
		this.taskContext = context;

		// Do the task
		return this.doTask();
	}

	/**
	 * Obtains the {@link WorkContext}.
	 * 
	 * @return {@link WorkContext}.
	 */
	protected final WorkContext getWorkContext() {
		return this.workContext;
	}

	/**
	 * Obtains the {@link TaskContext}.
	 * 
	 * @return {@link TaskContext}.
	 */
	protected final TaskContext<P, Work, Indexed, Indexed> getTaskContext() {
		return this.taskContext;
	}

	/**
	 * Provides the functionality of the {@link #doTask(TaskContext)}.
	 * 
	 * @return Parameter.
	 * @throws Exception
	 *             If fails.
	 */
	protected abstract Object doTask() throws Exception;

}
