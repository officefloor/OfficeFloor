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
package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.internal.structure.CleanupSequence;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link CleanupSequence} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class CleanupSequenceImpl implements CleanupSequence,
		ProcessCompletionListener {

	/**
	 * Active {@link JobNode}.
	 */
	private JobNode activeCleanup = null;

	/**
	 * First {@link JobNode} in the sequence.
	 */
	private JobNode head = null;

	/**
	 * Last {@link JobNode} in the sequence.
	 */
	private JobNode tail = null;

	/*
	 * ==================== CleanupSequence ============================
	 */

	@Override
	public synchronized void registerCleanUpJob(JobNode cleanupJob,
			TeamIdentifier teamIdentifier) {

		// Attempt the job
		if (this.attempJob(cleanupJob, teamIdentifier)) {
			return; // job activated
		}

		// Queue job for when current jobs complete
		if (this.head == null) {
			// No jobs queued, so add to head
			this.head = cleanupJob;
			this.tail = cleanupJob;
		} else {
			// Queue at end of list
			this.tail.setNext(cleanupJob);
			this.tail = cleanupJob;
		}
	}

	/**
	 * <p>
	 * Attempts to activate the {@link JobNode}.
	 * <p>
	 * Only one {@link JobNode} may be active at one time.
	 * 
	 * @param cleanupJob
	 *            Clean up {@link JobNode}.
	 * @param teamIdentifier
	 *            {@link TeamIdentifier}.
	 * @return <code>true</code> if the {@link JobNode} was activated.
	 */
	private boolean attempJob(JobNode cleanupJob, TeamIdentifier teamIdentifier) {

		// Determine if current activate clean up job
		if (this.activeCleanup != null) {
			return false; // Job already active
		}

		// No job active, so activate the job
		this.activeCleanup = cleanupJob;
		cleanupJob.getJobSequence().getThreadState().getProcessState()
				.registerProcessCompletionListener(this);
		cleanupJob.activateJob(teamIdentifier);

		// Job activated
		return true;
	}

	/*
	 * ================ ProcessCompletionListener ======================
	 */

	@Override
	public synchronized void processComplete(TeamIdentifier currentTeam) {

		// No further active job
		this.activeCleanup = null;

		// Obtain the next job
		if (this.head == null) {
			return; // No further jobs
		}

		// Obtain the head job
		JobNode cleanupJob = this.head;
		this.head = cleanupJob.getNext();
		cleanupJob.setNext(null);

		// Activate the job
		this.attempJob(cleanupJob, currentTeam);
	}

}