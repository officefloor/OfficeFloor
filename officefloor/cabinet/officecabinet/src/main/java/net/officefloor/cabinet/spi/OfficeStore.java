package net.officefloor.cabinet.spi;

import net.officefloor.cabinet.Document;

/**
 * Office Store.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeStore {

	/**
	 * Sets up the {@link OfficeCabinetArchive} for the {@link Document} type.
	 * 
	 * @param <D>          {@link Document} type.
	 * @param documentType {@link Document} type.
	 * @param indexes      {@link Index} instances for the {@link Document} type.
	 * @return {@link OfficeCabinetArchive} for the {@link Document} type.
	 * @throws Exception If fails to create the {@link OfficeCabinetArchive}.
	 */
	<D> OfficeCabinetArchive<D> setupOfficeCabinetArchive(Class<D> documentType, Index... indexes) throws Exception;

}