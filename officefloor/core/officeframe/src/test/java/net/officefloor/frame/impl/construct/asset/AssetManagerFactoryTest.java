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
package net.officefloor.frame.impl.construct.asset;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link AssetManagerFactoryImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerFactoryTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeManager}.
	 */
	private final OfficeManager officeManager = this
			.createMock(OfficeManager.class);

	/**
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory factory = new AssetManagerFactoryImpl(
			this.officeManager);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * Ensures able to create an {@link AssetManager}.
	 */
	public void testCreateAssetManager() {

		// Record registering the asset manager
		final AssetManager[] registeredAssetManager = new AssetManager[1];
		this.officeManager.registerAssetManager(null);
		this.control(this.officeManager).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				registeredAssetManager[0] = (AssetManager) actual[0];
				return true;
			}
		});

		// Create the Asset Manager
		this.replayMockObjects();
		AssetManager assetManager = this.factory.createAssetManager(
				AssetType.MANAGED_OBJECT, "connection", "timeout", this.issues);
		assertNotNull("Must create asset manager", assetManager);
		this.verifyMockObjects();

		// Ensure returned asset manager is also registered
		assertEquals("Incorrect registered asset manager", assetManager,
				registeredAssetManager[0]);
	}

	/**
	 * Ensure not able to create {@link AssetManager} for same {@link Asset}.
	 */
	public void testDuplicateAssetManager() {

		final AssetType assetType = AssetType.MANAGED_OBJECT;
		final String assetName = "connection";
		final String responsibility = "timeout";

		// Record only creating the first of the duplicates
		final AssetManager[] registeredAssetManager = new AssetManager[1];
		this.officeManager.registerAssetManager(null);
		this.control(this.officeManager).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertNull("Should only register first",
						registeredAssetManager[0]);
				registeredAssetManager[0] = (AssetManager) actual[0];
				return true;
			}
		});
		this.issues.addIssue(assetType, assetName,
				"AssetManager already responsible for 'timeout'");

		// Attempt to create the Asset Manager twice
		this.replayMockObjects();
		AssetManager assetManagerOne = this.factory.createAssetManager(
				assetType, assetName, responsibility, this.issues);
		assertNotNull("Should create asset manager on first attempt",
				assetManagerOne);
		AssetManager assetManagerTwo = this.factory.createAssetManager(
				assetType, assetName, responsibility, this.issues);
		assertNull("Should not create asset manager for same asset",
				assetManagerTwo);
		this.verifyMockObjects();

		// Ensure registered asset manager is the first
		assertEquals("Incorrect registered asset manager", assetManagerOne,
				registeredAssetManager[0]);
	}

}