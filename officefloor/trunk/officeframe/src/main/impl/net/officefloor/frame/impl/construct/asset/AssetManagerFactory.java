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

import net.officefloor.frame.api.OfficeFloorIssues;
import net.officefloor.frame.api.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;

/**
 * Factory for the creation of an {@link AssetManager}.
 * 
 * @author Daniel
 */
public interface AssetManagerFactory {

	/**
	 * Creates the {@link AssetManager}.
	 * 
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of the {@link Asset}.
	 * @param responsibility
	 *            Responsibility of the {@link AssetManager} for the
	 *            {@link Asset}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link AssetManager} or <code>null</code> if {@link AssetManager}
	 *         already created for the {@link Asset} with
	 *         {@link OfficeFloorIssues} informed.
	 */
	AssetManager createAssetManager(AssetType assetType, String assetName,
			String responsibility, OfficeFloorIssues issues);

}
