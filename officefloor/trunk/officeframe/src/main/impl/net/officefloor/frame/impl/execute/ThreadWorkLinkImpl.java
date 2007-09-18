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
package net.officefloor.frame.impl.execute;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadWorkLink;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.team.TaskContainer;

/**
 * Links a {@link net.officefloor.frame.internal.structure.ThreadState} to a
 * {@link net.officefloor.frame.internal.structure.WorkContainer}.
 * 
 * @author Daniel
 */
public class ThreadWorkLinkImpl<W extends Work> extends
		AbstractLinkedListEntry<ThreadWorkLink<?>> implements ThreadWorkLink<W> {

	/**
	 * {@link ThreadState} of the link.
	 */
	protected final ThreadState threadState;

	/**
	 * {@link WorkContainer} of the link.
	 */
	protected final WorkContainer<W> workContainer;

	/**
	 * Count of active {@link TaskContainer} instances using the underlying
	 * {@link WorkContainer}.
	 */
	protected int activeTaskCount = 0;

	/**
	 * Initiate.
	 * 
	 * @param threadState
	 *            {@link ThreadState} to link.
	 * @param workContainer
	 *            {@link WorkContainer} to link.
	 */
	public ThreadWorkLinkImpl(ThreadState threadState,
			WorkContainer<W> workContainer) {
		super(threadState.getWorkList());
		this.threadState = threadState;
		this.workContainer = workContainer;

		// Register this with the WorkContainer
		this.workContainer.registerThread(this.threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadWorkLink#getWorkContainer()
	 */
	public WorkContainer<W> getWorkContainer() {
		return this.workContainer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadWorkLink#getThreadState()
	 */
	public ThreadState getThreadState() {
		return this.threadState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadWorkLink#createThreadWorkLink(net.officefloor.frame.internal.structure.ThreadState)
	 */
	public ThreadWorkLink<W> createThreadWorkLink(ThreadState targetThread) {

		// Determine if the same thread
		if (this.threadState == targetThread) {
			// Same thread, therefore re-use link
			return this;
		} else {
			// New thread therefore create link for new thread
			return new ThreadWorkLinkImpl<W>(targetThread, this.workContainer);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadWorkLink#registerTask(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public void registerTask(TaskContainer task) {
		// Another active task
		this.activeTaskCount++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadWorkLink#unregisterTask(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public void unregisterTask(TaskContainer task) {
		// Task now inactive
		this.activeTaskCount--;

		// Determine if work still required by thread
		if (this.activeTaskCount == 0) {
			this.workContainer.unregisterThread(this.threadState);
		}
	}

}
