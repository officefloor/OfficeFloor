package net.officefloor.plugin.managedobject.clazz;

import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Mock {@link FlowInterface}.
 * 
 * @author Daniel Sagenschneider
 */
@FlowInterface
public interface MockProcessInterface {

	/**
	 * Method to invoke a {@link ProcessState} without a parameter.
	 */
	void doProcess();

	/**
	 * Method to invoke a {@link ProcessState} with a parameter.
	 * 
	 * @param parameter Parameter to the {@link ProcessState}.
	 */
	void parameterisedProcess(Integer parameter);

}