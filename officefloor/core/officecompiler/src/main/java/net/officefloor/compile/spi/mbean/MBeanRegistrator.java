/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.spi.mbean;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * Registers the MBean.
 * 
 * @author Daniel Sagenschneider
 */
public interface MBeanRegistrator {

	/**
	 * Obtains the platform ({@link ManagementFactory#getPlatformMBeanServer()})
	 * {@link MBeanRegistrator}.
	 * 
	 * @return Platform {@link MBeanRegistrator}.
	 */
	static MBeanRegistrator getPlatformMBeanRegistrator() {
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		return (name, mbean) -> mbeanServer.registerMBean(mbean, name);
	}

	/**
	 * Registers an MBean.
	 * 
	 * @param name
	 *            Name of the MBean.
	 * @param mbean
	 *            MBean.
	 * @throws InstanceAlreadyExistsException
	 *             If MBean already registered by name.
	 * @throws MBeanRegistrationException
	 *             If fails to register the MBean.
	 * @throws NotCompliantMBeanException
	 *             If MBean is not compliant.
	 */
	void registerMBean(ObjectName name, Object mbean)
			throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException;

}