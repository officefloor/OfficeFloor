package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;

/**
 * Factory for the domain specific {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DomainCabinetFactory<C> {

	/**
	 * Creates the domain specific {@link CabinetManager}.
	 * 
	 * @param documentManager {@link CabinetManager}.
	 * @return Domain specific {@link OfficeCabinet}.
	 */
	C createDomainSpecificCabinet(CabinetManager documentManager);

	/**
	 * Obtains the meta-data regarding the required {@link OfficeCabinetArchive}
	 * necessary to support this {@link DomainCabinetFactory}.
	 * 
	 * @return Meta-data regarding the required {@link OfficeCabinetArchive}
	 *         necessary to support this {@link DomainCabinetFactory}.
	 */
	DomainCabinetDocumentMetaData[] getMetaData();

}