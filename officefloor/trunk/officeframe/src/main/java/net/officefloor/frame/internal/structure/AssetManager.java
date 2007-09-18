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
 * {@link AssetManager} to manage
 * {@link net.officefloor.frame.internal.structure.Asset} instances.
 * 
 * @author Daniel
 */
public interface AssetManager {

	/**
	 * Creates a new {@link AssetMonitor}
	 * 
	 * @param lock
	 *            Lock for synchronising the {@link Asset}.
	 * @param asset
	 *            {@link Asset} that
	 *            {@link net.officefloor.frame.spi.team.TaskContainer} instances
	 *            will wait on.
	 * @return {@link AssetMonitor} for the {@link Asset}.
	 */
	AssetMonitor createAssetMonitor(Asset asset, Object assetLock);

	/**
	 * Does a single pass management of the {@link AssetManager}.
	 */
	void manageAssets();

	/**
	 * Registers a {@link AssetMonitor} within this {@link AssetManager}.
	 * 
	 * @param monitor
	 *            {@link AssetMonitor} to be monitored within this
	 *            {@link AssetManager}.
	 */
	void registerAssetMonitor(AssetMonitor monitor);

	/**
	 * Unregisters a {@link AssetMonitor} from this {@link AssetManager}.
	 * 
	 * @param monitor
	 *            {@link AssetMonitor} no longer requiring monitoring within
	 *            this {@link AssetManager}.
	 */
	void unregisterAssetMonitor(AssetMonitor monitor);

}
