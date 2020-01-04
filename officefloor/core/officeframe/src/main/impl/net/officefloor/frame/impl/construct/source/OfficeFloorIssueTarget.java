/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.construct.source;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.source.IssueTarget;

/**
 * {@link IssueTarget} implementation that adapts to the
 * {@link OfficeFloorIssues}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorIssueTarget implements IssueTarget {

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues;

	/**
	 * {@link AssetType}.
	 */
	private final AssetType assetType;

	/**
	 * Name of asset.
	 */
	private final String assetName;

	/**
	 * Instantiate.
	 * 
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of asset.
	 */
	public OfficeFloorIssueTarget(OfficeFloorIssues issues, AssetType assetType, String assetName) {
		this.issues = issues;
		this.assetType = assetType;
		this.assetName = assetName;
	}

	/*
	 * =============== IssueTarget ====================
	 */

	@Override
	public void addIssue(String issueDescription) {
		this.issues.addIssue(this.assetType, this.assetName, issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause) {
		this.issues.addIssue(this.assetType, this.assetName, issueDescription, cause);
	}

}
