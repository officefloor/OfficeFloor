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