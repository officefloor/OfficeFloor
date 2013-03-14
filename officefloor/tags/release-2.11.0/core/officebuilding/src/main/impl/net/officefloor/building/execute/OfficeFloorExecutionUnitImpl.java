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
package net.officefloor.building.execute;

import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ProcessConfiguration;

/**
 * {@link OfficeFloorExecutionUnit} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExecutionUnitImpl implements OfficeFloorExecutionUnit {

	/**
	 * {@link ManagedProcess}.
	 */
	private final ManagedProcess managedProcess;

	/**
	 * {@link ProcessConfiguration}.
	 */
	private final ProcessConfiguration processConfiguration;

	/**
	 * Flags to spawn a {@link Process}.
	 */
	private final boolean isSpawnProcess;

	/**
	 * Initiate.
	 * 
	 * @param managedProcess
	 *            {@link ManagedProcess}.
	 * @param processConfiguration
	 *            {@link ProcessConfiguration}.
	 * @param isSpawnProcess
	 *            Flags whether to spawn a {@link Process}.
	 */
	public OfficeFloorExecutionUnitImpl(ManagedProcess managedProcess,
			ProcessConfiguration processConfiguration, boolean isSpawnProcess) {
		this.managedProcess = managedProcess;
		this.processConfiguration = processConfiguration;
		this.isSpawnProcess = isSpawnProcess;
	}

	/*
	 * ===================== OfficeFloorExecutionUnit ===================
	 */

	@Override
	public ManagedProcess getManagedProcess() {
		return this.managedProcess;
	}

	@Override
	public ProcessConfiguration getProcessConfiguration() {
		return this.processConfiguration;
	}

	@Override
	public boolean isSpawnProcess() {
		return this.isSpawnProcess;
	}

}