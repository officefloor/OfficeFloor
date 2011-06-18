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

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Administration of an {@link AutoWireOfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireAdministration implements AutoWireAdministrationMBean {

	/**
	 * Name value for the {@link AutoWireAdministrationMBean}.
	 */
	public static final String MBEAN_NAME = "AutoWireAdministration";

	/**
	 * Obtains the index for the next {@link ObjectName} to ensure uniqueness.
	 */
	private static int nextNameIndex = 0;

	/**
	 * {@link QueryExp} to search for the {@link AutoWireOfficeFloorImpl}
	 * instances.
	 */
	private static QueryExp SEARCH_QUERY;

	static {
		try {
			SEARCH_QUERY = new ObjectName("officefloor:type="
					+ AutoWireAdministration.class.getName() + ",name="
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
	public static AutoWireOfficeFloor createAutoWireOfficeFloor(
			OfficeFloor officeFloor, String officeName) throws Exception {
		synchronized (AutoWireAdministration.class) {

			// Obtain the unique index for the OfficeFloor
			int index = nextNameIndex++; // increment for next

			// Create the Object Name
			ObjectName objectName = new ObjectName("officefloor:type="
					+ AutoWireAdministration.class.getName() + ",name="
					+ MBEAN_NAME + "_" + index);

			// Create the auto-wire OfficeFloor (and its administration)
			AutoWireAdministration administration = new AutoWireAdministration();
			AutoWireOfficeFloorImpl autoWire = new AutoWireOfficeFloorImpl(
					officeFloor, officeName, objectName, administration);
			administration.autoWireOfficeFloor = autoWire;

			// Register the auto-wire administration
			ManagementFactory.getPlatformMBeanServer().registerMBean(
					administration, objectName);

			// Return the auto-wire OfficeFloor
			return autoWire;
		}
	}

	/**
	 * Obtains the {@link AutoWireAdministrationMBean} instances.
	 * 
	 * @return {@link AutoWireAdministrationMBean} instances.
	 */
	public static AutoWireAdministrationMBean[] getAutoWireAdministrators() {

		// Obtain the MBean Server
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

		// Obtain the AutoWireOfficeFloor MBean names
		Set<ObjectName> names = mbeanServer.queryNames(null, SEARCH_QUERY);

		// Create the listing of the AutoWireOfficeFloor MBeans
		List<AutoWireAdministrationMBean> mbeans = new ArrayList<AutoWireAdministrationMBean>(
				names.size());
		for (ObjectName name : names) {
			AutoWireAdministrationMBean mbean = JMX.newMBeanProxy(mbeanServer,
					name, AutoWireAdministrationMBean.class);
			mbeans.add(mbean);
		}

		// Return the MBeans
		return mbeans.toArray(new AutoWireAdministrationMBean[mbeans.size()]);
	}

	/**
	 * Convenience method to ensure all {@link AutoWireOfficeFloor} instances
	 * are closed.
	 */
	public static void closeAllOfficeFloors() {
		for (AutoWireAdministrationMBean mbean : getAutoWireAdministrators()) {
			mbean.closeOfficeFloor();
		}
	}

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor autoWireOfficeFloor;

	/**
	 * Disallow instantiated except from static methods of this class.
	 */
	private AutoWireAdministration() {
	}

	/*
	 * ==================== AutoWireAdministrationMBean =====================
	 */

	@Override
	public void invokeTask(String workName, String taskName) throws Exception {
		synchronized (AutoWireAdministration.class) {
			this.autoWireOfficeFloor.invokeTask(workName, taskName, null);
		}
	}

	@Override
	public void closeOfficeFloor() {
		synchronized (AutoWireAdministration.class) {

			// Close the OfficeFloor
			this.autoWireOfficeFloor.getOfficeFloor().closeOfficeFloor();

			// Ensure unregistered
			try {
				MBeanServer mbeanServer = ManagementFactory
						.getPlatformMBeanServer();
				ObjectName name = this.autoWireOfficeFloor.getObjectName();
				if (mbeanServer.isRegistered(name)) {
					mbeanServer.unregisterMBean(name);
				}
			} catch (Exception ex) {
				throw new IllegalStateException(
						"Should not fail to unregister AutoWireAdministration MBean",
						ex);
			}
		}
	}

}