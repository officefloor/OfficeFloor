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

import java.io.Serializable;

import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

/**
 * Notifies of an MBean registration.
 * 
 * @author Daniel Sagenschneider
 */
public class MBeanRegistrationNotification implements Serializable {

	/**
	 * {@link JMXServiceURL} of the {@link JMXConnectorServer} where the MBean
	 * is registered.
	 */
	private final JMXServiceURL serviceUrl;

	/**
	 * MBean {@link ObjectName}.
	 */
	private final ObjectName mbeanName;

	/**
	 * Initiate.
	 * 
	 * @param serviceUrl
	 *            {@link JMXServiceURL} of the {@link JMXConnectorServer} where
	 *            the MBean is registered.
	 * @param mbeanName
	 *            MBean {@link ObjectName}.
	 */
	public MBeanRegistrationNotification(JMXServiceURL serviceUrl,
			ObjectName mbeanName) {
		this.serviceUrl = serviceUrl;
		this.mbeanName = mbeanName;
	}

	/**
	 * Obtains the {@link JMXServiceURL} of the {@link JMXConnectorServer} where
	 * the MBean is registered.
	 * 
	 * @return {@link JMXServiceURL} of the {@link JMXConnectorServer} where the
	 *         MBean is registered.
	 */
	public JMXServiceURL getServiceUrl() {
		return this.serviceUrl;
	}

	/**
	 * Obtains the {@link ObjectName} for the MBean.
	 * 
	 * @return {@link ObjectName} for the MBean.
	 */
	public ObjectName getMBeanName() {
		return this.mbeanName;
	}

}