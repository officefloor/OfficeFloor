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

import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * <p>
 * {@link Job} implementation that is not {@link Thread} safe.
 * <p>
 * This is available for the initial {@link JobNode} of a {@link ProcessState}
 * to avoid synchronising overheads if no other responsible
 * {@link TeamManagement} is involved.
 *
 * @author Daniel Sagenschneider
 */
public class UnsafeJobImpl implements Job {

	/**
	 * Initial {@link JobNode}.
	 */
	private final JobNode initialJobNode;

	/**
	 * Next {@link Job} that is managed by the {@link Team}.
	 */
	private Job nextJob = null;

	/**
	 * Instantiate.
	 * 
	 * @param initialJobNode
	 *            Initial {@link JobNode}.
	 */
	public UnsafeJobImpl(JobNode initialJobNode) {
		this.initialJobNode = initialJobNode;
	}

	/**
	 * Undertakes the {@link JobNode} loop for a particular {@link ThreadState}.
	 * 
	 * @param threadState
	 *            Particular {@link ThreadState}.
	 * @param head
	 *            Head {@link JobNode}.
	 * @param context
	 *            {@link JobContext}.
	 * @return Optional next {@link JobNode} that requires execution by another
	 *         {@link ThreadState} (or {@link TeamManagement}).
	 */
	protected JobNode doThreadStateJobNodeLoop(JobNode head, JobContext context) {

		// Obtain the current team
		TeamIdentifier currentTeam = context.getCurrentTeam();

		// Obtain the thread state for loop
		ThreadState threadState = head.getThreadState();
		try {
			// Attach thread state to thread
			threadState.attachThreadStateToThread();

			// Run job loop for the current thread state and team
			JobNode nextJobNode = head;
			do {

				// Ensure appropriate thread state
				if (nextJobNode.getThreadState() != this) {
					// Other thread state to undertake job node loop
					return nextJobNode;
				}

				// Ensure appropriate team undertakes the job
				TeamManagement responsible = nextJobNode.getResponsibleTeam();
				if ((responsible != null) && (currentTeam != responsible.getIdentifier())) {
					// Different responsible team
					return nextJobNode;
				}

				// Same team and thread state, so undertake execution
				nextJobNode = nextJobNode.doJob(context);

			} while (nextJobNode != null);

		} finally {
			// Detach thread state from the thread
			threadState.detachThreadStateFromThread();
		}

		// No further job nodes to execute
		return null;
	}

	/**
	 * Assigns the {@link JobNode} to its responsible {@link TeamManagement}.
	 * 
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param currentTeam
	 *            Current {@link TeamIdentifier}.
	 */
	protected void assignJob(JobNode jobNode, TeamIdentifier currentTeam) {

		// First assigning, so must synchronise thread state
		synchronized (jobNode.getThreadState()) {
		}

		// Assign the job to the responsible team
		jobNode.getResponsibleTeam().getTeam().assignJob(new SafeJobImpl(jobNode), currentTeam);
	}

	/*
	 * ========================= Job ========================================
	 */

	@Override
	public void doJob(JobContext context) {

		// Obtain the current thread state
		TeamIdentifier currentTeam = context.getCurrentTeam();

		// Execute the job nodes for the thread state
		JobNode nextJobNode = this.initialJobNode;
		do {

			// Ensure appropriate team undertakes the job
			TeamManagement responsible = nextJobNode.getResponsibleTeam();
			if ((responsible != null) && (currentTeam != responsible.getIdentifier())) {
				// Different responsible team
				this.assignJob(nextJobNode, currentTeam);
				return;
			}

			// Undertake loop for thread state
			nextJobNode = this.doThreadStateJobNodeLoop(nextJobNode, context);

		} while (nextJobNode != null);
	}

	@Override
	public Object getProcessIdentifier() {
		return this.initialJobNode.getThreadState().getProcessState().getProcessIdentifier();
	}

	@Override
	public void setNextJob(Job job) {
		this.nextJob = job;
	}

	@Override
	public Job getNextJob() {
		return this.nextJob;
	}

}