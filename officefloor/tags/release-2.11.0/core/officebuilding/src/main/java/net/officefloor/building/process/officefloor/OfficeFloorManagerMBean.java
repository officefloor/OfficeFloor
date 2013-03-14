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
package net.officefloor.building.process.officefloor;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * MBean interface for the {@link OfficeFloorManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorManagerMBean {

	/**
	 * Obtains the configuration location of the running {@link OfficeFloor}.
	 * 
	 * @return Configuration location of the running {@link OfficeFloor}.
	 */
	String getOfficeFloorLocation();

	/**
	 * Obtains the listing of the {@link Task} instances within the
	 * {@link OfficeFloor}.
	 * 
	 * @return Listing of the {@link Task} instances within the
	 *         {@link OfficeFloor}.
	 */
	String listTasks();

	/**
	 * Invokes {@link Work} within the {@link OfficeFloor}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office} containing the {@link Work}.
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}. May be <code>null</code> to invoke
	 *            the initial {@link Task} for the {@link Work}.
	 * @param parameter
	 *            Parameter for the initial {@link Task}.
	 * @throws Exception
	 *             If fails to invoke the {@link Work}.
	 */
	void invokeTask(String officeName, String workName, String taskName,
			String parameter) throws Exception;

}