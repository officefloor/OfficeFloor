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