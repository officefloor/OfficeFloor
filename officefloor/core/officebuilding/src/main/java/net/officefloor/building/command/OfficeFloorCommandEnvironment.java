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
package net.officefloor.building.command;

import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ProcessConfiguration;

/**
 * Environment for the {@link ManagedProcess}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCommandEnvironment {

	/**
	 * Obtains the environment property value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Property value for the name.
	 */
	String getProperty(String name);

	/**
	 * Specifies the {@link Process} name for the {@link ProcessConfiguration}.
	 * 
	 * @param processName
	 *            {@link Process} name for the {@link ProcessConfiguration}.
	 */
	void setProcessName(String processName);

	/**
	 * <p>
	 * Flags whether to spawn a {@link Process} to run the
	 * {@link OfficeFloorCommand}.
	 * <p>
	 * Default behaviour is to not spawn a {@link Process}.
	 * 
	 * @param isSpawn
	 *            <code>true</code> to spawn a {@link Process}.
	 */
	void setSpawnProcess(boolean isSpawn);

	/**
	 * <p>
	 * Adds a JVM option for the spawned {@link Process} JVM. The value is
	 * provided as is to the JVM and as such care must be taken to use options
	 * available to the underlying specific JVM.
	 * <p>
	 * These values are only utilised if spawning a process - otherwise they are
	 * ignored.
	 * 
	 * @param jvmOption
	 *            JVM option for the spawned {@link Process} JVM.
	 * 
	 * @see #setSpawnProcess(boolean)
	 */
	void addJvmOption(String jvmOption);

}