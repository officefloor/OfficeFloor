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
package net.officefloor.autowire;

import javax.management.ObjectName;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.Work;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link AutoWireManagementMBean} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireOfficeFloor {

	/**
	 * Obtains the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	OfficeFloor getOfficeFloor();

	/**
	 * Obtains the {@link ObjectName} for this {@link AutoWireOfficeFloor}.
	 * 
	 * @return {@link ObjectName} for this {@link AutoWireOfficeFloor}.
	 */
	ObjectName getObjectName();

	/**
	 * <p>
	 * Invokes a {@link ManagedFunction} on the {@link OfficeFloor}.
	 * <p>
	 * Should the {@link OfficeFloor} not be open, it is opened before invoking
	 * the {@link ManagedFunction}. Please note however the {@link OfficeFloor} will not be
	 * re-opened after being closed.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link ManagedFunction}.
	 * @param parameter
	 *            Parameter for the {@link ManagedFunction}. May be <code>null</code>.
	 * @throws Exception
	 *             If fails invoking the {@link ManagedFunction}.
	 */
	void invokeTask(String workName, String taskName, Object parameter)
			throws Exception;

	/**
	 * Closes the {@link OfficeFloor}.
	 */
	void closeOfficeFloor();

}