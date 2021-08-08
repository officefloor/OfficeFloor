/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
