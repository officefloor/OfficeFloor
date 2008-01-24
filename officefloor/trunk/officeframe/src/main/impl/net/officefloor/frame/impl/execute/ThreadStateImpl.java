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

import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.AssetReport;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskNode;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadWorkLink;
import net.officefloor.frame.spi.team.TaskContainer;

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.structure.ThreadState}.
 * 
 * @author Daniel
 */
public class ThreadStateImpl implements ThreadState, Asset {

	/**
	 * {@link LinkedList} of {@link Flow} instances for this {@link ThreadState}.
	 */
	protected final LinkedList<Flow> flows = new AbstractLinkedList<Flow>() {
		public void lastLinkedListEntryRemoved() {

			// Do nothing if reseting the state
			if (ThreadStateImpl.this.isResetingState) {
				return;
			}

			// Complete the thread
			ThreadStateImpl.this.completeThread();
		}
	};

	/**
	 * {@link LinkedList} of {@link ThreadWorkLink} instances for this
	 * {@link ThreadState}.
	 */
	protected final LinkedList<ThreadWorkLink<?>> works = new AbstractLinkedList<ThreadWorkLink<?>>() {
		public void lastLinkedListEntryRemoved() {
			// No action required on last work removed
		}
	};

	/**
	 * {@link ProcessState} for this {@link ThreadState}.
	 */
	protected final ProcessState processState;

	/**
	 * {@link AssetMonitor} for monitoring this {@link ThreadState}.
	 */
	protected final AssetMonitor threadMonitor;

	/**
	 * Flag indicating reseting the state.
	 */
	private boolean isResetingState = false;

	/**
	 * Failure of the {@link ThreadState}.
	 */
	protected Throwable failure = null;

	/**
	 * <p>
	 * Completion flag indicating when this
	 * {@link net.officefloor.frame.api.execute.FlowFuture} is complete.
	 * <p>
	 * <code>volatile</code> to enable inter-thread visibility.
	 */
	protected volatile boolean isFlowComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param processState
	 *            {@link ProcessState} for this {@link ThreadState}.
	 * @param flowMetaData
	 *            {@link FlowMetaData} for this {@link ThreadState}.
	 */
	public ThreadStateImpl(ProcessState processState,
			FlowMetaData<?> flowMetaData) {
		this.processState = processState;

		// Create the thread monitor (if required)
		AssetManager flowManager = flowMetaData.getFlowManager();
		if (flowManager == null) {
			this.threadMonitor = null;
		} else {
			this.threadMonitor = flowManager.createAssetMonitor(this, this
					.getThreadLock());
		}
	}

	/**
	 * Completes this {@link ThreadState}.
	 */
	protected void completeThread() {
		// Flow (thread) complete
		isFlowComplete = true;

		// Wake up all tasks waiting on this thread
		if (threadMonitor != null) {
			threadMonitor.notifyTasks();
		}

		// Thread complete
		processState.threadComplete(null);
	}

	/*
	 * ====================================================================
	 * ThreadState
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadState#getThreadLock()
	 */
	public Object getThreadLock() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadState#getFailure()
	 */
	public Throwable getFailure() {
		return this.failure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadState#setFailure(java.lang.Throwable)
	 */
	public void setFailure(Throwable cause) {
		this.failure = cause;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadState#createFlow()
	 */
	public Flow createFlow(FlowMetaData<?> flowMetaData) {
		// Create the flow
		Flow flow = new FlowImpl(this, this.flows);

		// Add the active flows
		this.flows.addLinkedListEntry(flow);

		// Return the flow
		return flow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadState#getWorkList()
	 */
	public LinkedList<ThreadWorkLink<?>> getWorkList() {
		return this.works;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadState#getProcessState()
	 */
	public ProcessState getProcessState() {
		return this.processState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadState#escalationStart(net.officefloor.frame.internal.structure.TaskNode,
	 *      boolean)
	 */
	@Override
	public void escalationStart(TaskNode currentTaskNode,
			boolean isResetThreadState) {
		// Determine if reset thread state
		if (isResetThreadState) {
			try {
				this.isResetingState = true;
				currentTaskNode.clearNodes();
			} finally {
				this.isResetingState = false;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ThreadState#escalationComplete(net.officefloor.frame.internal.structure.TaskNode)
	 */
	@Override
	public void escalationComplete(TaskNode currentTaskNode) {

		// Determine if thread is complete
		if ((currentTaskNode.getNextNode() != null)
				|| (currentTaskNode.getParallelNode() != null)
				|| (currentTaskNode.getParallelNode() != null)) {
			// Thread still active, therefore do nothing
			return;
		}

		// Thread finished, therefore complete
		this.completeThread();
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

		// Determine if the same thread
		if (this == taskContainer.getThreadState()) {
			// Do not wait on this thread
			return false;
		}

		// Return whether task is waiting on this thread.
		// Note: thread may already be complete.
		return this.threadMonitor.wait(taskContainer);
	}

	/*
	 * ====================================================================
	 * Asset
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Asset#reportOnAsset(net.officefloor.frame.internal.structure.AssetReport)
	 */
	public void reportOnAsset(AssetReport report) {
		// TODO Auto-generated method stub

	}

}
