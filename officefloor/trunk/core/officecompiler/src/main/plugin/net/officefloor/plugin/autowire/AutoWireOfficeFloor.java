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
package net.officefloor.plugin.autowire;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.QueryExp;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.ProcessContextTeam;

/**
 * {@link AutoWireOfficeFloorMBean} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloor implements AutoWireOfficeFloorMBean {

	/**
	 * Name value for the {@link AutoWireOfficeFloor} MBean.
	 */
	public static final String MBEAN_NAME = "AutoWireOfficeFloor";

	/**
	 * Obtains the index for the next {@link ObjectName} to ensure uniqueness.
	 */
	private static int nextNameIndex = 0;

	/**
	 * {@link QueryExp} to search for the {@link AutoWireOfficeFloor} instances.
	 */
	private static QueryExp SEARCH_QUERY;

	static {
		try {
			SEARCH_QUERY = new ObjectName("officefloor:type="
					+ AutoWireOfficeFloor.class.getName() + ",name="
					+ MBEAN_NAME + "*");
		} catch (Exception ex) {
			// This should never be the case
		}
	}

	/**
	 * Creates the {@link AutoWireOfficeFloor}.
	 * 
	 * @return {@link AutoWireOfficeFloor}.
	 * @throws Exception
	 *             If fails to create the {@link AutoWireOfficeFloor}.
	 */
	public static synchronized AutoWireOfficeFloor createAutoWireOfficeFloor(
			OfficeFloor officeFloor, String officeName) throws Exception {

		// Obtain the unique index for the OfficeFloor
		int index = nextNameIndex++; // increment for next

		// Create the Object Name
		ObjectName objectName = new ObjectName("officefloor:type="
				+ AutoWireOfficeFloor.class.getName() + ",name=" + MBEAN_NAME
				+ "_" + index);

		// Create the auto-wire OfficeFloor
		AutoWireOfficeFloor autoWire = new AutoWireOfficeFloor(officeFloor,
				officeName, objectName);

		// Register the auto-wire OfficeFloor
		ManagementFactory.getPlatformMBeanServer().registerMBean(autoWire,
				objectName);

		// Return the auto-wire OfficeFloor
		return autoWire;
	}

	/**
	 * Obtains the {@link AutoWireOfficeFloorMBean} instances.
	 * 
	 * @return {@link AutoWireOfficeFloorMBean} instances.
	 */
	public static AutoWireOfficeFloorMBean[] getOfficeFloors() {

		// Obtain the MBean Server
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

		// Obtain the AutoWireOfficeFloor MBean names
		Set<ObjectName> names = mbeanServer.queryNames(null, SEARCH_QUERY);

		// Create the listing of the AutoWireOfficeFloor MBeans
		List<AutoWireOfficeFloorMBean> mbeans = new ArrayList<AutoWireOfficeFloorMBean>(
				names.size());
		for (ObjectName name : names) {
			AutoWireOfficeFloorMBean mbean = JMX.newMBeanProxy(mbeanServer,
					name, AutoWireOfficeFloorMBean.class);
			mbeans.add(mbean);
		}

		// Return the MBeans
		return mbeans.toArray(new AutoWireOfficeFloorMBean[mbeans.size()]);
	}

	/**
	 * Convenience method to ensure all {@link AutoWireOfficeFloor} instances
	 * are closed.
	 */
	public static void closeAllOfficeFloors() {
		for (AutoWireOfficeFloorMBean mbean : getOfficeFloors()) {
			mbean.closeOfficeFloor();
		}
	}

	/**
	 * {@link OfficeFloor}.
	 */
	private final OfficeFloor officeFloor;

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link ObjectName} for this {@link AutoWireOfficeFloor}.
	 */
	private final ObjectName objectName;

	/**
	 * Initiate.
	 * 
	 * @param officeFloor
	 *            {@link OfficeFloor}.
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param objectName
	 *            {@link ObjectName} for this {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor(OfficeFloor officeFloor, String officeName,
			ObjectName objectName) {
		this.officeFloor = officeFloor;
		this.officeName = officeName;
		this.objectName = objectName;
	}

	/**
	 * Obtains the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	public OfficeFloor getOfficeFloor() {
		return this.officeFloor;
	}

	/**
	 * Obtains the {@link ObjectName} for this {@link AutoWireOfficeFloor}.
	 * 
	 * @return {@link ObjectName} for this {@link AutoWireOfficeFloor}.
	 */
	public ObjectName getObjectName() {
		return this.objectName;
	}

	/**
	 * <p>
	 * Invokes a {@link Task} on the {@link OfficeFloor}.
	 * <p>
	 * Should the {@link OfficeFloor} not be open, it is opened before invoking
	 * the {@link Task}. Please note however the {@link OfficeFloor} will not be
	 * re-opened after being closed.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param parameter
	 *            Parameter for the {@link Task}. May be <code>null</code>.
	 * @return {@link AutoWireOfficeFloor}.
	 * @throws Exception
	 *             If fails invoking the {@link Task}.
	 */
	public void invokeTask(String workName, String taskName, Object parameter)
			throws Exception {

		// Obtain the Task
		Office office = this.officeFloor.getOffice(this.officeName);
		WorkManager workManager = office.getWorkManager(workName);
		TaskManager taskManager = workManager.getTaskManager(taskName);

		// Invoke the task
		ProcessContextTeam.doTask(taskManager, parameter);
	}

	/*
	 * ==================== AutoWireOfficeFloorMBean =====================
	 */

	@Override
	public void invokeTask(String workName, String taskName) throws Exception {
		this.invokeTask(workName, taskName, null);
	}

	@Override
	public void closeOfficeFloor() {

		// Close the OfficeFloor
		this.officeFloor.closeOfficeFloor();

		// Ensure unregistered
		try {
			MBeanServer mbeanServer = ManagementFactory
					.getPlatformMBeanServer();
			if (mbeanServer.isRegistered(this.objectName)) {
				mbeanServer.unregisterMBean(this.objectName);
			}
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Should not fail to unregister OfficeFloor MBean", ex);
		}
	}

}