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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.internal.structure.CleanupSequence;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeLoop;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link CleanupSequence} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class CleanupSequenceImpl implements CleanupSequence, ProcessCompletionListener {

	/**
	 * {@link ProcessState} to be cleaned up.
	 */
	private final ProcessState processState;

	/**
	 * {@link JobNodeLoop}.
	 */
	private final JobNodeLoop jobNodeLoop;

	/**
	 * First {@link JobNode} in the sequence.
	 */
	private JobNode head = null;

	/**
	 * Last {@link JobNode} in the sequence.
	 */
	private JobNode tail = null;

	/**
	 * {@link CleanupEscalation} instances.
	 */
	private List<CleanupEscalation> cleanupEscalations = null;

	/**
	 * Instantiate.
	 * 
	 * @param processState
	 *            {@link ProcessState} to be cleaned up.
	 * @param jobNodeLoop
	 *            {@link JobNode}.
	 */
	public CleanupSequenceImpl(ProcessState processState, JobNodeLoop jobNodeLoop) {
		this.processState = processState;
		this.jobNodeLoop = jobNodeLoop;
	}

	/*
	 * ==================== CleanupSequence ============================
	 */

	@Override
	public JobNode registerCleanUpJob(JobNode cleanupJob) {
		return new CleanupOperation() {
			@Override
			public JobNode doJob() {
				// Easy access to sequence
				CleanupSequenceImpl sequence = CleanupSequenceImpl.this;

				// Queue job for when current jobs complete
				if (sequence.head == null) {
					// No jobs queued, so add to head
					sequence.head = cleanupJob;
					sequence.tail = cleanupJob;
				} else {
					// Queue at end of list
					sequence.tail.setNext(cleanupJob);
					sequence.tail = cleanupJob;
				}
			}
		};
	}

	@Override
	public synchronized CleanupEscalation[] getCleanupEscalations() {

		// Obtain the clean up escalations
		CleanupEscalation[] escalations = this.cachedCleanupEscalations;
		if (escalations == null) {
			escalations = (this.cleanupEscalations == null ? new CleanupEscalation[0]
					: this.cleanupEscalations.toArray(new CleanupEscalation[this.cleanupEscalations.size()]));

			// Cache to avoid recreation
			this.cachedCleanupEscalations = escalations;
		}

		// Return the escalations
		return escalations;
	}

	@Override
	public synchronized void registerCleanupEscalation(Class<?> objectType, Throwable escalation) {

		// Ensure have cleanup escalation list
		if (this.cleanupEscalations == null) {
			this.cleanupEscalations = new LinkedList<CleanupEscalation>();
		}

		// Add the cleanup escalation
		this.cleanupEscalations.add(new CleanupEscalationImpl(objectType, escalation));

		// Clear caching of cleanup escalations
		this.cachedCleanupEscalations = null;
	}

	/*
	 * ================ ProcessCompletionListener ======================
	 */

	@Override
	public void processComplete() {

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

	/**
	 * Clean up operation.
	 */
	private abstract class CleanupOperation implements JobNode {

		@Override
		public ThreadState getThreadState() {
			return CleanupSequenceImpl.this.processState.getMainThreadState();
		}
	}

	/**
	 * {@link CleanupEscalation} implementation.
	 */
	private static class CleanupEscalationImpl implements CleanupEscalation {

		/**
		 * Object type of the {@link ManagedObject}.
		 */
		private final Class<?> objectType;

		/**
		 * {@link Escalation} on cleanup of the {@link ManagedObject}.
		 */
		private final Throwable escalation;

		/**
		 * Initiate.
		 * 
		 * @param objectType
		 *            Object type of the {@link ManagedObject}.
		 * @param escalation
		 *            {@link Escalation} cleanup of the {@link ManagedObject}.
		 */
		public CleanupEscalationImpl(Class<?> objectType, Throwable escalation) {
			this.objectType = objectType;
			this.escalation = escalation;
		}

		/*
		 * ======================== CleanupEscalation ==========================
		 */

		@Override
		public Class<?> getObjectType() {
			return this.objectType;
		}

		@Override
		public Throwable getEscalation() {
			return this.escalation;
		}
	}

}