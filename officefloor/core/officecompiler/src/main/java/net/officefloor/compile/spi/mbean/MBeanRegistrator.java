/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
