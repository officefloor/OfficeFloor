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
package net.officefloor.frame.impl.execute;

import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.JobActivatableSet;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of {@link JobActivateSet}.
 * 
 * @author Daniel
 */
public class JobActivatableSetImpl implements JobActivatableSet {

	/**
	 * {@link JobNode} instances to be activated.
	 */
	protected final LinkedList<NotifiedJobNode, Object> jobNodes = new AbstractLinkedList<NotifiedJobNode, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			// Do nothing, as list should never be emptied
		}
	};

	/*
	 * ========================= JobActivatableSet ============================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.JobActivateSet#addNotifiedJobNode
	 * (net.officefloor.frame.internal.structure.JobNode)
	 */
	@Override
	public void addNotifiedJobNode(JobNode notifiedJobNode) {
		this.jobNodes.addLinkedListEntry(new NotifiedJobNode(this.jobNodes,
				notifiedJobNode, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.JobActivateSet#addFailedJobNode
	 * (net.officefloor.frame.internal.structure.JobNode, java.lang.Throwable)
	 */
	@Override
	public void addFailedJobNode(JobNode notifiedJobNode, Throwable failure) {
		this.jobNodes.addLinkedListEntry(new NotifiedJobNode(this.jobNodes,
				notifiedJobNode, failure));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.JobActivatableSet#activateJobs()
	 */
	@Override
	public void activateJobs() {

		// Iterate over the tasks activating them
		NotifiedJobNode notifiedJobNode = this.jobNodes.getHead();
		while (notifiedJobNode != null) {

			// Synchronise on thread of task to ensure safe activation
			ThreadState threadState = notifiedJobNode.jobNode.getThreadState();
			synchronized (threadState.getThreadLock()) {

				// Flag the failure (if one)
				if (notifiedJobNode.failure != null) {
					threadState.setFailure(notifiedJobNode.failure);
				}

				// Activate the job
				notifiedJobNode.jobNode.activateJob();
			}

			// Move to next task for activating
			notifiedJobNode = notifiedJobNode.getNext();
		}
	}

	/**
	 * {@link JobNode} to be activated.
	 */
	protected class NotifiedJobNode extends
			AbstractLinkedListEntry<NotifiedJobNode, Object> {

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
		public NotifiedJobNode(LinkedList<NotifiedJobNode, Object> linkedList,
				JobNode jobNode, Throwable failure) {
			super(linkedList);
			this.jobNode = jobNode;
			this.failure = failure;
		}
	}

}
