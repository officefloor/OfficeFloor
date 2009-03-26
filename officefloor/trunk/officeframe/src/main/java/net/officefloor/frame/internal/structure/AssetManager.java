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
package net.officefloor.frame.internal.structure;

/**
 * {@link AssetManager} to manage {@link Asset} instances.
 * 
 * @author Daniel
 */
public interface AssetManager {

	/**
	 * <p>
	 * Creates a new {@link AssetMonitor}.
	 * <p>
	 * The returned {@link AssetMonitor} is not being managed by this
	 * {@link AssetManager}. To have the {@link AssetMonitor} managed, it must
	 * be registered with this {@link AssetManager}. This allows for only the
	 * list of {@link AssetMonitor} instances requiring management to be
	 * managed.
	 * 
	 * @param asset
	 *            {@link Asset} that {@link JobNode} instances will wait on.
	 * @return {@link AssetMonitor} for the {@link Asset}.
	 */
	AssetMonitor createAssetMonitor(Asset asset);

	/**
	 * Does a single check on the {@link Asset} instances of the registered
	 * {@link AssetMonitor} instances for management.
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add all {@link JobNode}
	 *            instances requiring activation from the check. A
	 *            {@link AssetManager} should not activate {@link JobNode}
	 *            instances itself.
	 */
	void checkOnAssets(JobNodeActivateSet activateSet);

	/**
	 * Registers a {@link AssetMonitor} to be managed by this
	 * {@link AssetManager}.
	 * 
	 * @param monitor
	 *            {@link AssetMonitor} to be managed by this
	 *            {@link AssetManager}.
	 */
	void registerAssetMonitor(AssetMonitor monitor);

	/**
	 * Unregisters a {@link AssetMonitor} from being managed by this
	 * {@link AssetManager}.
	 * 
	 * @param monitor
	 *            {@link AssetMonitor} no longer requiring managing.
	 */
	void unregisterAssetMonitor(AssetMonitor monitor);

}