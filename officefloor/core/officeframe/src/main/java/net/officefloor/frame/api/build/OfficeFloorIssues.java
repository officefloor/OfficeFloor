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
