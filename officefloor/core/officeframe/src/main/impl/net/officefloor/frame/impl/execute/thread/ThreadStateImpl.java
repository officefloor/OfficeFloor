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
import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.impl.execute.job.JobImpl;
import net.officefloor.frame.impl.execute.job.JobSequenceImpl;
import net.officefloor.frame.impl.execute.job.ProcessCriticalSectionJobNode;
import net.officefloor.frame.impl.execute.job.RunnableJobNode;
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
import net.officefloor.frame.internal.structure.FlowCallbackJobNodeFactory;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeRunnable;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadProfiler;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Implementation of the {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadStateImpl extends AbstractLinkedListSetEntry<ThreadState, ProcessState> implements ThreadState {

	/**
	 * Active {@link ThreadState} for the executing {@link Thread}.
	 */
	private static final ThreadLocal<ThreadStateImpl> activeThreadState = new ThreadLocal<>();

	/**
	 * {@link TeamIdentifier} for decoupling current {@link Team} from invoking
	 * the {@link JobNode}.
	 */
	public static final TeamIdentifier INSTIGATE_TEAM_IDENTIFIER = new TeamIdentifier() {
	};

	/**
	 * Active {@link Flow} instances for this {@link ThreadState}.
	 */
	protected final LinkedListSet<Flow, ThreadState> activeJobSequences = new StrictLinkedListSet<Flow, ThreadState>() {
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
	 * {@link FlowCallbackJobNodeFactory}.
	 */
	private final FlowCallbackJobNodeFactory callbackFactory;

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
	 * @param assetManager
	 *            {@link AssetManager} for the {@link ThreadState}.
	 * @param processProfiler
	 *            {@link ProcessProfiler}. May be <code>null</code>.
	 * @param callbackFactory
	 *            {@link FlowCallbackJobNodeFactory} to create the
	 *            {@link FlowCallback} on completion of this
	 *            {@link ThreadState}. May be <code>null<code>.
	 */
	public ThreadStateImpl(ThreadMetaData threadMetaData, ProcessState processState, AssetManager assetManager,
			ProcessProfiler processProfiler, FlowCallbackJobNodeFactory callbackFactory) {
		this.threadMetaData = threadMetaData;
		this.processState = processState;
		this.threadManager = assetManager;
		this.callbackFactory = callbackFactory;

		// Create array to reference the managed objects
		ManagedObjectMetaData<?>[] moMetaData = this.threadMetaData.getManagedObjectMetaData();
		this.managedObjectContainers = new ManagedObjectContainer[moMetaData.length];

		// Create the array to reference the governances
		GovernanceMetaData<?, ?>[] governanceMetaData = this.threadMetaData.getGovernanceMetaData();
		this.governanceContainers = new GovernanceContainer[governanceMetaData.length];

		// Create array to reference the administrators
		AdministratorMetaData<?, ?>[] adminMetaData = this.threadMetaData.getAdministratorMetaData();
		this.administratorContainers = new AdministratorContainer[adminMetaData.length];

		// Create thread profiler
		this.profiler = (processProfiler == null ? null : processProfiler.addThread(this));
	}

	/**
	 * Instigates the {@link JobNode} with its responsible {@link Team}.
	 * 
	 * @param jobNode
	 *            {@link JobNode} to instigate.
	 */
	private void instigateJobNode(JobNode jobNode) {
		TeamManagement responsible = jobNode.getResponsibleTeam();
		responsible.getTeam().assignJob(new JobImpl(jobNode), INSTIGATE_TEAM_IDENTIFIER);
	}

	/**
	 * Acquires the {@link ThreadState} lock.
	 */
	private void acquireThreadLock() {
		// TODO implement
	}

	/**
	 * Releases the {@link ThreadState} lock.
	 */
	private void releaseThreadLock() {
		// TODO implement
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
	 */

	@Override
	public void doJobNodeLoop(JobNode head, JobContext context) {

		// Obtain the current team
		TeamIdentifier currentTeam = context.getCurrentTeam();

		// Current execution
		TeamManagement responsible;
		Job nextJob = null;

		try {
			// Activate this thread state on the thread
			activeThreadState.set(this);

			// Acquire lock for thread critical state
			// TODO don't take lock if first job of thread state
			this.acquireThreadLock();

			// Run job loop for the current team
			JobNode nextJobNode = head;
			do {

				// Ensure appropriate thread state
				if (nextJobNode.getThreadState() != this) {
					this.instigateJobNode(nextJobNode);
					return; // other thread state to undertake job loop
				}

				// Ensure appropriate team undertakes the job
				responsible = nextJobNode.getResponsibleTeam();
				if ((responsible == null) || (currentTeam == responsible.getIdentifier())) {
					// Same team, so undertake execution
					nextJobNode = nextJobNode.doJob(context);

				} else {
					// Different responsible team
					nextJob = new JobImpl(nextJobNode);
					nextJobNode = null; // quit loop
				}

			} while (nextJobNode != null);

		} finally {						
			// Release lock for thread critical state
			this.releaseThreadLock();

			// Deactivate this thread state on the thread
			activeThreadState.set(null);
		}

		// Activate the next possible job with responsible team
		if (nextJob != null) {
			responsible.getTeam().assignJob(nextJob, currentTeam);
		}
	}

	@Override
	public boolean isJobNodeLoopThread() {
		ThreadState active = activeThreadState.get();
		return (this == active);
	}

	@Override
	public void run(JobNodeRunnable runnable, TeamManagement responsibleTeam) {
		this.instigateJobNode(new RunnableJobNode(runnable, responsibleTeam, this));
	}

	@Override
	public void spawnThreadState(final FlowMetaData<?> flowMetaData, final Object parameter,
			final JobNode instigatingJobNode, final FlowCallbackJobNodeFactory callbackFactory) {

		// Ensure have thread lock (avoids corruption due to process critical sections)
		this.acquireThreadLock();
		
		// Obtain the responsible team for the first task of the flow
		TeamManagement responsibleTeam = flowMetaData.getInitialTaskMetaData().getResponsibleTeam();

		// Run the process critical section to spawn a thread state
		this.runProcessCriticalSection(new JobNodeRunnable() {
			@Override
			public JobNode run() {
				// Obtain the task meta-data for instigating the flow
				TaskMetaData<?, ?, ?> initTaskMetaData = flowMetaData.getInitialTaskMetaData();

				// Create thread to execute asynchronously
				AssetManager flowAssetManager = flowMetaData.getFlowManager();
				Flow asyncFlow = processState.createThread(flowAssetManager, callbackFactory);

				// Create job node for execution
				return asyncFlow.createManagedJobNode(initTaskMetaData, null, parameter,
						GovernanceDeactivationStrategy.ENFORCE);
			}
		}, responsibleTeam, null);
	}

	@Override
	public void runProcessCriticalSection(JobNodeRunnable runnable, final TeamManagement responsibleTeam,
			JobNode continueJobNode) {

		// Determine if the main thread
		ThreadState active = activeThreadState.get();
		if ((this == active) && (this == this.processState.getMainThreadState())) {
			// Job loop thread (all state is valid)
			this.doJobNodeLoop(
					new ProcessCriticalSectionJobNode(runnable, responsibleTeam, this.processState, continueJobNode),
					new JobContext() {
						@Override
						public long getTime() {
							return System.currentTimeMillis();
						}

						@Override
						public TeamIdentifier getCurrentTeam() {
							return responsibleTeam.getIdentifier();
						}

						@Override
						public boolean continueExecution() {
							return true;
						}
					});
			return;
		}

		// Outside thread safety, undertake on main thread
		this.processState.getMainThreadState().run(runnable, responsibleTeam);
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
	public Flow createJobSequence() {

		// Create and register the activate Job Sequence
		Flow jobSequence = new JobSequenceImpl(this);
		this.activeJobSequences.addEntry(jobSequence);

		// Return the Job Sequence
		return jobSequence;
	}

	@Override
	public JobNode jobSequenceComplete(Flow jobSequence, TeamIdentifier currentTeam, JobNode continueJobNode) {

		// Remove Job Sequence from active Job Sequence listing
		if (this.activeJobSequences.removeEntry(jobSequence)) {

			// Do nothing if searching for escalation
			if (this.isEscalating) {
				return null;
			}

			// Deactivate governance
			GovernanceDeactivationStrategy deactivationStrategy = this.threadMetaData
					.getGovernanceDeactivationStrategy();
			switch (deactivationStrategy) {
			case ENFORCE:
				// Enforce any active governance
				for (int i = 0; i < this.governanceContainers.length; i++) {
					GovernanceContainer<?, ?> container = this.governanceContainers[i];
					if (container != null) {
						JobNode enforceJobNode = container.enforceGovernance(continueJobNode);
						if (enforceJobNode != null) {
							return enforceJobNode;
						}
					}
				}
				break;

			case DISREGARD:
				// Disregard any active governance
				for (int i = 0; i < this.governanceContainers.length; i++) {
					GovernanceContainer<?, ?> container = this.governanceContainers[i];
					if (container != null) {
						JobNode disregardJobNode = container.disregardGovernance(continueJobNode);
						if (disregardJobNode != null) {
							return disregardJobNode;
						}
					}
				}
				break;

			default:
				throw new IllegalStateException(
						"Unknown " + GovernanceDeactivationStrategy.class.getSimpleName() + " " + deactivationStrategy);
			}

			// No more active job sequences so thread is complete
			this.isFlowComplete = true;

			// Unload managed objects (some may not have been used)
			for (int i = 0; i < this.managedObjectContainers.length; i++) {
				ManagedObjectContainer container = this.managedObjectContainers[i];
				if (container != null) {
					JobNode unloadJobNode = container.unloadManagedObject(continueJobNode);
					if (unloadJobNode != null) {
						return unloadJobNode;
					}
				}
			}

			// Activate all jobs waiting on this thread permanently
			JoinedJobNode joinedJobNode = this.joinedJobNodes.purgeEntries();
			while (joinedJobNode != null) {
				return joinedJobNode.jobNode;
				// joinedJobNode = joinedJobNode.getNext();
			}

			// Thread complete
			return this.processState.threadComplete(this);
		}

		// Thread complete
		return null;
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
			container = this.threadMetaData.getGovernanceMetaData()[index].createGovernanceContainer(this, index);
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
			container = this.threadMetaData.getAdministratorMetaData()[index].createAdministratorContainer();
			this.administratorContainers[index] = container;
		}
		return container;
	}

	@Override
	public void escalationStart(JobNode currentTaskNode) {
		this.isEscalating = true;
	}

	@Override
	public void escalationComplete(JobNode currentTaskNode) {
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

	/*
	 * ======================= FlowAsset ======================================
	 */

	@Override
	public JobNode waitOnFlow(JobNode jobNode, long timeout, Object token) {
		// Take lock on thread state, as called by different ThreadState/Thread

		// Determine if already complete
		if (this.isFlowComplete) {
			// Thread already complete (so activate job immediately)
			return jobNode;
		}

		// Determine if the same thread
		if (this == jobNode.getThreadState()) {
			// Do not wait on this thread (activate job immediately)
			return jobNode;
		}

		// Create and add the joined job node
		JoinedJobNode joinedJobNode = new JoinedJobNode(jobNode, timeout, token);
		this.joinedJobNodes.addEntry(joinedJobNode);

		// Have job node wait on thread to complete
		return joinedJobNode.assetMonitor.waitOnAsset(jobNode);
	}

	/*
	 * =================== Asset ==========================================
	 */

	/**
	 * Synchronized checking on this {@link Asset}.
	 * 
	 * @param joinedJobNode
	 *            {@link JoinedJobNode}.
	 * @param context
	 *            {@link CheckAssetContext}.
	 */
	private void checkOnAsset(JoinedJobNode joinedJobNode, CheckAssetContext context) {

		// Obtain the current time
		long currentTime = context.getTime();

		// Check if time past join completion time
		if (currentTime > joinedJobNode.timeoutTime) {

			// Fail the job due to join time out
			context.failJobNodes(new FlowJoinTimedOutEscalation(joinedJobNode.token), true);

			// Remove job node from the list (as no longer joined)
			this.joinedJobNodes.removeEntry(joinedJobNode);
		}
	}

	/**
	 * Contains details of the joined {@link JobNode} to this
	 * {@link ThreadState}.
	 */
	private class JoinedJobNode extends AbstractLinkedListSetEntry<JoinedJobNode, ThreadState> implements Asset {

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
			this.assetMonitor = ThreadStateImpl.this.threadManager.createAssetMonitor(this);

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
			ThreadStateImpl.this.checkOnAsset(this, context);
		}
	}

}