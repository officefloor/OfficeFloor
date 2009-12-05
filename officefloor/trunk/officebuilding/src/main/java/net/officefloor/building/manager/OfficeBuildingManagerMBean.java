/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building.manager;

import java.util.Date;

import javax.management.remote.JMXServiceURL;

/**
 * MBean interface for the {@link OfficeBuildingManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeBuildingManagerMBean {

	/**
	 * <p>
	 * Obtains the time the {@link OfficeBuilding} was started.
	 * <p>
	 * The time is specific to the host the {@link OfficeBuilding} is running
	 * on.
	 * 
	 * @return Time the {@link OfficeBuilding} was started.
	 */
	Date getStartTime();

	/**
	 * <p>
	 * Obtains the {@link JMXServiceURL} of the {@link OfficeBuilding} being
	 * managed by this {@link OfficeBuildingManager}.
	 * <p>
	 * The returned value can be used as is for the construction of a
	 * {@link JMXServiceURL} to the {@link OfficeBuilding}.
	 * 
	 * @return {@link JMXServiceURL} of the {@link OfficeBuilding} being managed
	 *         by this {@link OfficeBuildingManager}.
	 */
	String getOfficeBuildingJmxServiceUrl();

	/**
	 * Obtains the name of the host running the {@link OfficeBuilding}.
	 * 
	 * @return Name of the host running the {@link OfficeBuilding}.
	 */
	String getOfficeBuildingHostName();

	/**
	 * Obtains the port that the {@link OfficeBuilding} is listening on.
	 * 
	 * @return Port that the {@link OfficeBuilding} is listening on.
	 */
	int getOfficeBuildingPort();

	/**
	 * Stops the {@link OfficeBuilding}.
	 * 
	 * @throws Exception
	 *             If fails to stop the {@link OfficeBuilding}.
	 */
	void stopOfficeBuilding() throws Exception;

}