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
