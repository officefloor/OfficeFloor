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

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * Implementation of the {@link OfficeManager}.
 * 
 * @author Daniel
 */
public class OfficeManagerImpl implements OfficeManager {

	/**
	 * Interval in milliseconds between each check of the {@link Office}.
	 */
	private final long monitorInterval;

	/**
	 * List of {@link AssetManager} instances to be managed.
	 */
	private final List<AssetManager> assetManagers = new LinkedList<AssetManager>();

	/**
	 * {@link Timer} to monitor the {@link Office}.
	 */
	private Timer timer = null;

	/**
	 * {@link ManageOfficeTimerTask} that does the managing of the
	 * {@link Office}.
	 */
	private ManageOfficeTimerTask manageTask = null;

	/**
	 * Initiate.
	 * 
	 * @param monitorInterval
	 *            Interval in milliseconds between each check of the
	 *            {@link Office}. Setting this high reduces overhead of managing
	 *            the {@link Office}, however setting lower increases
	 *            responsiveness of the {@link Office}.
	 */
	public OfficeManagerImpl(long monitorInterval) {
		this.monitorInterval = monitorInterval;
	}

	/**
	 * Adds an {@link AssetManager} for managing the {@link Office}.
	 * 
	 * @param assetManager
	 *            {@link AssetManager}.
	 */
	public synchronized void addAssetManager(AssetManager assetManager) {

		// May only add asset manager when not managing the office
		if (this.timer != null) {
			throw new IllegalStateException(
					"Office being managed so can not add another Asset Manager");
		}

		// Add the asset manager
		this.assetManagers.add(assetManager);
	}

	/**
	 * Obtains the the listing of the added {@link AssetManager} instances.
	 * 
	 * @return Listing of the added {@link AssetManager} instances.
	 */
	public synchronized AssetManager[] getAssetManagers() {
		return this.assetManagers.toArray(new AssetManager[0]);
	}

	/*
	 * ================= OfficeManager =======================================
	 */

	@Override
	public synchronized void startManaging() {

		// Ensure not already managing office
		if (this.timer != null) {
			throw new IllegalStateException("Office already being managed");
		}

		// Create the timer to monitor the office
		this.timer = new Timer(true);

		// Create the manage office task and schedule for running
		this.manageTask = new ManageOfficeTimerTask();
		this.timer.schedule(this.manageTask, this.monitorInterval,
				this.monitorInterval);
	}

	@Override
	public synchronized void manage() {

		// Ensure office being managed
		if (this.timer == null) {
			throw new IllegalStateException("Office not being managed");
		}

		// Manage the office
		this.manageTask.run();
	}

	@Override
	public synchronized void stopManaging() {

		// Do nothing if not managing the office
		if (this.timer == null) {
			return;
		}

		// Stop managing the office
		try {
			this.timer.cancel();
		} finally {
			// Ensure flag no longer managing office
			this.timer = null;
			this.manageTask = null;
		}
	}

	/**
	 * {@link TimerTask} to manage the {@link Office}.
	 */
	private class ManageOfficeTimerTask extends TimerTask {

		@Override
		public void run() {

			// Lock on the office manager to ensure safe
			synchronized (OfficeManagerImpl.this) {

				// Manage the assets
				for (AssetManager assetManager : OfficeManagerImpl.this.assetManagers) {
					try {
						assetManager.manageAssets();
					} catch (Throwable ex) {
						// TODO provide better logging for manageAssets failure
						System.err.println("Failed managing asset: "
								+ ex.getMessage() + " ["
								+ ex.getClass().getName() + "]");
					}
				}
			}
		}
	}

}