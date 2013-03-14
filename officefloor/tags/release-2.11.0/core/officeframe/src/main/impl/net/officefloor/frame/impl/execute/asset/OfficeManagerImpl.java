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

import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.job.JobNodeActivatableSetImpl;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.LinkedListSetEntry;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Implementation of the {@link OfficeManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeManagerImpl implements OfficeManager {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(OfficeManagerImpl.class.getName());

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
	private final LinkedListSet<AssetManager, OfficeManager> assetManagers = new StrictLinkedListSet<AssetManager, OfficeManager>() {
		@Override
		protected OfficeManager getOwner() {
			return OfficeManagerImpl.this;
		}
	};

	/**
	 * Listing of {@link JobNodeActivatableSet} instances to be activated by the
	 * {@link ManageOfficeThread}.
	 */
	private final LinkedListSet<ActivateJobNodes, OfficeManager> activateJobNodeSets = new StrictLinkedListSet<ActivateJobNodes, OfficeManager>() {
		@Override
		protected OfficeManager getOwner() {
			return OfficeManagerImpl.this;
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
		this.assetManagers.addEntry(assetManager);
	}

	@Override
	public synchronized void startManaging() {

		// Ensure not already managing office
		if (this.manageOfficeThread != null) {
			throw new IllegalStateException("Office already being managed");
		}

		// Note: once the managing office thread is set no further AssetManagers
		// can be added. As both methods are synchronised, the listing of
		// AssetManagers is safe to use read only by the managing office thread.

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

		// Add the jobs to activate
		this.activateJobNodeSets.addEntry(new ActivateJobNodes(activatableSet));

		// Notify the office manager thread to active jobs
		this.notify();
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
	 * {@link TeamIdentifier} for the {@link ManageOfficeThread}.
	 */
	public static final TeamIdentifier MANAGE_OFFICE_TEAM = new TeamIdentifier() {
	};

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

				// Wait remaining time or until need to activate jobs
				ActivateJobNodes activateJobNodes;
				synchronized (OfficeManagerImpl.this) {

					// Before waiting, ensure no jobs require being activated.
					// Jobs may have been added while checking on assets.
					activateJobNodes = OfficeManagerImpl.this.activateJobNodeSets
							.purgeEntries();
					if (activateJobNodes == null) {

						// No jobs to activate, so wait
						try {
							OfficeManagerImpl.this.wait(remainingTime);
						} catch (InterruptedException ex) {
							return; // stop managing office
						}

						// Obtain jobs to activate (while in lock)
						activateJobNodes = OfficeManagerImpl.this.activateJobNodeSets
								.purgeEntries();
					}
				}

				// First thing, activate jobs to progress office processing
				while (activateJobNodes != null) {
					activateJobNodes.activatableSet
							.activateJobNodes(MANAGE_OFFICE_TEAM);
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

				// Check the assets of each asset manager
				AssetManager assetManager = OfficeManagerImpl.this.assetManagers
						.getHead();
				while (assetManager != null) {
					try {
						assetManager.checkOnAssets(activatableSet);
					} catch (Throwable ex) {
						// Warn of failure
						if (LOGGER.isLoggable(Level.WARNING)) {
							LOGGER.log(Level.WARNING, "Failed managing asset: "
									+ ex.getMessage() + " ["
									+ ex.getClass().getName() + "]", ex);
						}
					}
					assetManager = assetManager.getNext();
				}

			} finally {
				// Ensure the job nodes are activated
				activatableSet.activateJobNodes(MANAGE_OFFICE_TEAM);
			}
		}
	}

	/**
	 * {@link LinkedListSetEntry} containing the {@link JobNodeActivatableSet}
	 * to activate.
	 */
	private class ActivateJobNodes extends
			AbstractLinkedListSetEntry<ActivateJobNodes, OfficeManager> {

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
			this.activatableSet = activatableSet;
		}

		@Override
		public OfficeManager getLinkedListSetOwner() {
			return OfficeManagerImpl.this;
		}
	}

}