package net.officefloor.cabinet.common;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.spi.CabinetManager;

/**
 * State of the {@link CabinetManager} making changes to the {@link Document}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface CabinetManagerChange<T> {

	/**
	 * Registers the {@link Document} into the change.
	 * 
	 * @param document {@link Document}.
	 * @return {@link Key} to the {@link Document}.
	 */
	String registerDocument(Object document);

	/**
	 * Deletes the {@link Document} in the change.
	 * 
	 * @param document {@link Document}.
	 */
	void deleteDocument(Object document);

	/**
	 * Obtains the transaction for the change.
	 * 
	 * @return Transaction for the change.
	 */
	T getTransaction();

}