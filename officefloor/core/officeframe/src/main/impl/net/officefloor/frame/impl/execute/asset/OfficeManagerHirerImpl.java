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

import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetManagerHirer;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.OfficeManagerHirer;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link OfficeManagerHirer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeManagerHirerImpl implements OfficeManagerHirer {

	/**
	 * Hires all the {@link AssetManager} instances.
	 * 
	 * @param assetManagerHirers {@link AssetManagerHirer} instances.
	 * @param managingProcess    Managing {@link ProcessState}.
	 * @return {@link AssetManager}.
	 */
	public static AssetManager[] hireAssetManagers(AssetManagerHirer[] assetManagerHirers,
			ProcessState managingProcess) {
		AssetManager[] assetManagers = new AssetManager[assetManagerHirers.length];
		for (int i = 0; i < assetManagers.length; i++) {
			assetManagers[i] = assetManagerHirers[i].hireAssetManager(managingProcess);
		}
		return assetManagers;
	}

	/**
	 * {@link MonitorClock}.
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
	 * {@link AssetManagerHirer} instances.
	 */
	private AssetManagerHirer[] assetManagerHirers;

	/**
	 * Instantiate.
	 * 
	 * @param monitorClock    {@link MonitorClock}.
	 * @param monitorInterval Interval to monitor the {@link Asset} instances.
	 * @param functionLoop    {@link FunctionLoop}.
	 */
	public OfficeManagerHirerImpl(MonitorClockImpl monitorClock, long monitorInterval, FunctionLoop functionLoop) {
		this.monitorClock = monitorClock;
		this.monitorInterval = monitorInterval;
		this.functionLoop = functionLoop;
	}

	/**
	 * Specifies the {@link AssetManagerHirer} instances.
	 * 
	 * @param assetManagerHirers {@link AssetManagerHirer} instances.
	 */
	public void setAssetManagerHirers(AssetManagerHirer[] assetManagerHirers) {
		this.assetManagerHirers = assetManagerHirers;
	}

	/*
	 * ======================= OfficeManagerHirer ==========================
	 */

	@Override
	public OfficeManager hireOfficeManager(ProcessState managingProcess) {

		// Hire the Asset Managers
		AssetManager[] assetManagers = hireAssetManagers(this.assetManagerHirers, managingProcess);

		// Return the hired Office Manager
		return new OfficeManagerImpl(this.monitorClock, this.monitorInterval, this.functionLoop, assetManagers);
	}

}
