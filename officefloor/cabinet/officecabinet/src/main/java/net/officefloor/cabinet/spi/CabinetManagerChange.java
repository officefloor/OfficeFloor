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
	 * Registers the {@link Document} into the change.
	 * 
	 * @param document {@link Document}.
	 * @param isDelete Flags the {@link Document} for deletion.
	 * @return {@link Key} to the {@link Document}.
	 */
	String registerDocument(Object document, boolean isDelete);

}