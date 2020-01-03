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
	OFFICE_FLOOR, OFFICE, TEAM, EXECUTIVE, MANAGED_OBJECT, MANAGED_OBJECT_POOL, GOVERNANCE, ADMINISTRATOR, PROCESS,
	THREAD, FUNCTION
	}

	/**
	 * Adds an issue about an asset of the {@link OfficeFloor}.
	 * 
	 * @param assetType        {@link AssetType}.
	 * @param assetName        Name of the asset.
	 * @param issueDescription Description of the issue.
	 */
	void addIssue(AssetType assetType, String assetName, String issueDescription);

	/**
	 * Adds an issue about an asset of the {@link OfficeFloor}.
	 * 
	 * @param asset            {@link AssetType}.
	 * @param assetName        Name of the asset.
	 * @param issueDescription Description of the issue.
	 * @param cause            Cause of the issue.
	 */
	void addIssue(AssetType asset, String assetName, String issueDescription, Throwable cause);

}