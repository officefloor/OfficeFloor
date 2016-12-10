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
package net.officefloor.frame.impl.construct.asset;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.execute.asset.AssetManagerImpl;
import net.officefloor.frame.impl.execute.asset.OfficeManagerImpl;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * {@link AssetManagerFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerFactoryImpl implements AssetManagerFactory {

	/**
	 * Registry of the {@link AssetManager} instances by their name.
	 */
	private final Map<String, AssetManager> registry = new HashMap<String, AssetManager>();

	/**
	 * {@link OfficeManagerImpl} to register the created {@link AssetManager}
	 * instances.
	 */
	private final OfficeManager officeManager;

	/**
	 * Initiate.
	 * 
	 * @param officeManager
	 *            {@link OfficeManager} to register the created
	 *            {@link AssetManager} instances.
	 */
	public AssetManagerFactoryImpl(OfficeManager officeManager) {
		this.officeManager = officeManager;
	}

	/*
	 * ================= AssetManagerFactory =================================
	 */

	@Override
	public AssetManager createAssetManager(AssetType assetType,
			String assetName, String responsibility, OfficeFloorIssues issues) {

		// Create the name of the asset manager
		String assetManagerName = assetType.toString() + ":" + assetName + "["
				+ responsibility + "]";

		// Determine if manager already available for responsibility over asset
		if (this.registry.get(assetManagerName) != null) {
			issues.addIssue(assetType, assetName, AssetManager.class
					.getSimpleName()
					+ " already responsible for '" + responsibility + "'");
			return null; // can not carry on
		}

		// Create the asset manager
		AssetManager assetManager = new AssetManagerImpl(this.officeManager);

		// Register the asset manager
		this.registry.put(assetManagerName, assetManager);

		// Register the asset manager with the office manager
		this.officeManager.registerAssetManager(assetManager);

		// Return the asset manager
		return assetManager;
	}

}