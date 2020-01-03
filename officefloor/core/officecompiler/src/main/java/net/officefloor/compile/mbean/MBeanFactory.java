package net.officefloor.compile.mbean;

/**
 * <p>
 * Factory to create the MBean.
 * <p>
 * Sources may implement this interface to enable creating another object for
 * its MBean.
 * 
 * @author Daniel Sagenschneider
 */
public interface MBeanFactory {

	/**
	 * Creates the MBean.
	 * 
	 * @return MBean.
	 */
	Object createMBean();

}