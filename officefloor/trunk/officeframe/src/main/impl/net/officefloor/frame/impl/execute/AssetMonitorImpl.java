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
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.spi.team.Job;

/**
 * Implementation of the {@link AssetMonitor}.
 * 
 * @author Daniel
 */
public class AssetMonitorImpl extends
		AbstractLinkedListEntry<AssetMonitor, Object> implements AssetMonitor {

	/**
	 * {@link Asset} being monitored.
	 */
	protected final Asset asset;

	/**
	 * Lock for synchronising this {@link Asset}.
	 */
	protected final Object assetLock;

	/**
	 * {@link AssetManager} for managing this.
	 */
	protected final AssetManager assetManager;

	/**
	 * Flag indicating to permanently notify waiting {@link Job}
	 * instances.
	 */
	protected boolean isPermanentlyNotify = false;

	/**
	 * Permanently failure of this {@link AssetMonitor}.
	 */
	protected Throwable failure = null;

	/**
	 * List of {@link Job} instances waiting on the {@link Asset}.
	 */
	protected final LinkedList<MonitoredTask, Object> tasks = new AbstractLinkedList<MonitoredTask, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			// Unregister from the Asset Group
			AssetMonitorImpl.this.assetManager
					.unregisterAssetMonitor(AssetMonitorImpl.this);
		}
	};

	/**
	 * Initiate.
	 * 
	 * @param asset
	 *            {@link Asset} to be managed.
	 * @param assetLock
	 *            Lock for synchronising the {@link Asset}.
	 * @param assetManager
	 *            {@link AssetManager} for managing this.
	 * @param assetMonitors
	 *            {@link LinkedList} of the {@link AssetMonitor} instances for
	 *            the {@link AssetManager}.
	 */
	public AssetMonitorImpl(Asset asset, Object assetLock,
			AssetManager assetManager,
			LinkedList<AssetMonitor, Object> taskMonitors) {
		super(taskMonitors);
		this.asset = asset;
		this.assetLock = assetLock;
		this.assetManager = assetManager;
	}

	/*
	 * =================================================================================
	 * AssetMonitor
	 * =================================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMonitor#getAsset()
	 */
	public Asset getAsset() {
		return this.asset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMonitor#getAssetLock()
	 */
	public Object getAssetLock() {
		return this.assetLock;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetMonitor#wait(net.officefloor.frame.spi.team.TaskContainer,
	 *      net.officefloor.frame.internal.structure.AssetNotifySet)
	 */
	@Override
	public boolean wait(Job taskContainer, JobActivateSet notifySet) {
		// Create the monitored item for the task container
		MonitoredTask monitoredTask = new MonitoredTask(taskContainer,
				this.tasks);

		// Only allow one wait at a time
		Job wakeupTask = null;
		Throwable wakeupFailure = null;
		synchronized (this.assetLock) {

			// Determine action based on state
			if (this.isPermanentlyNotify) {
				// Permanently notifying, therefore wake up immediately
				wakeupTask = taskContainer;
				wakeupFailure = this.failure;

			} else {
				// Determine if first Task
				if (this.tasks.getHead() == null) {
					// Require monitoring, therefore register for monitoring
					this.assetManager.registerAssetMonitor(this);
				}

				// Add the monitored task
				this.tasks.addLinkedListEntry(monitoredTask);
			}
		}

		// Determine if wake up immediately
		if (wakeupTask == null) {
			// No task to wake up, therefore waiting
			return true;
		}

		// Have task to wake up (as permanent wake up)
		if (wakeupFailure == null) {
			notifySet.addNotifiedJob(wakeupTask);
		} else {
			notifySet.addFailedJob(wakeupTask, wakeupFailure);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetMonitor#notifyTasks(net.officefloor.frame.internal.structure.AssetNotifySet)
	 */
	@Override
	public void notifyTasks(JobActivateSet notifySet) {
		this.notify(notifySet, false, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetMonitor#notifyPermanently(net.officefloor.frame.internal.structure.AssetNotifySet)
	 */
	@Override
	public void notifyPermanently(JobActivateSet notifySet) {
		this.notify(notifySet, true, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetMonitor#failTasks(net.officefloor.frame.internal.structure.AssetNotifySet,
	 *      java.lang.Throwable)
	 */
	@Override
	public void failTasks(JobActivateSet notifySet, Throwable failure) {
		this.notify(notifySet, false, failure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetMonitor#failPermanently(net.officefloor.frame.internal.structure.AssetNotifySet,
	 *      java.lang.Throwable)
	 */
	@Override
	public void failPermanently(JobActivateSet notifySet, Throwable failure) {
		this.notify(notifySet, true, failure);
	}

	/**
	 * Purges the list of {@link MonitoredTask} instances, adding the
	 * {@link Job} instances to the {@link JobActivateSet} with the
	 * possible {@link Throwable} failure.
	 * 
	 * @param notifySet
	 *            {@link JobActivateSet}.
	 * @param isPermanentlyNotify
	 *            Flags whether to set into permanently notify state.
	 * @param failure
	 *            Possible {@link Throwable} failure. May be <code>null</code>.
	 */
	private void notify(JobActivateSet notifySet, boolean isPermanentlyNotify,
			Throwable failure) {

		// Obtain the tasks to be notified
		MonitoredTask task;
		synchronized (this.assetLock) {
			// Purge the list of tasks
			task = this.tasks.purgeLinkedList(null);

			// Flag permanently notify (and possible failure)
			if (isPermanentlyNotify) {
				this.isPermanentlyNotify = true;
				this.failure = failure;
			}
		}

		// Add the tasks for notifying
		while (task != null) {
			if (failure == null) {
				notifySet.addNotifiedJob(task.taskContainer);
			} else {
				notifySet.addFailedJob(task.taskContainer, failure);
			}
			task = task.getNext();
		}
	}

	/**
	 * {@link Job} being monitored by the {@link AssetMonitor}.
	 */
	private class MonitoredTask extends
			AbstractLinkedListEntry<MonitoredTask, Object> {

		/**
		 * {@link Job} being monitored.
		 */
		protected final Job taskContainer;

		/**
		 * Initiate.
		 * 
		 * @param linkedList
		 */
		public MonitoredTask(Job taskContainer,
				LinkedList<MonitoredTask, Object> linkedList) {
			super(linkedList);
			this.taskContainer = taskContainer;
		}
	}

}