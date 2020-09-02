package net.officefloor.test;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * JUnit functionality for {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorJUnit {

	/**
	 * Obtains the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	OfficeFloor getOfficeFloor();

	/**
	 * Convenience method to invoke the {@link ProcessState} for the
	 * {@link ManagedFunction} within the default {@link Office}.
	 * 
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param parameter    Parameter to the {@link ManagedFunction}.
	 */
	void invokeProcess(String functionName, Object parameter);

	/**
	 * Convenience method to invoke the {@link ProcessState} for the
	 * {@link ManagedFunction} within the default {@link Office}.
	 * 
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param parameter    Parameter to the {@link ManagedFunction}.
	 * @param waitTime     Time in milliseconds to wait for {@link ProcessState} to
	 *                     complete.
	 */
	void invokeProcess(String functionName, Object parameter, long waitTime);

	/**
	 * Convenience method to invoke the {@link ProcessState} for the
	 * {@link ManagedFunction}.
	 * 
	 * @param officeName   Name of the {@link Office} containing the
	 *                     {@link ManagedFunction}.
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param parameter    Parameter to the {@link ManagedFunction}.
	 * @param waitTime     Time in milliseconds to wait for {@link ProcessState} to
	 *                     complete.
	 */
	void invokeProcess(String officeName, String functionName, Object parameter, long waitTime);

}