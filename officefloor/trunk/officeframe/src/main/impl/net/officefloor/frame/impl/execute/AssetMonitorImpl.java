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
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.TaskContainer;

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.structure.AssetMonitor}.
 * 
 * @author Daniel
 */
public class AssetMonitorImpl extends AbstractLinkedListEntry<AssetMonitor>
		implements AssetMonitor {

	/**
	 * {@link Asset} being managed.
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
	 * State of this {@link AssetMonitor}.
	 */
	protected AssetMonitorState state = AssetMonitorState.WAITING;

	/**
	 * Possible failure of {@link Asset}.
	 */
	protected Throwable failure = null;

	/**
	 * List of {@link TaskContainer} instances waiting on the {@link Asset}.
	 */
	protected final LinkedList<MonitoredTask> tasks = new AbstractLinkedList<MonitoredTask>() {
		public void lastLinkedListEntryRemoved() {
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
			AssetManager assetManager, LinkedList<AssetMonitor> taskMonitors) {
		super(taskMonitors);
		this.asset = asset;
		this.assetLock = assetLock;
		this.assetManager = assetManager;
	}

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
	 * @see net.officefloor.frame.internal.structure.TaskMonitor#wait(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public boolean wait(TaskContainer taskContainer) {

		// Create the monitored item for the task container
		MonitoredTask monitoredTask = new MonitoredTask(taskContainer,
				this.tasks);

		// Only allow one wait at a time
		TaskContainer wakeupTask = null;
		Throwable wakeupFailure = null;
		synchronized (this) {

			// Determine action based on state
			switch (this.state) {
			case WAITING:
				// Determine if first Task
				if (this.tasks.getHead() == null) {
					// Require monitoring, therefore register for monitoring
					this.assetManager.registerAssetMonitor(this);
				}

				// Add the monitored task
				this.tasks.addLinkedListEntry(monitoredTask);
				break;

			default:
				// No longer waiting, therefore wake up immediately
				wakeupTask = taskContainer;
				wakeupFailure = this.failure;
			}
		}

		// Determine if wake up immediately
		if (wakeupTask != null) {
			ThreadState wakeupThread = wakeupTask.getThreadState();
			synchronized (wakeupThread.getThreadLock()) {
				// Specify failure if failure
				if (wakeupFailure != null) {
					wakeupThread.setFailure(wakeupFailure);
				}

				// Activate task immediately
				wakeupTask.activateTask();
			}
		}

		// Return whether waiting
		return (wakeupTask == null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMonitor#notifyTasks()
	 */
	public void notifyTasks() {

		// Head of the list of tasks to notify
		MonitoredTask task;

		synchronized (this) {
			// Purge the list of tasks
			task = this.tasks.purgeLinkedList();

			// Flag woken
			this.state = AssetMonitorState.WOKEN;
		}

		// Iterate over the tasks activating them
		while (task != null) {
			task.taskContainer.activateTask();
			task = task.getNext();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMonitor#failTasks(java.lang.Throwable)
	 */
	public void failTasks(Throwable failure) {

		// Head of the list of tasks to notify
		MonitoredTask task;

		synchronized (this) {
			// Purge the list of tasks
			task = this.tasks.purgeLinkedList();

			// Flag woken but failed
			this.state = AssetMonitorState.WOKEN;
			this.failure = failure;
		}

		// Iterate over the tasks failing them
		while (task != null) {
			// Lock on the task's thread to specify failure
			TaskContainer taskContainer = task.taskContainer;
			ThreadState threadState = taskContainer.getThreadState();
			synchronized (threadState.getThreadLock()) {
				// Fail the task
				threadState.setFailure(failure);

				// Active the task to handle failure
				taskContainer.activateTask();
			}

			// Next task
			task = task.getNext();
		}
	}

	/**
	 * States of this {@link AssetMonitor}.
	 */
	private enum AssetMonitorState {

		/**
		 * Initial state of waiting.
		 */
		WAITING,

		/**
		 * Indicates that has been woken.
		 */
		WOKEN
	}
}

/**
 * {@link net.officefloor.frame.spi.team.TaskContainer} being monitored by the
 * {@link net.officefloor.frame.internal.structure.AssetMonitor}.
 * 
 * @author Daniel
 */
class MonitoredTask extends AbstractLinkedListEntry<MonitoredTask> {

	/**
	 * {@link TaskContainer} being monitored.
	 */
	protected final TaskContainer taskContainer;

	/**
	 * Initiate.
	 * 
	 * @param linkedList
	 */
	public MonitoredTask(TaskContainer taskContainer,
			LinkedList<MonitoredTask> linkedList) {
		super(linkedList);
		this.taskContainer = taskContainer;
	}

}