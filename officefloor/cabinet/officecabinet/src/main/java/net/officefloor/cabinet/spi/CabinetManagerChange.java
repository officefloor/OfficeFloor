package net.officefloor.cabinet.spi;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;

/**
 * State of the {@link CabinetManager} making changes to the {@link Document}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface CabinetManagerChange {

	/**
	 * Stores the {@link Document}.
	 * 
	 * @param document {@link Document}.
	 * @return {@link Key} to the {@link Document}.
	 */
	String store(Object document);

}
