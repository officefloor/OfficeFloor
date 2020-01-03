package net.officefloor.compile.internal.structure;

/**
 * Register to register the possible MBeans.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorMBeanRegistrator {

	/**
	 * Registers a possible MBean.
	 * 
	 * @param type
	 *            Type of MBean.
	 * @param name
	 *            Name of MBean.
	 * @param mbean
	 *            MBean.
	 */
	void registerPossibleMBean(Class<?> type, String name, Object mbean);

}