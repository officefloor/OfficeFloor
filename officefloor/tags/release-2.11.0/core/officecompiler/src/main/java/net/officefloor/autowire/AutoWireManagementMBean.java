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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * MBean for auto-wire {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireManagementMBean {

	/**
	 * Invokes the {@link Task} on the {@link OfficeFloor}.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @throws Exception
	 *             If fails to invoke the {@link Task}.
	 */
	void invokeTask(String workName, String taskName) throws Exception;

	/**
	 * Closes the {@link OfficeFloor}.
	 */
	void closeOfficeFloor();

}