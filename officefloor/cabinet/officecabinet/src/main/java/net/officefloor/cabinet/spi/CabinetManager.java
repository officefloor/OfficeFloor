package net.officefloor.cabinet.spi;

import net.officefloor.cabinet.Document;

/**
 * Manages a session with the {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CabinetManager {

	/**
	 * Obtains the {@link OfficeCabinet} for the {@link Document} type.
	 * 
	 * @param <D>          {@link Document} type.
	 * @param documentType {@link Document} type.
	 * @return {@link OfficeCabinet} for the {@link Document} type.
	 */
	<D> OfficeCabinet<D> getOfficeCabinet(Class<D> documentType);

	/**
	 * Flush changes to persistent store.
	 */
	void flush();

}