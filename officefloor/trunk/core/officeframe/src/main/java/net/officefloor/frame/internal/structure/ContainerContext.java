/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.team.Job;

/**
 * Context for the {@link WorkContainer} and {@link ManagedObjectContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ContainerContext {

	/**
	 * Flags for the {@link Job} to wait and be activated at a later time once a
	 * dependency is ready.
	 */
	void flagJobToWait();

	/**
	 * Adds a setup {@link Job} to be executed before the current {@link Job}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} for the setup {@link Job}.
	 * @param parameter
	 *            Parameter for the {@link Job}.
	 */
	void addSetupJob(FlowMetaData<?> flowMetaData, Object parameter);

}