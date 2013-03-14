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
package net.officefloor.building.process;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * Context for the {@link ManagedProcess}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedProcessContext {

	/**
	 * Obtains the name space for the {@link ManagedProcess}.
	 * 
	 * @return Name space for the {@link ManagedProcess}.
	 */
	String getProcessNamespace();

	/**
	 * <p>
	 * Registers a MBean.
	 * <p>
	 * This is equivalent to corresponding {@link MBeanServer} method except
	 * that it also registers the MBean within the {@link MBeanServer} in the
	 * parent managing the {@link Process}.
	 * 
	 * @param mbean
	 *            MBean.
	 * @param name
	 *            {@link ObjectName} of the MBean.
	 * @throws InstanceAlreadyExistsException
	 *             The MBean is already under the control of the
	 *             {@link MBeanServer}.
	 * @throws MBeanRegistrationException
	 *             Failure in registering the MBean.
	 * @throws NotCompliantMBeanException
	 *             This MBean is not a JMX compliant MBean.
	 * 
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	void registerMBean(Object mbean, ObjectName name)
			throws InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException;

	/**
	 * Flags whether to continue processing.
	 * 
	 * @return <code>true</code> for the {@link ManagedProcess} to continue
	 *         processing. <code>false</code> to gracefully stop.
	 */
	boolean continueProcessing();

}