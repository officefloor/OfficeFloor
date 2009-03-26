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

import java.util.Timer;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.job.JobNodeActivatableSetImpl;
import net.officefloor.frame.impl.execute.linkedlist.AbstractLinkedList;
import net.officefloor.frame.impl.execute.linkedlist.AbstractLinkedListEntry;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.LinkedListEntry;
import net.officefloor.frame.internal.structure.LinkedListItem;
import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * Implementation of the {@link OfficeManager}.
 * 
 * @author Daniel
 */
public class OfficeManagerImpl implements OfficeManager {

	/**
	 * Name of the {@link Office} being managed.
	 */
	private final String officeName;

	/**
	 * Interval in milliseconds between each check of the {@link Office}.
	 */
	private final long monitorInterval;

	/**
	 * List of {@link AssetManager} instances to be managed.
	 */
	private final LinkedList<OfficeAssetManager, Object> officeAssetManagers = new AbstractLinkedList<OfficeAssetManager, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			// Do nothing
		}
	};

	/**
	 * Listing of {@link JobNodeActivatableSet} instances to be activated by the
	 * {@link ManageOfficeThread}.
	 */
	private final LinkedList<ActivateJobNodes, Object> activateJobNodeSets = new AbstractLinkedList<ActivateJobNodes, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			// Do nothing
		}
	};

	/**
	 * {@link ManageOfficeThread} that does the managing of the {@link Office}.
	 */
	private ManageOfficeThread manageOfficeThread = null;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            Name of the {@link Office} being managed.
	 * @param monitorInterval
	 *            Interval in milliseconds between each check of the
	 *            {@link Office}. Setting this high reduces overhead of managing
	 *            the {@link Office}, however setting lower increases
	 *            responsiveness of the {@link Office}.
	 */
	public OfficeManagerImpl(String officeName, long monitorInterval) {
		this.officeName = officeName;
		this.monitorInterval = monitorInterval;
	}

	/*
	 * ================= OfficeManager =======================================
	 */

	@Override
	public synchronized void registerAssetManager(AssetManager assetManager) {

		// May only add asset manager when not managing the office
		if (this.manageOfficeThread != null) {
			throw new IllegalStateException(
					"Office being managed so can not add another Asset Manager");
		}

		// Add the asset manager to be managed by this office manager
		this.officeAssetManagers.addLinkedListEntry(new OfficeAssetManager(
				assetManager));
	}

	@Override
	public synchronized void startManaging() {

		// Ensure not already managing office
		if (this.manageOfficeThread != null) {
			throw new IllegalStateException("Office already being managed");
		}

		// Create the manage office task and start running
		this.manageOfficeThread = new ManageOfficeThread();
		this.manageOfficeThread.start();
	}

	@Override
	public void checkOnAssets() {

		// Obtain the managed office thread
		ManageOfficeThread thread;
		synchronized (this) {
			if (this.manageOfficeThread == null) {
				throw new IllegalStateException("Office not being managed");
			}
			thread = this.manageOfficeThread;
		}

		// Check on the assets of the office
		thread.checkOnAssets();
	}

	@Override
	public synchronized void activateJobNodes(
			JobNodeActivatableSet activatableSet) {
		this.activateJobNodeSets.addLinkedListEntry(new ActivateJobNodes(
				activatableSet));
	}

	@Override
	public synchronized void stopManaging() {

		// Do nothing if not managing the office
		if (this.manageOfficeThread == null) {
			return;
		}

		// Stop managing the office
		try {
			this.manageOfficeThread.interrupt();
		} finally {
			// Ensure flag no longer managing office
			this.manageOfficeThread = null;
		}
	}

	/**
	 * <p>
	 * Using a dedicated {@link Thread} rather than {@link Timer} to not require
	 * creating possible additional {@link Thread} instances. This ensures only
	 * one {@link Thread} for managing the {@link Office}.
	 * <p>
	 * It also allows to wake up the {@link Thread} to activate {@link JobNode}
	 * instances as necessary.
	 */
	private class ManageOfficeThread extends Thread {

		/**
		 * Initiate.
		 */
		public ManageOfficeThread() {
			super("Manage office " + OfficeManagerImpl.this.officeName);
		}

		@Override
		public void run() {

			// Time for first check of assets
			long nextCheckTime = System.currentTimeMillis()
					+ OfficeManagerImpl.this.monitorInterval;

			// Loop forever
			for (;;) {

				// Calculate the remaining time before next check of assets
				long remainingTime = nextCheckTime - System.currentTimeMillis();

				// Determine if time to check on the assets
				if (remainingTime <= 0) {

					// Check on the assets
					this.checkOnAssets();

					// Set up for next time to check on assets
					remainingTime = OfficeManagerImpl.this.monitorInterval;
					nextCheckTime = System.currentTimeMillis() + remainingTime;
				}

				// Wait remaining time or need to activate jobs
				ActivateJobNodes activateJobNodes;
				synchronized (OfficeManagerImpl.this) {
					try {
						OfficeManagerImpl.this.wait(remainingTime);
					} catch (InterruptedException ex) {
						return; // stop managing office
					}

					// Obtain the list of job nodes to activate (while in lock)
					activateJobNodes = OfficeManagerImpl.this.activateJobNodeSets
							.purgeLinkedList(null);
				}

				// First thing, activate jobs to progress office processing
				while (activateJobNodes != null) {
					activateJobNodes.activatableSet.activateJobNodes();
					activateJobNodes = activateJobNodes.getNext();
				}
			}
		}

		/**
		 * Checks on the {@link Asset} instances of the {@link Office} requiring
		 * management.
		 */
		public void checkOnAssets() {

			// Check on the assets for the asset manager
			JobNodeActivatableSetImpl activatableSet = new JobNodeActivatableSetImpl();
			try {

				// Obtain the list of asset managers to check
				// TODO save memory by not copying Office AssetManager list
				LinkedListItem<OfficeAssetManager> officeAssetManager;
				synchronized (OfficeManagerImpl.this) {
					officeAssetManager = OfficeManagerImpl.this.officeAssetManagers
							.copyLinkedList();
				}

				// Check the assets of each asset manager
				while (officeAssetManager != null) {
					try {
						officeAssetManager.getEntry().assetManager
								.checkOnAssets(activatableSet);
					} catch (Throwable ex) {
						// TODO OfficeFloor EscalationHandler for Asset failures
						System.err.println("Failed managing asset: "
								+ ex.getMessage() + " ["
								+ ex.getClass().getName() + "]");
					}
					officeAssetManager = officeAssetManager.getNext();
				}

			} finally {
				// Ensure the job nodes are activated
				activatableSet.activateJobNodes();
			}
		}
	}

	/**
	 * {@link LinkedListEntry} containing the {@link AssetManager} instances
	 * registered with this {@link OfficeManager}.
	 */
	private class OfficeAssetManager extends
			AbstractLinkedListEntry<OfficeAssetManager, Object> {

		/**
		 * {@link AssetManager} registered with this {@link OfficeManager}.
		 */
		public final AssetManager assetManager;

		/**
		 * Initialise.
		 * 
		 * @param assetManager
		 *            {@link AssetManager} registered with this
		 *            {@link OfficeManager}.
		 */
		public OfficeAssetManager(AssetManager assetManager) {
			super(OfficeManagerImpl.this.officeAssetManagers);
			this.assetManager = assetManager;
		}
	}

	/**
	 * {@link LinkedListEntry} containing the {@link JobNodeActivatableSet} to
	 * activate.
	 */
	private class ActivateJobNodes extends
			AbstractLinkedListEntry<ActivateJobNodes, Object> {

		/**
		 * {@link JobNodeActivatableSet} to activate.
		 */
		public final JobNodeActivatableSet activatableSet;

		/**
		 * Initialise.
		 * 
		 * @param activatableSet
		 *            {@link JobNodeActivatableSet} to activate.
		 */
		public ActivateJobNodes(JobNodeActivatableSet activatableSet) {
			super(OfficeManagerImpl.this.activateJobNodeSets);
			this.activatableSet = activatableSet;
		}
	}

}