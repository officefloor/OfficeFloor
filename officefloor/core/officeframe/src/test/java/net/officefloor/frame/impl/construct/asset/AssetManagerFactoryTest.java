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
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.OfficeClock;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AssetManagerFactoryImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerFactoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState = this.createMock(ProcessState.class);

	/**
	 * {@link OfficeClock}.
	 */
	private final OfficeClock clock = this.createMock(OfficeClock.class);

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop = this.createMock(FunctionLoop.class);

	/**
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactoryImpl factory = new AssetManagerFactoryImpl(this.processState, this.clock,
			this.functionLoop);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensures able to create an {@link AssetManager}.
	 */
	public void testCreateAssetManager() {

		// Create the Asset Manager
		this.replayMockObjects();
		AssetManager assetManager = this.factory.createAssetManager(AssetType.MANAGED_OBJECT, "connection", "timeout",
				this.issues);
		assertNotNull("Must create asset manager", assetManager);
		this.verifyMockObjects();

		// Ensure returned asset manager is also registered
		assertEquals("Incorrect registered asset manager", assetManager, this.factory.getAssetManagers()[0]);
	}

	/**
	 * Ensure not able to create {@link AssetManager} for same {@link Asset}.
	 */
	public void testDuplicateAssetManager() {

		final AssetType assetType = AssetType.MANAGED_OBJECT;
		final String assetName = "connection";
		final String responsibility = "timeout";

		// Record only creating the first of the duplicates
		this.issues.addIssue(assetType, assetName, "AssetManager already responsible for 'timeout'");

		// Attempt to create the Asset Manager twice
		this.replayMockObjects();
		AssetManager assetManagerOne = this.factory.createAssetManager(assetType, assetName, responsibility,
				this.issues);
		assertNotNull("Should create asset manager on first attempt", assetManagerOne);
		AssetManager assetManagerTwo = this.factory.createAssetManager(assetType, assetName, responsibility,
				this.issues);
		assertNull("Should not create asset manager for same asset", assetManagerTwo);
		this.verifyMockObjects();

		// Ensure registered asset manager is the first
		AssetManager[] registered = this.factory.getAssetManagers();
		assertEquals("Incorrect number of registered asset managers", 1, registered.length);
		assertEquals("Incorrect registered asset manager", assetManagerOne, registered[0]);
	}

}