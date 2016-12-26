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
package net.officefloor.autowire.impl;

import javax.management.ObjectName;

import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireManagementMBean;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.ProcessContextTeam;

/**
 * {@link AutoWireManagementMBean} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorImpl implements AutoWireOfficeFloor {

	/**
	 * {@link OfficeFloor}.
	 */
	private final OfficeFloor officeFloor;

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link ObjectName} for this {@link AutoWireOfficeFloorImpl}.
	 */
	private final ObjectName objectName;

	/**
	 * {@link AutoWireManagement}.
	 */
	private final AutoWireManagement administration;

	/**
	 * Initiate.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloor}.
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param objectName
	 *            {@link ObjectName} for this {@link AutoWireOfficeFloorImpl}.
	 * @param administration
	 *            {@link AutoWireManagement}.
	 */
	public AutoWireOfficeFloorImpl(OfficeFloor officeFloor, String officeName,
			ObjectName objectName, AutoWireManagement administration) {
		this.officeFloor = officeFloor;
		this.officeName = officeName;
		this.objectName = objectName;
		this.administration = administration;
	}

	/*
	 * =========================== AutoWireOfficeFloor =========================
	 */

	@Override
	public OfficeFloor getOfficeFloor() {
		return this.officeFloor;
	}

	@Override
	public ObjectName getObjectName() {
		return this.objectName;
	}

	@Override
	public void invokeTask(String workName, String taskName, Object parameter)
			throws Exception {

		// Obtain the Task
		Office office = this.officeFloor.getOffice(this.officeName);
		WorkManager workManager = office.getWorkManager(workName);
		FunctionManager taskManager = workManager.getTaskManager(taskName);

		// Invoke the task
		ProcessContextTeam.doTask(taskManager, parameter);
	}

	@Override
	public void closeOfficeFloor() {
		this.administration.closeOfficeFloor();
	}

}