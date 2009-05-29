/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.api.escalate.FlowJoinTimedOutEscalation;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.impl.execute.flow.FlowImpl;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.ComparatorLinkedListSet;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadStateImpl extends
		AbstractLinkedListSetEntry<ThreadState, ProcessState> implements
		ThreadState {

	/**
	 * Active {@link Flow} instances for this {@link ThreadState}.
	 */
	protected final LinkedListSet<Flow, ThreadState> activeFlows = new StrictLinkedListSet<Flow, ThreadState>() {
		@Override
		protected ThreadState getOwner() {
			return ThreadStateImpl.this;
		}
	};

	/**
	 * Unique set of joined {@link JobNode} instances to this
	 * {@link ThreadState}.
	 */
	private final LinkedListSet<JoinedJobNode, ThreadState> joinedJobNodes = new ComparatorLinkedListSet<JoinedJobNode, ThreadState>() {
		@Override
		protected ThreadState getOwner() {
			return ThreadStateImpl.this;
		}

		@Override
		protected boolean isEqual(JoinedJobNode entryA, JoinedJobNode entryB) {
			return (entryA.jobNode == entryB.jobNode);
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
	 * {@link AssetManager} for monitoring this {@link ThreadState}.
	 */
	private final AssetManager threadManager;

	/**
	 * Flag indicating that looking for {@link EscalationFlow}.
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
	public ThreadStateImpl(ThreadMetaData threadMetaData,
			ProcessState processState, FlowMetaData<?> flowMetaData) {
		this.threadMetaData = threadMetaData;
		this.processState = processState;

		// Create the managed object containers
		ManagedObjectMetaData<?>[] moMetaData = this.threadMetaData
				.getManagedObjectMetaData();
		this.managedObjectContainers = new ManagedObjectContainer[moMetaData.length];
		for (int i = 0; i < this.managedObjectContainers.length; i++) {
			this.managedObjectContainers[i] = moMetaData[i]
					.createManagedObjectContainer(this.processState);
		}

		// Create the administrator containers
		AdministratorMetaData<?, ?>[] adminMetaData = this.threadMetaData
				.getAdministratorMetaData();
		this.administratorContainers = new AdministratorContainer[adminMetaData.length];
		for (int i = 0; i < this.administratorContainers.length; i++) {
			this.administratorContainers[i] = adminMetaData[i]
					.createAdministratorContainer();
		}

		// Obtain the thread manager
		this.threadManager = flowMetaData.getFlowManager();
	}

	/*
	 * ====================== LinkedListSetEntry ===========================
	 */

	@Override
	public ProcessState getLinkedListSetOwner() {
		return this.processState;
	}

	/*
	 * ===================== ThreadState ==================================
	 * 
	 * Methods do not requiring synchronising as will all be called within the
	 * ThreadState lock taken by the JobContainer.
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
		Flow flow = new FlowImpl(this);
		this.activeFlows.addEntry(flow);

		// Return the flow
		return flow;
	}

	@Override
	public void flowComplete(Flow flow, JobNodeActivateSet activateSet) {
		// Remove flow from active flow listing
		if (this.activeFlows.removeEntry(flow)) {

			// Do nothing if searching for escalation
			if (this.isEscalating) {
				return;
			}

			// No more active flows so thread is complete
			this.isFlowComplete = true;

			// Unload managed objects
			for (int i = 0; i < this.managedObjectContainers.length; i++) {
				this.managedObjectContainers[i]
						.unloadManagedObject(activateSet);
			}

			// Activate all jobs waiting on this thread permanently
			JoinedJobNode joinedJobNode = this.joinedJobNodes.purgeEntries();
			while (joinedJobNode != null) {
				joinedJobNode.assetMonitor.activateJobNodes(activateSet, true);
				joinedJobNode = joinedJobNode.getNext();
			}

			// Thread complete
			this.processState.threadComplete(this, activateSet);
		}
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
	public boolean waitOnFlow(JobNode jobNode, long timeout, Object token,
			JobNodeActivateSet activateSet) {
		// Need to synchronise as will be called by other ThreadStates
		synchronized (this.getThreadLock()) {

			// Determine if already complete
			if (this.isFlowComplete) {
				// Thread already complete (so activate job immediately)
				activateSet.addJobNode(jobNode);
				return false; // not waiting
			}

			// Determine if the same thread
			if (this == jobNode.getFlow().getThreadState()) {
				// Do not wait on this thread (activate job immediately)
				activateSet.addJobNode(jobNode);
				return false; // not waiting
			}

			// Create and add the joined job node
			JoinedJobNode joinedJobNode = new JoinedJobNode(jobNode, timeout,
					token);
			this.joinedJobNodes.addEntry(joinedJobNode);

			// Have job node wait on thread to complete (should always wait)
			return joinedJobNode.assetMonitor.waitOnAsset(jobNode, activateSet);
		}
	}

	/*
	 * =================== Asset ==========================================
	 */

	/**
	 * Contains details of the joined {@link JobNode} to this
	 * {@link ThreadState}.
	 */
	private class JoinedJobNode extends
			AbstractLinkedListSetEntry<JoinedJobNode, ThreadState> implements
			Asset {

		/**
		 * {@link JobNode} waiting for this {@link ThreadState} to complete.
		 */
		public final JobNode jobNode;

		/**
		 * {@link AssetMonitor} to monitor this join.
		 */
		public final AssetMonitor assetMonitor;

		/**
		 * Time by which this {@link ThreadState} must complete for this joined
		 * {@link JobNode}.
		 */
		public final long timeoutTime;

		/**
		 * Token identifying the join.
		 */
		public final Object token;

		/**
		 * Initiate.
		 * 
		 * @param jobNode
		 *            {@link JobNode} waiting for this {@link ThreadState} to
		 *            complete.
		 * @param assetMonitor
		 *            {@link AssetMonitor} to monitor this join.
		 * @param timeoutTime
		 *            The maximum time to wait in milliseconds for the
		 *            {@link Flow} to complete.
		 * @param token
		 *            Token identifying the join.
		 */
		public JoinedJobNode(JobNode jobNode, long timeout, Object token) {
			this.jobNode = jobNode;
			this.token = token;

			// Create and register the monitor for the join
			this.assetMonitor = ThreadStateImpl.this.threadManager
					.createAssetMonitor(this);

			// Calculate the time that join should time out
			this.timeoutTime = System.currentTimeMillis() + timeout;
		}

		/*
		 * ==================== LinkedListSetEntry ======================
		 */

		@Override
		public ThreadState getLinkedListSetOwner() {
			return ThreadStateImpl.this;
		}

		/*
		 * ========================= Asset ===============================
		 */

		@Override
		public void checkOnAsset(CheckAssetContext context) {
			synchronized (ThreadStateImpl.this.getThreadLock()) {

				// Obtain the current time
				long currentTime = context.getTime();

				// Check if time past join completion time
				if (currentTime > this.timeoutTime) {

					// Fail the job due to join time out
					context.failJobNodes(new FlowJoinTimedOutEscalation(
							this.token), true);

					// Remove job node from the list (as no longer joined)
					ThreadStateImpl.this.joinedJobNodes.removeEntry(this);
				}
			}
		}
	}

}