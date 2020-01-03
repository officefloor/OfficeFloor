package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link OfficeFloor} where {@link ManagedFunction} instances are executed
 * within {@link Office} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloor extends AutoCloseable {

	/**
	 * Opens the OfficeFloor and starts necessary {@link ManagedFunction} instances.
	 * 
	 * @throws Exception If fails to open the OfficeFloor.
	 */
	void openOfficeFloor() throws Exception;

	/**
	 * Closes the OfficeFloor. This stops all {@link ManagedFunction} instances
	 * executing within the {@link Office} instances and releases all resources.
	 * 
	 * @throws Exception If fails to close the {@link OfficeFloor}.
	 */
	default void closeOfficeFloor() throws Exception {
		this.close();
	}

	/**
	 * <p>
	 * Obtains the names of the {@link Office} instances within this
	 * {@link OfficeFloor}.
	 * <p>
	 * This allows to dynamically manage this {@link OfficeFloor}.
	 * 
	 * @return Names of the {@link Office} instances within this
	 *         {@link OfficeFloor}.
	 */
	String[] getOfficeNames();

	/**
	 * Obtains the {@link Office} for the input office name.
	 * 
	 * @param officeName Name of the {@link Office}.
	 * @return Specified {@link Office}.
	 * @throws UnknownOfficeException If no {@link Office} by the name within this
	 *                                {@link OfficeFloor}.
	 */
	Office getOffice(String officeName) throws UnknownOfficeException;

}