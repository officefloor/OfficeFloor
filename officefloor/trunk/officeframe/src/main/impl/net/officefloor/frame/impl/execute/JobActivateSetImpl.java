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
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.spi.team.Job;

/**
 * Implementation of {@link JobActivateSet}.
 * 
 * @author Daniel
 */
public class JobActivateSetImpl implements JobActivateSet {

	/**
	 * {@link Job} instances to be activated.
	 */
	protected final LinkedList<NotifiedTask, Object> tasks = new AbstractLinkedList<NotifiedTask, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			// Do nothing, as list should never be emptied
		}
	};

	/**
	 * Activates the {@link Job} instances within this
	 * {@link JobActivateSet}.
	 */
	public void activateJobs() {

		// Iterate over the tasks activating them
		NotifiedTask task = this.tasks.getHead();
		while (task != null) {

			// Synchronise on thread of task to ensure safe activation
			synchronized (task.task.getThreadState().getThreadLock()) {

				// Flag the failure (if one)
				if (task.failure != null) {
					task.task.getThreadState().setFailure(task.failure);
				}

				// Activate the task
				task.task.activateJob();
			}

			// Move to next task for activating
			task = task.getNext();
		}
	}

	/*
	 * =============================================================================
	 * AssetNotifySet
	 * =============================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetNotifySet#addNotifiedTask(net.officefloor.frame.spi.team.TaskContainer)
	 */
	@Override
	public void addNotifiedJob(Job notifiedTask) {
		this.tasks.addLinkedListEntry(new NotifiedTask(this.tasks,
				notifiedTask, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetNotifySet#addFailedTask(net.officefloor.frame.spi.team.TaskContainer,
	 *      java.lang.Throwable)
	 */
	@Override
	public void addFailedJob(Job notifiedTask, Throwable failure) {
		this.tasks.addLinkedListEntry(new NotifiedTask(this.tasks,
				notifiedTask, failure));
	}

	/**
	 * {@link Job} to be activated.
	 */
	protected class NotifiedTask extends
			AbstractLinkedListEntry<NotifiedTask, Object> {

		/**
		 * {@link Job} to be activated.
		 */
		public final Job task;

		/**
		 * Potential failure of {@link Asset} for notifying the
		 * {@link Job}.
		 */
		public final Throwable failure;

		/**
		 * Initiate.
		 * 
		 * @param linkedList
		 *            {@link LinkedList}.
		 */
		public NotifiedTask(LinkedList<NotifiedTask, Object> linkedList,
				Job task, Throwable failure) {
			super(linkedList);
			this.task = task;
			this.failure = failure;
		}
	}

}
