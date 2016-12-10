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
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;

/**
 * Factory for the creation of an {@link AssetManager}.
 * 
 * @author Daniel Sagenschneider
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
