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
package net.officefloor.compile.issues;

import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.Model;

/**
 * Notified of issues in compilation of the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public interface CompilerIssues {

	/**
	 * Type of location where issues may arise.
	 */
	public static enum LocationType {
		DESK, SECTION, OFFICE, OFFICE_FLOOR
	}

	/**
	 * Adds an issue about an asset at a particular location.
	 * 
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Path to {@link Model} file containing the issue.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of asset.
	 * @param issueDescription
	 *            Description of the issue.
	 */
	void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription);

	/**
	 * Adds an issue about an asset at a particular location.
	 * 
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Path to {@link Model} file containing the issue.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of asset.
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription,
			Throwable cause);

}