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

import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Implementation of {@link JobNodeActivateSet}.
 * 
 * @author Daniel Sagenschneider
 */
public class JobNodeActivatableSetImpl implements JobNodeActivatableSet {

	/**
	 * {@link JobNode} instances to be activated.
	 */
	private final LinkedListSet<ActivatedJobNode, JobNodeActivatableSet> jobNodes = new StrictLinkedListSet<ActivatedJobNode, JobNodeActivatableSet>() {
		@Override
		protected JobNodeActivatableSet getOwner() {
			return JobNodeActivatableSetImpl.this;
		}
	};

	/*
	 * ========================= JobNodeActivatableSet =======================
	 */

	@Override
	public void addJobNode(JobNode jobNode) {
		this.jobNodes.addEntry(new ActivatedJobNode(jobNode, null));
	}

	@Override
	public void addJobNode(JobNode jobNode, Throwable failure) {
		this.jobNodes.addEntry(new ActivatedJobNode(jobNode, failure));
	}

	@Override
	public void activateJobNodes(TeamIdentifier currentTeam) {

		// Iterate over the jobs activating them
		ActivatedJobNode notifiedJobNode = this.jobNodes.getHead();
		while (notifiedJobNode != null) {

			// Flag the failure (if one)
			if (notifiedJobNode.failure != null) {
				// Synchronise on thread of job to ensure specifying failure
				ThreadState threadState = notifiedJobNode.jobNode
						.getJobSequence().getThreadState();
				synchronized (threadState.getThreadLock()) {
					threadState.setFailure(notifiedJobNode.failure);
				}
			}

			// Activate the job (should be outside thread lock)
			notifiedJobNode.jobNode.activateJob(currentTeam);

			// Move to next job for activating
			notifiedJobNode = notifiedJobNode.getNext();
		}
	}

	/**
	 * {@link JobNode} to be activated.
	 */
	private class ActivatedJobNode extends
			AbstractLinkedListSetEntry<ActivatedJobNode, JobNodeActivatableSet> {

		/**
		 * {@link JobNode} to be activated.
		 */
		public final JobNode jobNode;

		/**
		 * Potential failure of {@link Asset} for notifying the {@link JobNode}.
		 */
		public final Throwable failure;

		/**
		 * Initiate.
		 * 
		 * @param jobNode
		 *            {@link JobNode} to be notified.
		 * @param failure
		 *            Potential failure.
		 */
		public ActivatedJobNode(JobNode jobNode, Throwable failure) {
			this.jobNode = jobNode;
			this.failure = failure;
		}

		@Override
		public JobNodeActivatableSet getLinkedListSetOwner() {
			return JobNodeActivatableSetImpl.this;
		}
	}

}