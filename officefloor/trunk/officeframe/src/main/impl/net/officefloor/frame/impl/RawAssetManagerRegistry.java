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
package net.officefloor.frame.impl;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.impl.execute.asset.AssetManagerImpl;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.structure.AssetManager;

/**
 * Registry of the raw
 * {@link net.officefloor.frame.internal.structure.AssetManager} instances.
 * 
 * @author Daniel
 */
@Deprecated
public class RawAssetManagerRegistry {

	/**
	 * Registry of the {@link AssetManager} instances.
	 */
	protected final Map<String, AssetManager> assetManagers = new HashMap<String, AssetManager>();

	/**
	 * Creates and registers an {@link AssetManager} by the input name.
	 * 
	 * @param name
	 *            Name of the {@link AssetManager}.
	 * @return {@link AssetManager} created and registered under the input name.
	 * @throws ConfigurationException
	 *             If {@link AssetManager} already registered under the name.
	 */
	public AssetManager createAssetManager(String name)
			throws ConfigurationException {
		// Determine if already registered
		if (this.assetManagers.get(name) != null) {
			// Asset Manager already registered
			throw new ConfigurationException("AssetManager '" + name
					+ "' is already registered.");
		}

		// Not registered therefore create and register
		AssetManager assetManager = new AssetManagerImpl();
		this.assetManagers.put(name, assetManager);

		// Return the Asset Manager
		return assetManager;
	}
}
