package net.officefloor.plugin.managedfunction.method;

/**
 * Manufactures the {@link MethodObjectInstanceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodObjectInstanceManufacturer {

	/**
	 * Creates the {@link MethodObjectInstanceFactory}.
	 * 
	 * @return {@link MethodObjectInstanceFactory}.
	 */
	MethodObjectInstanceFactory createMethodObjectInstanceFactory();
}
