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
package net.officefloor.frame.impl.construct.asset;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.OfficeFloorIssues;
import net.officefloor.frame.api.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.execute.asset.AssetManagerImpl;
import net.officefloor.frame.internal.structure.AssetManager;

/**
 * {@link AssetManagerFactory} implementation.
 * 
 * @author Daniel
 */
public class AssetManagerFactoryImpl implements AssetManagerFactory {

	/**
	 * Registry of the {@link AssetManager} instances by their name.
	 */
	private final Map<String, AssetManager> registry = new HashMap<String, AssetManager>();

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.impl.construct.asset.AssetManagerFactory#
	 * createAssetManager(net.officefloor.frame.api.OfficeFloorIssues.AssetType,
	 * java.lang.String, java.lang.String,
	 * net.officefloor.frame.api.OfficeFloorIssues)
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
		AssetManager assetManager = new AssetManagerImpl();

		// Register the asset manager
		this.registry.put(assetManagerName, assetManager);

		// Return the asset manager
		return assetManager;
	}

}
