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
package net.officefloor.frame.impl.execute.job;

import net.officefloor.frame.impl.execute.linkedlist.AbstractLinkedList;
import net.officefloor.frame.impl.execute.linkedlist.AbstractLinkedListEntry;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of {@link JobNodeActivateSet}.
 * 
 * @author Daniel
 */
public class JobNodeActivatableSetImpl implements JobNodeActivatableSet {

	/**
	 * {@link JobNode} instances to be activated.
	 */
	protected final LinkedList<ActivatedJobNode, Object> jobNodes = new AbstractLinkedList<ActivatedJobNode, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			// Do nothing, as list should never be emptied
		}
	};

	/*
	 * ========================= JobActivatableSet ============================
	 */

	@Override
	public void addJobNode(JobNode jobNode) {
		this.jobNodes.addLinkedListEntry(new ActivatedJobNode(this.jobNodes,
				jobNode, null));
	}

	@Override
	public void addJobNode(JobNode jobNode, Throwable failure) {
		this.jobNodes.addLinkedListEntry(new ActivatedJobNode(this.jobNodes,
				jobNode, failure));
	}

	@Override
	public void activateJobNodes() {

		// Iterate over the jobs activating them
		ActivatedJobNode notifiedJobNode = this.jobNodes.getHead();
		while (notifiedJobNode != null) {

			// Synchronise on thread of job to ensure safe activation
			ThreadState threadState = notifiedJobNode.jobNode.getFlow()
					.getThreadState();
			synchronized (threadState.getThreadLock()) {

				// Flag the failure (if one)
				if (notifiedJobNode.failure != null) {
					threadState.setFailure(notifiedJobNode.failure);
				}

				// Activate the job
				notifiedJobNode.jobNode.activateJob();
			}

			// Move to next job for activating
			notifiedJobNode = notifiedJobNode.getNext();
		}
	}

	/**
	 * {@link JobNode} to be activated.
	 */
	private class ActivatedJobNode extends
			AbstractLinkedListEntry<ActivatedJobNode, Object> {

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
		 * @param linkedList
		 *            {@link LinkedList}.
		 * @param jobNode
		 *            {@link JobNode} to be notified.
		 * @param failure
		 *            Potential failure.
		 */
		public ActivatedJobNode(
				LinkedList<ActivatedJobNode, Object> linkedList,
				JobNode jobNode, Throwable failure) {
			super(linkedList);
			this.jobNode = jobNode;
			this.failure = failure;
		}
	}

}