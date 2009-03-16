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
package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.impl.execute.administrator.AdministratorContainerImpl;
import net.officefloor.frame.impl.execute.flow.FlowImpl;
import net.officefloor.frame.impl.execute.linkedlist.AbstractLinkedList;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.AssetReport;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link ThreadState}.
 * 
 * @author Daniel
 */
public class ThreadStateImpl implements ThreadState, Asset {

	/**
	 * {@link LinkedList} of the {@link Flow} instances for this
	 * {@link ThreadState}.
	 */
	protected final LinkedList<Flow, JobActivateSet> flows = new AbstractLinkedList<Flow, JobActivateSet>() {
		@Override
		public void lastLinkedListEntryRemoved(JobActivateSet notifySet) {

			// Do nothing if searching for escalation
			if (ThreadStateImpl.this.isEscalating) {
				return;
			}

			// Complete the thread as not escalating and no more flows
			ThreadStateImpl.this.completeThread(notifySet);
		}
	};

	/**
	 * {@link ThreadMetaData} for this {@link ThreadState}.
	 */
	private final ThreadMetaData threadMetaData;

	/**
	 * {@link ManagedObjectContainer} instances for this {@link ThreadState}.
	 */
	private final ManagedObjectContainer[] managedObjectContainers;

	/**
	 * {@link AdministratorContainer} instances for this {@link ThreadState}.
	 */
	private final AdministratorContainer<?, ?>[] administratorContainers;

	/**
	 * {@link ProcessState} for this {@link ThreadState}.
	 */
	private final ProcessState processState;

	/**
	 * {@link AssetMonitor} for monitoring this {@link ThreadState}.
	 */
	private final AssetMonitor threadMonitor;

	/**
	 * Flag indicating that looking for {@link Escalation}.
	 */
	private boolean isEscalating = false;

	/**
	 * Failure of the {@link ThreadState}.
	 */
	private Throwable failure = null;

	/**
	 * {@link EscalationLevel} for this {@link ThreadState}.
	 */
	private EscalationLevel escalationLevel = EscalationLevel.FLOW;

	/**
	 * <p>
	 * Completion flag indicating when this {@link FlowFuture} is complete.
	 * <p>
	 * <code>volatile</code> to enable inter-thread visibility.
	 */
	private volatile boolean isFlowComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param threadMetaData
	 *            {@link ThreadMetaData} for this {@link ThreadState}.
	 * @param processState
	 *            {@link ProcessState} for this {@link ThreadState}.
	 * @param flowMetaData
	 *            {@link FlowMetaData} for this {@link ThreadState}.
	 */
	@SuppressWarnings("unchecked")
	public ThreadStateImpl(ThreadMetaData threadMetaData,
			ProcessState processState, FlowMetaData<?> flowMetaData) {
		this.threadMetaData = threadMetaData;
		this.processState = processState;

		// Create the managed object containers
		ManagedObjectMetaData<?>[] moMetaData = this.threadMetaData
				.getManagedObjectMetaData();
		this.managedObjectContainers = new ManagedObjectContainer[moMetaData.length];
		for (int i = 0; i < this.managedObjectContainers.length; i++) {
			this.managedObjectContainers[i] = new ManagedObjectContainerImpl(
					moMetaData[i], this.processState);
		}

		// Create the administrator containers
		AdministratorMetaData<?, ?>[] adminMetaData = this.threadMetaData
				.getAdministratorMetaData();
		this.administratorContainers = new AdministratorContainer[adminMetaData.length];
		for (int i = 0; i < this.administratorContainers.length; i++) {
			this.administratorContainers[i] = new AdministratorContainerImpl(
					adminMetaData[i]);
		}

		// Create the thread monitor (if required)
		AssetManager flowManager = flowMetaData.getFlowManager();
		if (flowManager == null) {
			this.threadMonitor = null;
		} else {
			this.threadMonitor = flowManager.createAssetMonitor(this);
		}
	}

	/**
	 * Completes this {@link ThreadState}.
	 */
	protected void completeThread(JobActivateSet notifySet) {
		// Flow (thread) complete
		this.isFlowComplete = true;

		// Wake up all tasks waiting on this thread permanently
		if (this.threadMonitor != null) {
			this.threadMonitor.notifyPermanently(notifySet);
		}

		// Thread complete
		this.processState.threadComplete(this);
	}

	/*
	 * ===================== ThreadState ==================================
	 */

	@Override
	public Object getThreadLock() {
		return this;
	}

	@Override
	public ThreadMetaData getThreadMetaData() {
		return this.threadMetaData;
	}

	@Override
	public Throwable getFailure() {
		return this.failure;
	}

	@Override
	public void setFailure(Throwable cause) {
		this.failure = cause;
	}

	@Override
	public Flow createFlow(FlowMetaData<?> flowMetaData) {
		// Create the flow
		Flow flow = new FlowImpl(this, this.flows);

		// Add the active flows
		this.flows.addLinkedListEntry(flow);

		// Return the flow
		return flow;
	}

	@Override
	public ProcessState getProcessState() {
		return this.processState;
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		return this.managedObjectContainers[index];
	}

	@Override
	public AdministratorContainer<?, ?> getAdministratorContainer(int index) {
		return this.administratorContainers[index];
	}

	@Override
	public void escalationStart(JobNode currentTaskNode,
			JobActivateSet notifySet) {
		this.isEscalating = true;
	}

	@Override
	public void escalationComplete(JobNode currentTaskNode,
			JobActivateSet notifySet) {
		this.isEscalating = false;
	}

	@Override
	public EscalationLevel getEscalationLevel() {
		return this.escalationLevel;
	}

	@Override
	public void setEscalationLevel(EscalationLevel escalationLevel) {
		this.escalationLevel = escalationLevel;
	}

	/*
	 * ====================== FlowFuture ==================================
	 */

	@Override
	public boolean isComplete() {
		return this.isFlowComplete;
	}

	/*
	 * ======================= FlowAsset ======================================
	 */

	@Override
	public boolean waitOnFlow(JobNode jobNode, JobActivateSet notifySet) {

		// Determine if the same thread
		if (this == jobNode.getFlow().getThreadState()) {
			// Do not wait on this thread
			return false;
		}

		// Return whether task is waiting on this thread.
		// Note: thread may already be complete.
		return this.threadMonitor.wait(jobNode, notifySet);
	}

	/*
	 * =================== Asset ==========================================
	 */

	@Override
	public Object getAssetLock() {
		return this.getThreadLock();
	}

	@Override
	public void reportOnAsset(AssetReport report) {
		// TODO implement asset reporting on a ThreadState
	}

}