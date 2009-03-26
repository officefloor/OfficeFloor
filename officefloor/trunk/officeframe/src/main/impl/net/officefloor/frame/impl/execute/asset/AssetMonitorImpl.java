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
import net.officefloor.frame.impl.execute.linkedlist.AbstractLinkedListEntry;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.LinkedList;

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
	 * List of {@link JobNode} instances waiting on the {@link Asset}.
	 */
	private final LinkedList<MonitoredJobNode, Object> jobNodes = new AbstractLinkedList<MonitoredJobNode, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			// Unregister from the Asset Manager
			AssetMonitorImpl.this.assetManager
					.unregisterAssetMonitor(AssetMonitorImpl.this);
		}
	};

	/**
	 * Initiate.
	 * 
	 * @param asset
	 *            {@link Asset} to be managed.
	 * @param assetManager
	 *            {@link AssetManager} for managing this.
	 * @param assetMonitors
	 *            {@link LinkedList} of the {@link AssetMonitor} instances for
	 *            the {@link AssetManager}.
	 */
	public AssetMonitorImpl(Asset asset, AssetManager assetManager,
			LinkedList<AssetMonitor, Object> assetMonitors) {
		super(assetMonitors);
		this.asset = asset;
		this.assetManager = assetManager;
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
				// Determine if first Task
				if (this.jobNodes.getHead() == null) {
					// Require monitoring, therefore register for monitoring
					this.assetManager.registerAssetMonitor(this);
				}

				// TODO test not re-add a JobNode (ie only have it added once)
				boolean isAlreadyAdded = false;
				MonitoredJobNode entry = this.jobNodes.getHead();
				CHECK_ALREADY_ADDED: while (entry != null) {
					if (entry.jobNode == jobNode) {
						System.out.println("Already added");
						isAlreadyAdded = true;
						break CHECK_ALREADY_ADDED;
					}
					entry = entry.getNext();
				}

				// Add the monitored job (it not already added)
				if (!isAlreadyAdded) {
					this.jobNodes.addLinkedListEntry(new MonitoredJobNode(
							jobNode, this.jobNodes));
				}
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

		// Obtain the jobs to be notified
		MonitoredJobNode monitoredJobNode;
		synchronized (this.jobNodes) {
			// Purge the list of tasks
			monitoredJobNode = this.jobNodes.purgeLinkedList(null);

			// Flag permanently activated (and possible failure).
			// Can not reset once permanently activated.
			if (isPermanent) {
				this.isPermanentlyActivate = true;
				// TODO once permanent failure of Asset can not clear failure
				this.failure = failure;
			}
		}

		// Add the job nodes for activation
		while (monitoredJobNode != null) {
			if (failure == null) {
				activateSet.addJobNode(monitoredJobNode.jobNode);
			} else {
				activateSet.addJobNode(monitoredJobNode.jobNode, failure);
			}
			monitoredJobNode = monitoredJobNode.getNext();
		}
	}

	/**
	 * {@link JobNode} being monitored by the {@link AssetMonitor}.
	 */
	private class MonitoredJobNode extends
			AbstractLinkedListEntry<MonitoredJobNode, Object> {

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
		 *            {@link LinkedList}.
		 */
		public MonitoredJobNode(JobNode jobNode,
				LinkedList<MonitoredJobNode, Object> linkedList) {
			super(linkedList);
			this.jobNode = jobNode;
		}
	}

}