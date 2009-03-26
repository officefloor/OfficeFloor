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
import net.officefloor.frame.impl.execute.linkedlist.AbstractLinkedListEntry;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
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
public class ThreadStateImpl extends
		AbstractLinkedListEntry<ThreadState, JobNodeActivateSet> implements
		ThreadState, Asset {

	/**
	 * Active {@link Flow} instances for this {@link ThreadState}.
	 */
	protected final LinkedList<Flow, JobNodeActivateSet> activeFlows = new AbstractLinkedList<Flow, JobNodeActivateSet>() {
		@Override
		public void lastLinkedListEntryRemoved(JobNodeActivateSet notifySet) {

			// Do nothing if searching for escalation
			if (ThreadStateImpl.this.isEscalating) {
				return;
			}

			// Flow (thread) complete
			ThreadStateImpl.this.isFlowComplete = true;

			// Activate all jobs waiting on this thread permanently
			ThreadStateImpl.this.threadMonitor
					.activateJobNodes(notifySet, true);

			// Thread complete
			ThreadStateImpl.this.processState.threadComplete(
					ThreadStateImpl.this, notifySet);
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
	 * @param processThreads
	 *            {@link LinkedList} of the {@link ThreadState} instances for
	 *            the {@link ProcessState} containing this {@link ThreadState}.
	 * @param processState
	 *            {@link ProcessState} for this {@link ThreadState}.
	 * @param flowMetaData
	 *            {@link FlowMetaData} for this {@link ThreadState}.
	 */
	@SuppressWarnings("unchecked")
	public ThreadStateImpl(ThreadMetaData threadMetaData,
			LinkedList<ThreadState, JobNodeActivateSet> processThreads,
			ProcessState processState, FlowMetaData<?> flowMetaData) {
		super(processThreads);
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

		// Create the thread monitor
		AssetManager flowAssetManager = flowMetaData.getFlowManager();
		this.threadMonitor = flowAssetManager.createAssetMonitor(this);
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

		// Create and register the activate flow
		Flow flow = new FlowImpl(this, this.activeFlows);
		this.activeFlows.addLinkedListEntry(flow);

		// Return the flow
		return flow;
	}

	@Override
	public void flowComplete(Flow flow, JobNodeActivateSet activateSet) {
		// Remove flow from listing.
		// Will trigger thread complete if last flow of thread.
		flow.removeFromLinkedList(activateSet);
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
			JobNodeActivateSet notifySet) {
		this.isEscalating = true;
	}

	@Override
	public void escalationComplete(JobNode currentTaskNode,
			JobNodeActivateSet notifySet) {
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
	public boolean waitOnFlow(JobNode jobNode, JobNodeActivateSet notifySet) {

		// Determine if the same thread
		if (this == jobNode.getFlow().getThreadState()) {
			// Do not wait on this thread
			return false;
		}

		// Return whether job is waiting on this thread.
		// Note: thread may already be complete.
		return this.threadMonitor.waitOnAsset(jobNode, notifySet);
	}

	/*
	 * =================== Asset ==========================================
	 */

	@Override
	public void checkOnAsset(CheckAssetContext report) {
		// TODO implement checking on ThreadState as an Asset
	}

}