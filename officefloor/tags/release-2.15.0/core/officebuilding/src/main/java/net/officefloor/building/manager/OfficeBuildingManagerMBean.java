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
package net.officefloor.building.manager;

import java.io.IOException;
import java.util.Date;

import javax.management.remote.JMXServiceURL;

import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ProcessException;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;

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
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link OfficeBuildingManagerMBean}.
	 */
	Date getStartTime() throws IOException;

	/**
	 * <p>
	 * Obtains the {@link JMXServiceURL} of the {@link OfficeBuilding} being
	 * managed by this {@link OfficeBuildingManagerMBean}.
	 * <p>
	 * The returned value can be used as is for the construction of a
	 * {@link JMXServiceURL} to the {@link OfficeBuilding}.
	 * 
	 * @return {@link JMXServiceURL} of the {@link OfficeBuilding} being managed
	 *         by this {@link OfficeBuildingManagerMBean}.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link OfficeBuildingManagerMBean}.
	 */
	String getOfficeBuildingJmxServiceUrl() throws IOException;

	/**
	 * Obtains the name of the host running the {@link OfficeBuilding}.
	 * 
	 * @return Name of the host running the {@link OfficeBuilding}.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link OfficeBuildingManagerMBean}.
	 */
	String getOfficeBuildingHostName() throws IOException;

	/**
	 * Obtains the port that the {@link OfficeBuilding} is listening on.
	 * 
	 * @return Port that the {@link OfficeBuilding} is listening on.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link OfficeBuildingManagerMBean}.
	 */
	int getOfficeBuildingPort() throws IOException;

	/**
	 * <p>
	 * Opens an {@link OfficeFloor} within the {@link OfficeBuilding}.
	 * <p>
	 * Allows for open type parameters for JMX invocation.
	 * 
	 * @param arguments
	 *            {@link OfficeFloor} arguments separated by white spacing.
	 * @return {@link Process} name space of the opened {@link OfficeFloor}.
	 * @throws ProcessException
	 *             If fails to open the {@link OfficeFloor}.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link OfficeBuildingManagerMBean}.
	 * 
	 * @see ProcessManagerMBean#getProcessNamespace()
	 */
	String openOfficeFloor(String arguments) throws ProcessException,
			IOException;

	/**
	 * <p>
	 * Opens an {@link OfficeFloor} within the {@link OfficeBuilding}.
	 * <p>
	 * The complex argument means this method will typically only be able to be
	 * programmatically invoked - not for JMX console invocation. Use the other
	 * <code>openOfficeFloor</code> for this type of invocation.
	 * 
	 * @param configuration
	 *            {@link OpenOfficeFloorConfiguration}.
	 * @return {@link Process} name space of the opened {@link OfficeFloor}.
	 * @throws ProcessException
	 *             If fails to open the {@link OfficeFloor}.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link OfficeBuildingManagerMBean}.
	 * 
	 * @see ProcessManagerMBean#getProcessNamespace()
	 */
	String openOfficeFloor(OpenOfficeFloorConfiguration configuration)
			throws ProcessException, IOException;

	/**
	 * Provides a listing of the {@link Process} name spaces currently running
	 * within the {@link OfficeBuilding}.
	 * 
	 * @return Listing of the {@link Process} name spaces currently running
	 *         within the {@link OfficeBuilding}.
	 * @throws ProcessException
	 *             If fails to list process namespaces.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link OfficeBuildingManagerMBean}.
	 */
	String[] listProcessNamespaces() throws ProcessException, IOException;

	/**
	 * Closes the {@link OfficeFloor}.
	 * 
	 * @param processNamespace
	 *            Process name space for the {@link OfficeFloor}.
	 * @param waitTime
	 *            Time to wait for {@link OfficeFloor} to stop before being
	 *            destroyed.
	 * @throws ProcessException
	 *             If fails to close the {@link OfficeFloor}.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link OfficeBuildingManagerMBean}.
	 * @see ProcessManagerMBean#getProcessNamespace()
	 */
	void closeOfficeFloor(String processNamespace, long waitTime)
			throws ProcessException, IOException;

	/**
	 * Stops the {@link OfficeBuilding}.
	 * 
	 * @param waitTime
	 *            Time to wait for {@link ManagedProcess} instances to stop
	 *            before being destroyed.
	 * @return Details of stopping the {@link OfficeBuilding}.
	 * @throws ProcessException
	 *             If fails to stop the {@link OfficeBuilding}.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link OfficeBuildingManagerMBean}.
	 */
	String stopOfficeBuilding(long waitTime) throws ProcessException,
			IOException;

	/**
	 * Indicates if the {@link OfficeBuilding} has been stopped.
	 * 
	 * @return <code>true</code> should the {@link OfficeBuilding} be stopped.
	 * @throws IOException
	 *             If fails communication with the remote
	 *             {@link OfficeBuildingManagerMBean}.
	 */
	boolean isOfficeBuildingStopped() throws IOException;

}