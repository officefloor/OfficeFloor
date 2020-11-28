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

package net.officefloor.frame.impl.construct.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.execute.asset.OfficeManagerHirerImpl;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetManagerHirer;
import net.officefloor.frame.internal.structure.AssetManagerReference;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Tests the {@link AssetManagerRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class AssetManagerRegistryTest {

	/**
	 * {@link MockTestSupport}.
	 */
	public final MockTestSupport mocks = new MockTestSupport();

	/**
	 * {@link ProcessState}.
	 */
	private ProcessState processState;

	/**
	 * {@link MonitorClock}.
	 */
	private MonitorClock clock;

	/**
	 * {@link FunctionLoop}.
	 */
	private FunctionLoop functionLoop;

	/**
	 * {@link AssetManagerRegistry}.
	 */
	private AssetManagerRegistry registry;

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private OfficeFloorIssues issues;

	@BeforeEach
	public void setup() {
		this.processState = this.mocks.createMock(ProcessState.class);
		this.clock = this.mocks.createMock(MonitorClock.class);
		this.functionLoop = this.mocks.createMock(FunctionLoop.class);
		this.registry = new AssetManagerRegistry(this.clock, this.functionLoop);
		this.issues = this.mocks.createMock(OfficeFloorIssues.class);
	}

	/**
	 * Ensures able to create an {@link AssetManager}.
	 */
	@Test
	public void createAssetManager() {

		// Create the Asset Manager
		this.mocks.replayMockObjects();
		AssetManagerReference assetManagerReference = this.registry.createAssetManager(AssetType.MANAGED_OBJECT,
				"connection", "timeout", this.issues);
		assertNotNull(assetManagerReference, "Must create asset manager reference");
		this.mocks.verifyMockObjects();

		// Ensure reference asset manager
		assertEquals(0, assetManagerReference.getAssetManagerIndex(), "Should reference only asset manager");

		// Ensure returned asset manager is also registered
		AssetManager[] assetManagers = OfficeManagerHirerImpl.hireAssetManagers(this.registry.getAssetManagerHirers(),
				this.processState);
		assertEquals(1, assetManagers.length, "Incorrect number of registered asset managers");
		assertNotNull(assetManagers[0], "Should hire the asset manager");
	}

	/**
	 * Ensure not able to create {@link AssetManager} for same {@link Asset}.
	 */
	@Test
	public void duplicateAssetManager() {

		final AssetType assetType = AssetType.MANAGED_OBJECT;
		final String assetName = "connection";
		final String responsibility = "timeout";

		// Record only creating the first of the duplicates
		this.issues.addIssue(assetType, assetName, "AssetManager already responsible for 'timeout'");

		// Attempt to create the Asset Manager twice
		this.mocks.replayMockObjects();
		AssetManagerReference assetManagerOne = this.registry.createAssetManager(assetType, assetName, responsibility,
				this.issues);
		assertNotNull(assetManagerOne, "Should create asset manager on first attempt");
		AssetManagerReference assetManagerTwo = this.registry.createAssetManager(assetType, assetName, responsibility,
				this.issues);
		assertNull(assetManagerTwo, "Should not create asset manager for same asset");
		this.mocks.verifyMockObjects();

		// Ensure registered asset manager is the first
		AssetManagerHirer[] registered = this.registry.getAssetManagerHirers();
		assertEquals(1, registered.length, "Incorrect number of registered asset managers");
	}

}
