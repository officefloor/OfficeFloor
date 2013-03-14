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
package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.impl.execute.job.JobNodeActivatableSetImpl;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.ComparatorLinkedListSet;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.LinkedListSet;

/**
 * Implementation of the {@link AssetMonitor}.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetMonitorImpl extends
		AbstractLinkedListSetEntry<AssetMonitor, AssetManager> implements
		AssetMonitor {

	/**
	 * {@link Asset} being monitored.
	 */
	private final Asset asset;

	/**
	 * {@link AssetManager} to managed this {@link AssetMonitor}.
	 */
	private final AssetManager assetManager;

	/**
	 * Flag indicating to permanently activate waiting {@link JobNode}
	 * instances.
	 */
	private boolean isPermanentlyActivate = false;

	/**
	 * Permanent failure of the {@link Asset}.
	 */
	private Throwable failure = null;

	/**
	 * Set of {@link JobNode} instances waiting on the {@link Asset}.
	 */
	private final LinkedListSet<MonitoredJobNode, AssetMonitor> jobNodes = new ComparatorLinkedListSet<MonitoredJobNode, AssetMonitor>() {
		@Override
		protected AssetMonitor getOwner() {
			return AssetMonitorImpl.this;
		}

		@Override
		protected boolean isEqual(MonitoredJobNode entryA,
				MonitoredJobNode entryB) {
			return (entryA.jobNode == entryB.jobNode);
		}
	};

	/**
	 * Initiate.
	 * 
	 * @param asset
	 *            {@link Asset} to be managed.
	 * @param assetManager
	 *            {@link AssetManager} for managing this.
	 */
	public AssetMonitorImpl(Asset asset, AssetManager assetManager) {
		this.asset = asset;
		this.assetManager = assetManager;
	}

	/*
	 * ======================= LinkedListSetEntry ==============================
	 */

	@Override
	public AssetManager getLinkedListSetOwner() {
		return this.assetManager;
	}

	/*
	 * ================= AssetMonitor ==========================================
	 */

	@Override
	public Asset getAsset() {
		return this.asset;
	}

	@Override
	public boolean waitOnAsset(JobNode jobNode, JobNodeActivateSet activateSet) {

		// Determine if to wait on the asset
		JobNode activateJobNode = null;
		Throwable activateFailure = null;
		synchronized (this.jobNodes) {

			// Determine action based on state
			if (this.isPermanentlyActivate) {
				// Permanently activating, therefore activate immediately
				activateJobNode = jobNode;
				activateFailure = this.failure;

			} else {
				// Determine if first Job
				if (this.jobNodes.getHead() == null) {
					// Require monitoring, therefore register for management
					this.assetManager.registerAssetMonitor(this);
				}

				// Add the monitored job (ensures job only added once)
				this.jobNodes.addEntry(new MonitoredJobNode(jobNode));
			}
		}

		// Determine if activate immediately
		if (activateJobNode == null) {
			// No job node to activate, therefore waiting
			return true;
		}

		// Have job to activate (as permanent activate)
		if (activateFailure == null) {
			activateSet.addJobNode(activateJobNode);
		} else {
			activateSet.addJobNode(activateJobNode, activateFailure);
		}
		return false; // not waiting as activated
	}

	@Override
	public void activateJobNodes(JobNodeActivateSet activateSet,
			boolean isPermanent) {
		this.activate(activateSet, null, isPermanent);
	}

	@Override
	public void failJobNodes(JobNodeActivateSet activateSet, Throwable failure,
			boolean isPermanent) {
		this.activate(activateSet, failure, isPermanent);
	}

	/**
	 * Purges the list of {@link MonitoredJobNode} instances, adding the
	 * {@link JobNode} instances to the {@link JobNodeActivateSet} with the
	 * possible {@link Throwable} failure.
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param failure
	 *            Possible {@link Throwable} failure. May be <code>null</code>.
	 * @param isPermanent
	 *            Flags whether to set into permanently activate state.
	 */
	private void activate(JobNodeActivateSet activateSet, Throwable failure,
			boolean isPermanent) {

		// Determine if require OfficeManager to activate jobs
		JobNodeActivatableSet activatableSet = null;
		if (activateSet == null) {
			// Require OfficeManager
			activatableSet = new JobNodeActivatableSetImpl();
			activateSet = activatableSet;
		}

		// Obtain the jobs to be notified
		MonitoredJobNode monitoredJobNode;
		synchronized (this.jobNodes) {

			// Purge the list of jobs
			monitoredJobNode = this.jobNodes.purgeEntries();

			// Unregister from management if have jobs (as was managed)
			if (monitoredJobNode != null) {
				this.assetManager.unregisterAssetMonitor(this);
			}

			// Flag permanently activated (and possible failure).
			// Can not reset once permanently activated.
			if (isPermanent) {
				this.isPermanentlyActivate = true;
				if (failure != null) {
					this.failure = failure;
				}
			}
		}

		// Determine if jobs to activate
		boolean isJobsToActivate = (monitoredJobNode != null);

		// Add the job nodes for activation
		while (monitoredJobNode != null) {
			if (failure == null) {
				activateSet.addJobNode(monitoredJobNode.jobNode);
			} else {
				activateSet.addJobNode(monitoredJobNode.jobNode, failure);
			}
			monitoredJobNode = monitoredJobNode.getNext();
		}

		// Use OfficeManager to activate if no activate set provided
		if ((activatableSet != null) && (isJobsToActivate)) {
			this.assetManager.getOfficeManager().activateJobNodes(
					activatableSet);
		}
	}

	/**
	 * {@link JobNode} being monitored by the {@link AssetMonitor}.
	 */
	private class MonitoredJobNode extends
			AbstractLinkedListSetEntry<MonitoredJobNode, AssetMonitor> {

		/**
		 * {@link JobNode} being monitored.
		 */
		public final JobNode jobNode;

		/**
		 * Initiate.
		 * 
		 * @param jobNode
		 *            {@link JobNode} being monitored.
		 * @param linkedList
		 *            {@link LinkedListSet}.
		 */
		public MonitoredJobNode(JobNode jobNode) {
			this.jobNode = jobNode;
		}

		@Override
		public AssetMonitor getLinkedListSetOwner() {
			return AssetMonitorImpl.this;
		}
	}

}