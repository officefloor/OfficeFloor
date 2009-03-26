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
package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.impl.execute.linkedlist.AbstractLinkedList;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.LinkedListItem;

/**
 * Implementation of the {@link AssetManager}.
 * 
 * @author Daniel
 */
public class AssetManagerImpl implements AssetManager, CheckAssetContext {

	/**
	 * Indicates no current time has been specified.
	 */
	private static final long NO_TIME = 0;

	/**
	 * {@link LinkedList} of {@link AssetMonitor} instances requiring managing.
	 */
	private final LinkedList<AssetMonitor, Object> monitors = new AbstractLinkedList<AssetMonitor, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			// No action required
		}
	};

	/**
	 * Time for the {@link CheckAssetContext}.
	 */
	private long time = NO_TIME;

	/**
	 * {@link JobNodeActivateSet} to use to check the current
	 * {@link AssetMonitor} instances.
	 */
	private JobNodeActivateSet activateSet = null;

	/**
	 * {@link AssetMonitor} currently being checked.
	 */
	private AssetMonitor assetMonitor = null;

	/*
	 * ================ AssetManager ======================================
	 */

	@Override
	public AssetMonitor createAssetMonitor(Asset asset) {
		return new AssetMonitorImpl(asset, this, this.monitors);
	}

	@Override
	public void checkOnAssets(JobNodeActivateSet activateSet) {

		// Access Point: Office Manager
		// Locks: None

		// Obtain the list of monitors to check on
		LinkedListItem<AssetMonitor> item;
		synchronized (this.monitors) {
			item = this.monitors.copyLinkedList();
		}

		try {
			// Set up for checking on the assets
			this.time = NO_TIME;
			this.activateSet = activateSet;

			// Iterate over the monitors managing them
			while (item != null) {

				// Specify the monitor about to be checked
				this.assetMonitor = item.getEntry();

				try {
					// Check on the Asset for the monitor
					this.assetMonitor.getAsset().checkOnAsset(this);

				} catch (Throwable ex) {
					// Fail jobs based on check failure
					this.assetMonitor.failJobNodes(activateSet, ex, false);
				}

				// Next monitor
				item = item.getNext();
			}
		} finally {
			// Ensure release references for current check
			this.activateSet = null;
			this.assetMonitor = null;
		}
	}

	@Override
	public void registerAssetMonitor(AssetMonitor monitor) {
		synchronized (this.monitors) {
			this.monitors.addLinkedListEntry(monitor);
		}
	}

	@Override
	public void unregisterAssetMonitor(AssetMonitor monitor) {
		synchronized (this.monitors) {
			monitor.removeFromLinkedList(null);
		}
	}

	/*
	 * ================== CheckAssetContext ==================================
	 * 
	 * No synchronising necessary as will be invoked by the same thread invoking
	 * the checkOnAsset method.
	 */

	@Override
	public long getTime() {

		// Ensure have time
		if (this.time == NO_TIME) {
			this.time = System.currentTimeMillis();
		}

		// Return the time
		return this.time;
	}

	@Override
	public void activateJobNodes(boolean isPermanent) {
		this.assetMonitor.activateJobNodes(this.activateSet, isPermanent);
	}

	@Override
	public void failJobNodes(Throwable failure, boolean isPermanent) {
		this.assetMonitor.failJobNodes(this.activateSet, failure, isPermanent);
	}

}