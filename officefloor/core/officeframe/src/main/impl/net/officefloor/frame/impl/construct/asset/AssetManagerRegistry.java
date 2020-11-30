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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.execute.asset.AssetManagerHirerImpl;
import net.officefloor.frame.impl.execute.asset.AssetManagerReferenceImpl;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetManagerHirer;
import net.officefloor.frame.internal.structure.AssetManagerReference;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.MonitorClock;

/**
 * Registry of the {@link AssetManager} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerRegistry {

	/**
	 * Registered {@link AssetManager} names.
	 */
	private final Set<String> registeredAssetManagerNames = new HashSet<>();

	/**
	 * Listing of {@link AssetManagerHirer} instances in the order they are
	 * registered. This enables {@link AssetManagerReference} to appropriately
	 * reference the {@link AssetManagerHirer}.
	 */
	private final List<AssetManagerHirer> assetManagerHirers = new LinkedList<>();

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock monitorClock;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * Flag to ensure no further {@link AssetManager} instances are created once the
	 * {@link AssetManager} array is created.
	 */
	private boolean isAssetManagerListCreated = false;

	/**
	 * Instantiate.
	 * 
	 * @param monitorClock {@link MonitorClock}.
	 * @param functionLoop {@link FunctionLoop}.
	 */
	public AssetManagerRegistry(MonitorClock monitorClock, FunctionLoop functionLoop) {
		this.monitorClock = monitorClock;
		this.functionLoop = functionLoop;
	}

	/**
	 * Obtain all the registered {@link AssetManagerHirer} instances.
	 * 
	 * @return Registered {@link AssetManagerHirer} instances.
	 */
	public AssetManagerHirer[] getAssetManagerHirers() {
		this.isAssetManagerListCreated = true;
		return this.assetManagerHirers.toArray(new AssetManagerHirer[this.assetManagerHirers.size()]);
	}

	/**
	 * Creates the {@link AssetManager}.
	 * 
	 * @param assetType      {@link AssetType}.
	 * @param assetName      Name of the {@link Asset}.
	 * @param responsibility Responsibility of the {@link AssetManager} for the
	 *                       {@link Asset}.
	 * @param issues         {@link OfficeFloorIssues}.
	 * @return {@link AssetManagerReference} or <code>null</code> if
	 *         {@link AssetManager} already created for the {@link Asset} with
	 *         {@link OfficeFloorIssues} informed.
	 */
	public AssetManagerReference createAssetManager(AssetType assetType, String assetName, String responsibility,
			OfficeFloorIssues issues) {

		// Create the name of the asset manager
		String assetManagerName = assetType.toString() + ":" + assetName + "[" + responsibility + "]";

		// Ensure will be included in the list
		if (this.isAssetManagerListCreated) {
			throw new IllegalStateException("Can not create " + AssetManagerHirer.class.getSimpleName() + " "
					+ assetManagerName + " as list already generated");
		}

		// Determine if manager already available for responsibility over asset
		if (this.registeredAssetManagerNames.contains(assetManagerName)) {
			issues.addIssue(assetType, assetName,
					AssetManagerHirer.class.getSimpleName() + " already responsible for '" + responsibility + "'");
			return null; // can not carry on
		}

		// Create the asset manager hirer
		AssetManagerHirer assetManagerHirer = new AssetManagerHirerImpl(this.monitorClock, this.functionLoop);

		// Register the asset manager
		int referenceIndex = this.assetManagerHirers.size();
		this.assetManagerHirers.add(assetManagerHirer);
		this.registeredAssetManagerNames.add(assetManagerName);

		// Return the asset manager reference
		return new AssetManagerReferenceImpl(referenceIndex);
	}

}
