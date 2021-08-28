package net.officefloor.cabinet.common.manage;

import net.officefloor.cabinet.Document;

/**
 * Interface implemented by sub-class to manage the {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedDocument {

	/**
	 * Obtains the {@link ManagedDocumentState} for the {@link Document}.
	 * 
	 * @return {@link ManagedDocumentState} for the {@link Document}.
	 */
	ManagedDocumentState $$OfficeFloor$$_getManagedDocumentState();

}