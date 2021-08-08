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

import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetManagerHirer;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link AssetManagerHirer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerHirerImpl implements AssetManagerHirer {

	/**
	 * {@link MonitorClock}.
	 */
	private final MonitorClock clock;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop loop;

	/**
	 * Instantiate.
	 * 
	 * @param clock {@link MonitorClock}.
	 * @param loop  {@link FunctionLoop}.
	 */
	public AssetManagerHirerImpl(MonitorClock clock, FunctionLoop loop) {
		this.clock = clock;
		this.loop = loop;
	}

	/*
	 * ===================== AssetManagerHirer ========================
	 */

	@Override
	public AssetManager hireAssetManager(ProcessState managingProcess) {
		return new AssetManagerImpl(managingProcess, this.clock, this.loop);
	}

}
