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
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AssetManagerFactoryImpl}.
 * 
 * @author Daniel
 */
public class AssetManagerFactoryTest extends OfficeFrameTestCase {

	/**
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory factory = new AssetManagerFactoryImpl();

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * Ensures able to create an {@link AssetManager}.
	 */
	public void testCreateAssetManager() {

		// No recording
		this.replayMockObjects();

		// Create the Asset Manager
		AssetManager assetManager = this.factory.createAssetManager(
				AssetType.MANAGED_OBJECT, "connection", "timeout", this.issues);
		assertNotNull("Must create asset manager", assetManager);

		// Verify mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensure not able to create {@link AssetManager} for same {@link Asset}.
	 */
	public void testDuplicateAssetManager() {

		final AssetType assetType = AssetType.MANAGED_OBJECT;
		final String assetName = "connection";
		final String responsibility = "timeout";

		// Record reporting an issue
		this.issues.addIssue(assetType, assetName,
				"AssetManager already responsible for 'timeout'");

		// Attempt to create the Asset Manager twice
		this.replayMockObjects();
		AssetManager assetManager = this.factory.createAssetManager(assetType,
				assetName, responsibility, this.issues);
		assertNotNull("Should create asset manager on first attempt",
				assetManager);
		assetManager = this.factory.createAssetManager(assetType, assetName,
				responsibility, this.issues);
		assertNull("Should not create asset manager for same asset",
				assetManager);
		this.verifyMockObjects();
	}
}
