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

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TaskNode;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadWorkLink;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.team.TaskContainer;

/**
 * Implementation of the {@link net.officefloor.frame.internal.structure.Flow}.
 * 
 * @author Daniel
 */
public class FlowImpl extends AbstractLinkedListEntry<Flow> implements Flow {

	/**
	 * {@link ThreadState} that this {@link Flow} is bound.
	 */
	protected final ThreadState threadState;

	/**
	 * Count of active {@link TaskContainer} instances.
	 */
	protected int activeTaskCount = 0;

	/**
	 * Completion flag indicating if this {@link Flow} is complete.
	 */
	protected volatile boolean isFlowComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param threadState
	 *            {@link ThreadState} for this {@link Flow}.
	 * @param flows
	 *            {@link LinkedList} of {@link Flow} instances for the
	 *            {@link ThreadState}.
	 */
	public FlowImpl(ThreadState threadState, LinkedList<Flow> flows) {
		super(flows);
		this.threadState = threadState;
	}

	/*
	 * ========================================================================
	 * Flow
	 * ========================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Flow#createTaskContainer(net.officefloor.frame.internal.structure.TaskMetaData,
	 *      net.officefloor.frame.internal.structure.TaskNode, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public TaskContainer createTaskContainer(TaskMetaData<?, ?, ?, ?> taskMetaData,
			TaskNode parallelNodeOwner, Object parameter,
			ThreadWorkLink<?> currentWorkLink) {

		// Obtain the work meta-data
		WorkMetaData workMetaData = taskMetaData.getWorkMetaData();

		// Obtain the work link
		ThreadWorkLink newWorkLink;
		if (currentWorkLink.getWorkContainer().getWorkId() == workMetaData
				.getWorkId()) {
			// Same work, thus re-use work
			newWorkLink = currentWorkLink
					.createThreadWorkLink(this.threadState);
		} else {
			// Source work (as different work)
			// TODO: consider bounding work to Process
			WorkContainer newWorkContainer = new WorkContainerImpl(workMetaData
					.getWorkFactory().createWork(), workMetaData,
					this.threadState.getProcessState());

			// Create thread work link for the new work
			newWorkLink = new ThreadWorkLinkImpl(this.threadState,
					newWorkContainer);
		}

		// Create the TaskContainer
		TaskContainer taskContainer = new TaskContainerImpl(this.threadState,
				this, newWorkLink, taskMetaData, parameter, parallelNodeOwner);

		// Register the task with the work
		newWorkLink.registerTask(taskContainer);

		// Increment the number of active task containers
		this.activeTaskCount++;

		// Return the TaskContainer
		return taskContainer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Flow#taskContainerComplete(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public void taskContainerComplete(TaskContainer taskContainer) {
		// Task container now inactive
		this.activeTaskCount--;

		// Determine if flow is complete
		if (this.activeTaskCount == 0) {
			// Flow complete
			this.isFlowComplete = true;
			this.removeFromLinkedList();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Flow#getThreadState()
	 */
	public ThreadState getThreadState() {
		return this.threadState;
	}

	/*
	 * ====================================================================
	 * FlowFuture
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.FlowFuture#isComplete()
	 */
	public boolean isComplete() {
		return this.isFlowComplete;
	}

	/*
	 * ========================================================================
	 * FlowAsset
	 * ========================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.FlowAsset#waitOnFlow(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public boolean waitOnFlow(TaskContainer taskContainer) {
		// Delegate to the thread
		return this.threadState.waitOnFlow(taskContainer);
	}

}
