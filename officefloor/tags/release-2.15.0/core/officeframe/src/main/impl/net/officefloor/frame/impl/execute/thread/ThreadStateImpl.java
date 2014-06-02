/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.api.escalate.FlowJoinTimedOutEscalation;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.execute.job.JobSequenceImpl;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.ComparatorLinkedListSet;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadProfiler;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Implementation of the {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadStateImpl extends
		AbstractLinkedListSetEntry<ThreadState, ProcessState> implements
		ThreadState {

	/**
	 * Active {@link JobSequence} instances for this {@link ThreadState}.
	 */
	protected final LinkedListSet<JobSequence, ThreadState> activeJobSequences = new StrictLinkedListSet<JobSequence, ThreadState>() {
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
	 * {@link GovernanceContainer} instances for the {@link ProcessState}.
	 */
	private final GovernanceContainer<?, ?>[] governanceContainers;

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
	 * {@link ThreadProfiler}.
	 */
	private final ThreadProfiler profiler;

	/**
	 * Flag indicating that looking for {@link EscalationFlow}.
	 */
	private boolean isEscalating = false;

	/**
	 * Flag indicating that a setup {@link Task} or {@link GovernanceActivity}
	 * was triggered.
	 */
	private boolean isTriggerTaskOrActivity = false;

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
	 * @param assetManager
	 *            {@link AssetManager} for the {@link ThreadState}.
	 * @param processProfiler
	 *            {@link ProcessProfiler}. May be <code>null</code>.
	 */
	public ThreadStateImpl(ThreadMetaData threadMetaData,
			ProcessState processState, AssetManager assetManager,
			ProcessProfiler processProfiler) {
		this.threadMetaData = threadMetaData;
		this.processState = processState;
		this.threadManager = assetManager;

		// Create array to reference the managed objects
		ManagedObjectMetaData<?>[] moMetaData = this.threadMetaData
				.getManagedObjectMetaData();
		this.managedObjectContainers = new ManagedObjectContainer[moMetaData.length];

		// Create the array to reference the governances
		GovernanceMetaData<?, ?>[] governanceMetaData = this.threadMetaData
				.getGovernanceMetaData();
		this.governanceContainers = new GovernanceContainer[governanceMetaData.length];

		// Create array to reference the administrators
		AdministratorMetaData<?, ?>[] adminMetaData = this.threadMetaData
				.getAdministratorMetaData();
		this.administratorContainers = new AdministratorContainer[adminMetaData.length];

		// Create thread profiler
		this.profiler = (processProfiler == null ? null : processProfiler
				.addThread(this));
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
	public JobSequence createJobSequence() {

		// Create and register the activate Job Sequence
		JobSequence jobSequence = new JobSequenceImpl(this);
		this.activeJobSequences.addEntry(jobSequence);

		// Return the Job Sequence
		return jobSequence;
	}

	@Override
	public void jobSequenceComplete(JobSequence jobSequence,
			JobNodeActivateSet activateSet, TeamIdentifier currentTeam) {

		// Remove Job Sequence from active Job Sequence listing
		if (this.activeJobSequences.removeEntry(jobSequence)) {

			// Do nothing if searching for escalation
			if (this.isEscalating) {
				return;
			}

			// Reset trigger for disregard governance
			this.isTriggerTaskOrActivity = false;

			// Create the container context for job sequence completion
			ContainerContext containerContext = new JobSequenceCompleteContainerContext(
					currentTeam);

			// Deactivate governance
			GovernanceDeactivationStrategy deactivationStrategy = this.threadMetaData
					.getGovernanceDeactivationStrategy();
			switch (deactivationStrategy) {
			case ENFORCE:
				// Enforce any active governance
				for (int i = 0; i < this.governanceContainers.length; i++) {
					GovernanceContainer<?, ?> container = this.governanceContainers[i];
					if (container != null) {
						container.enforceGovernance(containerContext);
					}
				}
				break;

			case DISREGARD:
				// Disregard any active governance
				for (int i = 0; i < this.governanceContainers.length; i++) {
					GovernanceContainer<?, ?> container = this.governanceContainers[i];
					if (container != null) {
						container.disregardGovernance(containerContext);
					}
				}
				break;

			default:
				throw new IllegalStateException("Unknown "
						+ GovernanceDeactivationStrategy.class.getSimpleName()
						+ " " + deactivationStrategy);
			}

			// Determine if setup task or activity for cleanup.
			// Note not check list as passive team may already remove.
			if (this.isTriggerTaskOrActivity) {
				return; // wait for cleanup of Governance
			}

			// No more active job sequences so thread is complete
			this.isFlowComplete = true;

			// Unload managed objects (some may not have been used)
			for (int i = 0; i < this.managedObjectContainers.length; i++) {
				ManagedObjectContainer container = this.managedObjectContainers[i];
				if (container != null) {
					container.unloadManagedObject(activateSet, currentTeam);
				}
			}

			// Activate all jobs waiting on this thread permanently
			JoinedJobNode joinedJobNode = this.joinedJobNodes.purgeEntries();
			while (joinedJobNode != null) {
				joinedJobNode.assetMonitor.activateJobNodes(activateSet, true);
				joinedJobNode = joinedJobNode.getNext();
			}

			// Thread complete
			this.processState.threadComplete(this, activateSet, currentTeam);
		}
	}

	@Override
	public ProcessState getProcessState() {
		return this.processState;
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		// Lazy load the Managed Object Container
		// (This should be thread safe as should always be called within the
		// Process lock of the Thread before the Job uses it).
		ManagedObjectContainer container = this.managedObjectContainers[index];
		if (container == null) {
			container = this.threadMetaData.getManagedObjectMetaData()[index]
					.createManagedObjectContainer(this.processState);
			this.managedObjectContainers[index] = container;
		}
		return container;
	}

	@Override
	public boolean isGovernanceActive(int index) {
		// Determine if container is active (not created is not active).
		// (This should be thread safe as should always be called within the
		// Thread lock of the Thread before the Job uses it).
		GovernanceContainer<?, ?> container = this.governanceContainers[index];
		return (container != null) && (container.isActive());
	}

	@Override
	public GovernanceContainer<?, ?> getGovernanceContainer(int index) {
		// Lazy load the Governance Container
		// (This should be thread safe as should always be called within the
		// Thread lock of the Thread before the Job uses it).
		GovernanceContainer<?, ?> container = this.governanceContainers[index];
		if (container == null) {
			container = this.threadMetaData.getGovernanceMetaData()[index]
					.createGovernanceContainer(this, index);
			this.governanceContainers[index] = container;
		}
		return container;
	}

	@Override
	public void governanceComplete(GovernanceContainer<?, ?> governanceContainer) {
		// Unregister the governance
		int index = governanceContainer.getProcessRegisteredIndex();
		this.governanceContainers[index] = null;
	}

	@Override
	public AdministratorContainer<?, ?> getAdministratorContainer(int index) {
		// Lazy load the Administrator Container
		// (This should be thread safe as should always called within the
		// Process lock by the WorkContainer)
		AdministratorContainer<?, ?> container = this.administratorContainers[index];
		if (container == null) {
			container = this.threadMetaData.getAdministratorMetaData()[index]
					.createAdministratorContainer();
			this.administratorContainers[index] = container;
		}
		return container;
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

	@Override
	public void profile(JobMetaData jobMetaData) {

		// Only profile if have profiler
		if (this.profiler == null) {
			return;
		}

		// Profile the job execution
		this.profiler.profileJob(jobMetaData);
	}

	/**
	 * {@link ContainerContext} for completing the {@link JobSequence}.
	 */
	private class JobSequenceCompleteContainerContext implements
			ContainerContext {

		/**
		 * {@link TeamIdentifier} of current {@link Team} completing the
		 * {@link JobSequence}.
		 */
		private final TeamIdentifier currentTeam;

		/**
		 * Initiate.
		 * 
		 * @param currentTeam
		 *            {@link TeamIdentifier} of current {@link Team} completing
		 *            the {@link JobSequence}.
		 */
		public JobSequenceCompleteContainerContext(TeamIdentifier currentTeam) {
			this.currentTeam = currentTeam;
		}

		/*
		 * ==================== ContainerContext ======================
		 */

		@Override
		public void flagJobToWait() {
			// Ignore, as not waiting for tidy up
		}

		@Override
		public void addSetupTask(TaskMetaData<?, ?, ?> taskMetaData,
				Object parameter) {

			// Create the flow to run setup task
			JobSequence jobSequence = ThreadStateImpl.this.createJobSequence();

			// Create and activate the task
			JobNode taskNode = jobSequence.createTaskNode(taskMetaData, null,
					parameter, GovernanceDeactivationStrategy.ENFORCE);
			taskNode.activateJob(this.currentTeam);

			// Flag triggered setup task
			ThreadStateImpl.this.isTriggerTaskOrActivity = true;
		}

		@Override
		public void addGovernanceActivity(GovernanceActivity<?, ?> activity) {

			// Create the flow to run the activity
			JobSequence jobSequence = ThreadStateImpl.this.createJobSequence();

			// Create and activate the activity
			JobNode activityNode = jobSequence.createGovernanceNode(activity,
					null);
			activityNode.activateJob(this.currentTeam);

			// Flag triggered governance activity
			ThreadStateImpl.this.isTriggerTaskOrActivity = true;
		}
	};

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
			if (this == jobNode.getJobSequence().getThreadState()) {
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
		 *            {@link JobSequence} to complete.
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