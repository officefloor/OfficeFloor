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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.spi.team.TaskContainer;

/**
 * State of a thread within a Process.
 * 
 * @author Daniel
 */
public interface ThreadState extends FlowFuture {

	/**
	 * Obtains the lock for the thread of this {@link ThreadState}.
	 * 
	 * @return Lock for the thread of this {@link ThreadState}.
	 */
	Object getThreadLock();

	/**
	 * Returning a {@link Throwable} indicates that the thread has failed.
	 * 
	 * @return {@link Throwable} indicating the thread has failed or
	 *         <code>null</code> indicating thread still going fine.
	 */
	Throwable getFailure();

	/**
	 * Sets the {@link Throwable} cause to indicate that the thread has failed.
	 * 
	 * @param cause
	 *            Cause of the thread's failure.
	 */
	void setFailure(Throwable cause);

	/**
	 * Creates a {@link Flow} bound to this {@link ThreadState}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} for the new {@link Flow}.
	 * @return New {@link Flow}.
	 */
	Flow createFlow(FlowMetaData<?> flowMetaData);

	/**
	 * Obtains the {@link LinkedList} of
	 * {@link net.officefloor.frame.api.execute.Work} bound to this
	 * {@link ThreadState}.
	 * 
	 * @return {@link LinkedList} of
	 *         {@link net.officefloor.frame.api.execute.Work} bound to this
	 *         {@link ThreadState}.
	 */
	LinkedList<ThreadWorkLink<?>> getWorkList();

	/**
	 * Obtains the {@link ProcessState} of the process containing this
	 * {@link ThreadState}.
	 * 
	 * @return {@link ProcessState} of the process containing this
	 *         {@link ThreadState}.
	 */
	ProcessState getProcessState();

	/**
	 * Flags that escalation is about to happen on this {@link ThreadState}.
	 * 
	 * @param task
	 *            {@link TaskContainer} causing the escalation.
	 * @param flow
	 *            {@link Flow} that the {@link TaskContainer} is bound.
	 */
	void escalationStart(TaskContainer task, Flow flow);

	/**
	 * Flags that escalation has complete on this {@link ThreadState}.
	 * 
	 * @param task
	 *            {@link TaskContainer} causing the escalation.
	 * @param flow
	 *            {@link Flow} that the {@link TaskContainer} is bound.
	 */
	void escalationComplete(TaskContainer task, Flow flow);

}
