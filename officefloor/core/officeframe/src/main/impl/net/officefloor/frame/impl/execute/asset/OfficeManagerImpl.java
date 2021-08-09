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
