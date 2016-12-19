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
package net.officefloor.frame.impl.execute.job;

import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl.ActiveThreadState;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeLoop;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link JobNodeLoop} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class JobNodeLoopImpl implements JobNodeLoop {

	/**
	 * {@link TeamIdentifier} for the {@link JobNodeLoopImpl}.
	 */
	private final TeamIdentifier LOOP_TEAM = new TeamIdentifier() {
	};

	/**
	 * Default {@link TeamManagement} to assign {@link JobNode} instances.
	 */
	private final TeamManagement defaultTeam;

	/**
	 * Instantiates.
	 * 
	 * @param defaultTeam
	 *            Default {@link TeamManagement}.
	 */
	public JobNodeLoopImpl(TeamManagement defaultTeam) {
		this.defaultTeam = defaultTeam;
	}

	/*
	 * =================== JobNodeDelegator ===========================
	 */

	@Override
	public void runJobNode(JobNode jobNode) {
		// Run on current thread (will swap to appropriate team as necessary)
		new UnsafeJob(jobNode, LOOP_TEAM).run();
	}

	@Override
	public void delegateJobNode(JobNode jobNode) {

		// Obtain the responsible team
		TeamManagement responsibleTeam = jobNode.getResponsibleTeam();
		if (responsibleTeam == null) {
			responsibleTeam = this.defaultTeam;
		}

		// Delegate job node to the responsible team
		responsibleTeam.getTeam().assignJob(new SafeJob(jobNode, responsibleTeam.getIdentifier()));
	}

	/**
	 * Undertakes the {@link JobNode} loop for a particular {@link ThreadState}.
	 * 
	 * @param threadState
	 *            Particular {@link ThreadState}.
	 * @param head
	 *            Head {@link JobNode}.
	 * @param isThreadStateSafe
	 *            Flag indicating if changes to the {@link ThreadState} are safe
	 *            on the current {@link Thread}.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team}.
	 * @return Optional next {@link JobNode} that requires execution by another
	 *         {@link ThreadState} (or {@link TeamManagement}).
	 */
	private JobNode executeThreadStateJobNodeLoop(JobNode head, boolean isThreadStateSafe, TeamIdentifier currentTeam) {

		// Obtain existing active thread state
		ActiveThreadState existingThreadState = ThreadStateImpl.getActiveThreadState();

		// Obtain the thread state for loop
		ThreadState threadState = head.getThreadState();
		try {
			// Attach thread state to thread
			threadState.attachThreadStateToThread(isThreadStateSafe);

			// Run job loop for the current thread state and team
			JobNode nextJobNode = head;
			do {

				// Ensure appropriate thread state
				if (nextJobNode.getThreadState() != this) {
					// Other thread state to undertake job node loop
					return nextJobNode;
				}

				// Ensure appropriate team undertakes the job node
				TeamManagement responsible = nextJobNode.getResponsibleTeam();
				if ((responsible != null) && (currentTeam != responsible.getIdentifier())) {
					// Different responsible team
					return nextJobNode;
				}

				// Ensure providing appropriate thread state safety
				if ((!isThreadStateSafe) && (nextJobNode.isRequireThreadStateSafety())) {
					// Exit loop to obtain thread state safety
					return nextJobNode;
				}

				// Required team, thread state and safety, so execute
				nextJobNode = nextJobNode.doJob();

			} while (nextJobNode != null);

		} finally {
			// Detach thread state from the thread
			threadState.detachThreadStateFromThread();

			// Re-attach possible previous thread state
			if (existingThreadState != null) {
				existingThreadState.threadState.attachThreadStateToThread(existingThreadState.isThreadStateSafe);
			}
		}

		// No further job nodes to execute
		return null;
	}

	/**
	 * <p>
	 * {@link Job} implementation that is not {@link Thread} safe.
	 * <p>
	 * This is available for the initial {@link JobNode} of a
	 * {@link ProcessState} to avoid synchronising overheads if no other
	 * responsible {@link TeamManagement} nor {@link ThreadState} is involved.
	 */
	private class UnsafeJob implements Job {

		/**
		 * Initial {@link JobNode}.
		 */
		private final JobNode initialJobNode;

		/**
		 * {@link TeamIdentifier} of the current {@link Team} executing this
		 * {@link Job}.
		 */
		protected TeamIdentifier currentTeam;

		/**
		 * Instantiate.
		 * 
		 * @param initialJobNode
		 *            Initial {@link JobNode}.
		 * @param currentTeam
		 *            {@link TeamIdentifier} of the current {@link Team}
		 *            executing this {@link Job}.
		 */
		public UnsafeJob(JobNode initialJobNode, TeamIdentifier currentTeam) {
			this.initialJobNode = initialJobNode;
			this.currentTeam = currentTeam;
		}

		/**
		 * Undertakes the {@link JobNode} for the {@link ThreadState}.
		 * 
		 * @param head
		 *            Head {@link JobNode} for loop.
		 * @param isRequireThreadStateSafe
		 *            <code>true</code> to provide {@link Thread} safety on
		 *            executing the {@link JobNode} instances.
		 * @return Optional next {@link JobNode} that requires execution by
		 *         another {@link ThreadState} (or {@link TeamManagement} or
		 *         requires {@link Thread} safety).
		 */
		protected JobNode doThreadStateJobNodeLoop(JobNode head, boolean isRequireThreadStateSafety) {
			if (isRequireThreadStateSafety) {
				// Execute loop with thread state safety
				synchronized (head.getThreadState()) {
					return JobNodeLoopImpl.this.executeThreadStateJobNodeLoop(head, true, this.currentTeam);
				}
			} else {
				// Execute loop unsafely (only one thread, avoid overhead)
				return JobNodeLoopImpl.this.executeThreadStateJobNodeLoop(head, false, this.currentTeam);
			}
		}

		/**
		 * Assigns the {@link JobNode} to its responsible
		 * {@link TeamManagement}.
		 * 
		 * @param jobNode
		 *            {@link JobNode}.
		 * @param responsibleTeam
		 *            Responsible {@link TeamManagement} for the
		 *            {@link JobNode}.
		 */
		protected void assignJob(JobNode jobNode, TeamManagement responsibleTeam) {

			// First assigning, so must synchronise thread state
			synchronized (jobNode.getThreadState()) {
			}

			// Assign the job to the responsible team
			responsibleTeam.getTeam().assignJob(new SafeJob(jobNode, responsibleTeam.getIdentifier()));
		}

		/*
		 * ==================== Job ====================
		 */

		@Override
		public Object getProcessIdentifier() {
			return this.initialJobNode.getThreadState().getProcessState().getProcessIdentifier();
		}

		@Override
		public void run() {

			// Execute the job nodes for the thread state
			JobNode nextJobNode = this.initialJobNode;
			do {

				// Ensure appropriate team undertakes the job
				TeamManagement responsible = nextJobNode.getResponsibleTeam();
				if ((responsible != null) && (this.currentTeam != responsible.getIdentifier())) {
					// Different responsible team
					this.assignJob(nextJobNode, responsible);
					return;
				}

				// Undertake loop for thread state
				nextJobNode = this.doThreadStateJobNodeLoop(nextJobNode, nextJobNode.isRequireThreadStateSafety());

			} while (nextJobNode != null);
		}
	}

	/**
	 * {@link Thread} safe {@link Job} implementation.
	 */
	private class SafeJob extends UnsafeJob {

		/**
		 * Instantiate.
		 * 
		 * @param initialJobNode
		 *            Initial {@link JobNode}.
		 * @param currentTeam
		 *            Current {@link TeamIdentifier}.
		 */
		public SafeJob(JobNode initialJobNode, TeamIdentifier currentTeam) {
			super(initialJobNode, currentTeam);
		}

		/*
		 * ====================== UnsafeJobImpl ======================
		 */

		@Override
		protected JobNode doThreadStateJobNodeLoop(JobNode head, boolean isRequireThreadSafety) {
			// Always require thread state safety
			return super.doThreadStateJobNodeLoop(head, true);
		}

		@Override
		protected void assignJob(JobNode jobNode, TeamManagement responsibleTeam) {
			// No need to synchronise on assigning jobs, as loop is thread safe
			responsibleTeam.getTeam().assignJob(new SafeJob(jobNode, responsibleTeam.getIdentifier()));
		}
	}

}