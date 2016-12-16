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

/**
 * <p>
 * {@link AssetManager} to manage {@link Asset} instances.
 * <p>
 * May be used as a {@link LinkedListSetEntry} in a list of {@link AssetManager}
 * instances for a {@link OfficeManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AssetManager extends LinkedListSetEntry<AssetManager, OfficeManager> {

	/**
	 * Obtains the {@link OfficeManager} managing this {@link AssetManager}.
	 * 
	 * @return {@link OfficeManager} managing this {@link AssetManager}.
	 */
	OfficeManager getOfficeManager();

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
	 */
	void checkOnAssets();

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