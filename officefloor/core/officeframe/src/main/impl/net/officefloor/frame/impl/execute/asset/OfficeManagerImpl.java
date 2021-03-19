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

package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetManagerReference;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * Implementation of the {@link OfficeManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeManagerImpl implements OfficeManager {

	/**
	 * {@link MonitorClockImpl}.
	 */
	private final MonitorClockImpl monitorClock;

	/**
	 * Interval to monitor the {@link Asset} instances.
	 */
	private final long monitorInterval;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * {@link AssetManager} instances of the {@link Office}.
	 */
	private final AssetManager[] assetManagers;

	/**
	 * Initiate.
	 * 
	 * @param monitorClock    {@link MonitorClock} for the {@link Office}.
	 * @param monitorInterval Interval to monitor the {@link Asset} instances.
	 * @param functionLoop    {@link FunctionLoop} for the {@link Office}.
	 * @param assetManagers   {@link AssetManager} instances.
	 */
	public OfficeManagerImpl(MonitorClockImpl monitorClock, long monitorInterval, FunctionLoop functionLoop,
			AssetManager[] assetManagers) {
		this.monitorClock = monitorClock;
		this.monitorInterval = monitorInterval;
		this.functionLoop = functionLoop;
		this.assetManagers = assetManagers;
	}

	/*
	 * ================= OfficeManager =======================================
	 */

	@Override
	public AssetManager getAssetManager(AssetManagerReference assetManagerReference) {
		return this.assetManagers[assetManagerReference.getAssetManagerIndex()];
	}

	@Override
	public long getMonitorInterval() {
		return this.monitorInterval;
	}

	@Override
	public void runAssetChecks() {

		// Update the approximate time for the office
		if (this.monitorClock != null) {
			this.monitorClock.updateTime();
		}

		// Trigger the monitoring of the office
		for (int i = 0; i < this.assetManagers.length; i++) {
			AssetManager assetManager = this.assetManagers[i];
			this.functionLoop.delegateFunction(assetManager);
		}
	}

}
