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
import net.officefloor.frame.internal.structure.AssetReport;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.LinkedListItem;

/**
 * Implementation of the {@link AssetManager}.
 * 
 * @author Daniel
 */
public class AssetManagerImpl implements AssetManager, AssetReport {

	/**
	 * Indicates no current time has been specified.
	 */
	protected static final long NO_TIME = 0;

	/**
	 * {@link LinkedList} of {@link AssetMonitor} instances requiring
	 * monitoring.
	 */
	protected final LinkedList<AssetMonitor, Object> monitors = new AbstractLinkedList<AssetMonitor, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			// No action required
		}
	};

	/**
	 * Time for the {@link #getTime()} method.
	 */
	protected long time = 0;

	/**
	 * Failure of an {@link Asset}.
	 */
	protected Throwable failure = null;

	/*
	 * ====================================================================
	 * AssetManager
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetGroup#createTaskMonitor(java.lang.Object,
	 *      net.officefloor.frame.internal.structure.Asset)
	 */
	public AssetMonitor createAssetMonitor(Asset asset, Object lock) {
		return new AssetMonitorImpl(asset, lock, this, this.monitors);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetManager#manageAssets()
	 */
	public void manageAssets() {

		// Access Point: Project Manager
		// Locks: None

		// Obtain the list of monitors to manage
		LinkedListItem<AssetMonitor> item;
		synchronized (this.monitors) {
			item = this.monitors.copyLinkedList();
		}

		// Reset the Asset Report time
		this.time = NO_TIME;

		// Iterate over the monitors managing them
		JobActivatableSetImpl notifySet = new JobActivatableSetImpl();
		while (item != null) {

			// Reset the Asset Report
			this.failure = null;

			// Obtain the Monitor
			AssetMonitor monitor = item.getEntry();

			// Obtain the Asset
			Asset asset = monitor.getAsset();

			// Lock the Asset from changes
			try {
				synchronized (monitor.getAssetLock()) {
					// Report on the Asset
					asset.reportOnAsset(this);
				}
			} catch (Throwable ex) {
				// Fail tasks based on failure
				this.failure = ex;
			}

			// Determine if failure of asset
			if (this.failure != null) {
				// Fail the Asset Monitor
				// (manages own locks and unregisters from this)
				monitor.failTasks(notifySet, this.failure);
			}

			// TODO: provide asset group reporting

			// Next iteration
			item = item.getNext();
		}

		// Notify the failed tasks
		notifySet.activateJobs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetGroup#registerTaskMonitor(net.officefloor.frame.internal.structure.TaskMonitor)
	 */
	public void registerAssetMonitor(AssetMonitor monitor) {
		// Co-ordinate with managing
		synchronized (this.monitors) {
			this.monitors.addLinkedListEntry(monitor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetGroup#unregisterTaskMonitor(net.officefloor.frame.internal.structure.TaskMonitor)
	 */
	public void unregisterAssetMonitor(AssetMonitor monitor) {
		// Co-ordinate with managing
		synchronized (this.monitors) {
			monitor.removeFromLinkedList(null);
		}
	}

	/*
	 * ====================================================================
	 * AssetReport
	 * 
	 * No synchronising necessary as will be invoked by the same thread invoking
	 * the managed AssetGroup method.
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetReport#getTime()
	 */
	public long getTime() {
		// Lazy obtain current time
		if (this.time == NO_TIME) {
			this.time = System.currentTimeMillis();
		}

		// Return the current time
		return this.time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AssetReport#setFailure(java.lang.Throwable)
	 */
	public void setFailure(Throwable failure) {
		this.failure = failure;
	}

}
