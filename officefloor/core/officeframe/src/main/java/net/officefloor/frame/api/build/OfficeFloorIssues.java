/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Notified of issues in construction of the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorIssues {

	/**
	 * Listing of the asset types within the {@link OfficeFloor} that may have
	 * issues in construction.
	 */
	public static enum AssetType {
		OFFICE_FLOOR, OFFICE, TEAM, MANAGED_OBJECT, MANAGED_OBJECT_POOL, GOVERNANCE, ADMINISTRATOR, DUTY, PROCESS, THREAD, FUNCTION
	}

	/**
	 * Adds an issue about an asset of the {@link OfficeFloor}.
	 * 
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of the asset.
	 * @param issueDescription
	 *            Description of the issue.
	 */
	void addIssue(AssetType assetType, String assetName, String issueDescription);

	/**
	 * Adds an issue about an asset of the {@link OfficeFloor}.
	 * 
	 * @param asset
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of the asset.
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	void addIssue(AssetType asset, String assetName, String issueDescription, Throwable cause);

}