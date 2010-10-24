/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ProcessManagerMBean;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.main.OfficeBuildingMain;

/**
 * MBean interface for the {@link OfficeBuildingManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeBuildingManagerMBean {

	/**
	 * <p>
	 * Obtains the time the {@link OfficeBuildingMain} was started.
	 * <p>
	 * The time is specific to the host the {@link OfficeBuildingMain} is
	 * running on.
	 * 
	 * @return Time the {@link OfficeBuildingMain} was started.
	 */
	Date getStartTime();

	/**
	 * <p>
	 * Obtains the {@link JMXServiceURL} of the {@link OfficeBuildingMain} being
	 * managed by this {@link OfficeBuildingManager}.
	 * <p>
	 * The returned value can be used as is for the construction of a
	 * {@link JMXServiceURL} to the {@link OfficeBuildingMain}.
	 * 
	 * @return {@link JMXServiceURL} of the {@link OfficeBuildingMain} being
	 *         managed by this {@link OfficeBuildingManager}.
	 */
	String getOfficeBuildingJmxServiceUrl();

	/**
	 * Obtains the name of the host running the {@link OfficeBuildingMain}.
	 * 
	 * @return Name of the host running the {@link OfficeBuildingMain}.
	 */
	String getOfficeBuildingHostName();

	/**
	 * Obtains the port that the {@link OfficeBuildingMain} is listening on.
	 * 
	 * @return Port that the {@link OfficeBuildingMain} is listening on.
	 */
	int getOfficeBuildingPort();

	/**
	 * <p>
	 * Opens an {@link OfficeFloor} within the {@link OfficeBuildingMain}. Uses
	 * location of a Jar file to seed the class path for the {@link OfficeFloor}.
	 * <p>
	 * Open type parameters for JMX invocation.
	 * 
	 * @param processName
	 *            Name identifying the {@link Process} for the
	 *            {@link OfficeFloor}.
	 * @param jarName
	 *            Name of the JAR containing the {@link OfficeFloor}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor} within the JAR to open.
	 * @param jvmOptions
	 *            Options for the JVM of the {@link Process} to contain the
	 *            {@link OfficeFloor}.
	 * @return {@link Process} name space of the opened {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to open the {@link OfficeFloor}.
	 * 
	 * @see ProcessManagerMBean#getProcessNamespace()
	 */
	String openOfficeFloor(String processName, String jarName,
			String officeFloorLocation, String jvmOptions) throws Exception;

	/**
	 * <p>
	 * Opens an {@link OfficeFloor} within the {@link OfficeBuildingMain}. Uses
	 * Maven artifact reference to seed the class path for the
	 * {@link OfficeFloor}.
	 * <p>
	 * Open type parameters for JMX invocation.
	 * 
	 * @param processName
	 *            Name identifying the {@link Process} for the
	 *            {@link OfficeFloor}.
	 * @param groupId
	 *            Group identifier of the {@link OfficeFloor} artifact.
	 * @param artifactId
	 *            Artifact identifier of the {@link OfficeFloor} artifact.
	 * @param version
	 *            Version of the {@link OfficeFloor} artifact.
	 * @param type
	 *            Type of the {@link OfficeFloor} artifact.
	 * @param classifier
	 *            Classifier of the {@link OfficeFloor} artifact. May be
	 *            <code>null</code> if no classifier.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor} within the artifact to
	 *            open. May be <code>null</code> if provided via
	 *            {@link OfficeFloorDecorator}.
	 * @param jvmOptions
	 *            Options for the JVM of the {@link Process} to contain the
	 *            {@link OfficeFloor}.
	 * @return {@link Process} name space of the opened {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to open the {@link OfficeFloor}.
	 * 
	 * @see ProcessManagerMBean#getProcessNamespace()
	 */
	String openOfficeFloor(String processName, String groupId,
			String artifactId, String version, String type, String classifier,
			String officeFloorLocation, String jvmOptions) throws Exception;

	/**
	 * <p>
	 * Opens an {@link OfficeFloor} within the {@link OfficeBuildingMain}.
	 * <p>
	 * The complex arguments means this method will typically only be able to be
	 * programmatically invoked - not for JMX console invocation. Use the other
	 * <code>openOfficeFloor</code> for this type of invocation.
	 * <p>
	 * Also note that due to this method&apos;s flexibility and non-typed
	 * interface that caution should be taken in using this programmatically as
	 * arguments may change.
	 * 
	 * @param processName
	 *            Name identifying the {@link Process} for the
	 *            {@link OfficeFloor}.
	 * @param arguments
	 *            {@link OfficeFloor} arguments.
	 * @param jvmOptions
	 *            Options for the JVM of the {@link Process} to contain the
	 *            {@link OfficeFloor}.
	 * @return {@link Process} name space of the opened {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to open the {@link OfficeFloor}.
	 * 
	 * @see ProcessManagerMBean#getProcessNamespace()
	 */
	String openOfficeFloor(String processName, String[] arguments,
			String jvmOptions) throws Exception;

	/**
	 * Provides a listing of the {@link Process} name spaces currently running
	 * within the {@link OfficeBuildingMain}.
	 * 
	 * @return Listing of the {@link Process} name spaces currently running
	 *         within the {@link OfficeBuildingMain}.
	 */
	String listProcessNamespaces();

	/**
	 * Closes the {@link OfficeFloor}.
	 * 
	 * @param processNamespace
	 *            Process name space for the {@link OfficeFloor}.
	 * @param waitTime
	 *            Time to wait for {@link OfficeFloor} to stop before being
	 *            destroyed.
	 * @return <code>&quot;Closed&quot;</code> or text explaining why not
	 *         closed.
	 * @throws Exception
	 *             If fails to close the {@link OfficeFloor}.
	 * @see ProcessManagerMBean#getProcessNamespace()
	 */
	String closeOfficeFloor(String processNamespace, long waitTime)
			throws Exception;

	/**
	 * Stops the {@link OfficeBuildingMain}.
	 * 
	 * @param waitTime
	 *            Time to wait for {@link ManagedProcess} instances to stop
	 *            before being destroyed.
	 * @return Details of stopping the {@link OfficeBuildingMain}.
	 * @throws Exception
	 *             If fails to stop the {@link OfficeBuildingMain}.
	 */
	String stopOfficeBuilding(long waitTime) throws Exception;

}