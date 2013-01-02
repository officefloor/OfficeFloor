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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.manage.Office;

/**
 * Manages the {@link AssetManager} instances within an {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManager {

	/**
	 * Registers an {@link AssetManager} to be managed by this
	 * {@link OfficeManager}.
	 * 
	 * @param assetManager
	 *            {@link AssetManager} to be managed by this
	 *            {@link OfficeManager}.
	 */
	void registerAssetManager(AssetManager assetManager);

	/**
	 * <p>
	 * Starts this {@link OfficeManager} managing the {@link AssetManager}
	 * instances within the {@link Office}.
	 * <p>
	 * This typically kicks off periodic checking of the {@link Asset} instances
	 * within the {@link Office}.
	 */
	void startManaging();

	/**
	 * <p>
	 * Does a single check on the {@link Asset} instances within the
	 * {@link Office}.
	 * <p>
	 * Provided to allow manual checking of {@link Asset} instances.
	 */
	void checkOnAssets();

	/**
	 * <p>
	 * Activates the {@link JobNode} instances within the input
	 * {@link JobNodeActivatableSet}.
	 * <p>
	 * As the {@link OfficeManager} typically will be running its own
	 * {@link Thread} to check on the {@link Asset} instances, the activation of
	 * the {@link JobNode} instances should be done by that {@link Thread}. This
	 * provides a lock free state to safely activate the {@link JobNode}
	 * instances and not cause dead-lock.
	 * <p>
	 * This should only be called by an {@link AssetMonitor} instance when it
	 * was not provided a {@link JobNodeActivateSet}.
	 * 
	 * @param activatableSet
	 *            {@link JobNodeActivatableSet} to be activated by the
	 *            {@link OfficeManager} {@link Thread}.
	 */
	void activateJobNodes(JobNodeActivatableSet activatableSet);

	/**
	 * Stops this {@link OfficeManager} managing the {@link AssetManager}
	 * instances within the {@link Office}.
	 */
	void stopManaging();

}