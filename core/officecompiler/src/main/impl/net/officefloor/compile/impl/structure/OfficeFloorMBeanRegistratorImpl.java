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

package net.officefloor.compile.impl.structure;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import net.officefloor.compile.internal.structure.OfficeFloorMBeanRegistrator;
import net.officefloor.compile.mbean.MBeanFactory;
import net.officefloor.compile.spi.mbean.MBeanRegistrator;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;

/**
 * {@link OfficeFloorMBeanRegistrator} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMBeanRegistratorImpl implements OfficeFloorMBeanRegistrator, OfficeFloorListener {

	/**
	 * {@link MBeanRegistrator}.
	 */
	private final MBeanRegistrator mbeanRegistrator;

	/**
	 * {@link PossibleMBean} instances.
	 */
	private final List<PossibleMBean> possibleMBeans = new ArrayList<>();

	/**
	 * {@link ObjectName} instances of the registered MBeans.
	 */
	private final List<ObjectName> registeredMBeans = new ArrayList<>();

	/**
	 * Instantiate.
	 * 
	 * @param mbeanRegistrator
	 *            {@link MBeanRegistrator}.
	 */
	public OfficeFloorMBeanRegistratorImpl(MBeanRegistrator mbeanRegistrator) {
		this.mbeanRegistrator = mbeanRegistrator;
	}

	/*
	 * ======================= MBeanRegistrator ======================
	 */

	@Override
	public void registerPossibleMBean(Class<?> type, String name, Object mbean) {
		this.possibleMBeans.add(new PossibleMBean(type, name, mbean));
	}

	/*
	 * ====================== OfficeFloorListener ====================
	 */

	@Override
	public void officeFloorOpened(OfficeFloorEvent event) throws Exception {

		// Attempt to register the MBeans
		for (PossibleMBean possibleMBean : this.possibleMBeans) {

			// Create the name for the MBean
			ObjectName name = new ObjectName(
					"net.officefloor:type=" + possibleMBean.type.getName() + ",name=" + possibleMBean.name);

			// Determine if MBean factory
			Object mbean = possibleMBean.mbean;
			if (mbean instanceof MBeanFactory) {
				mbean = ((MBeanFactory) mbean).createMBean();
			}

			try {
				// Attempt to register the MBean
				this.mbeanRegistrator.registerMBean(name, mbean);

				// MBean registered
				this.registeredMBeans.add(name);

			} catch (NotCompliantMBeanException ex) {
				// Ignore registering, as not an MBean
			}
		}
	}

	@Override
	public void officeFloorClosed(OfficeFloorEvent event) throws Exception {

		// Obtain the MBean server
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		// Unregister the MBeans
		for (ObjectName registeredName : this.registeredMBeans) {
			server.unregisterMBean(registeredName);
		}
	}

	/**
	 * Possible MBean.
	 */
	private static class PossibleMBean {

		/**
		 * Type of MBeann.
		 */
		private final Class<?> type;

		/**
		 * Name of MBean.
		 */
		private final String name;

		/**
		 * Possible MBean.
		 */
		private final Object mbean;

		/**
		 * Instantiate.
		 * 
		 * @param type
		 *            Type of MBean.
		 * @param name
		 *            Name of MBean.
		 * @param mbean
		 *            Possible MBean.
		 */
		public PossibleMBean(Class<?> type, String name, Object mbean) {
			this.type = type;
			this.name = name;
			this.mbean = mbean;
		}
	}

}
