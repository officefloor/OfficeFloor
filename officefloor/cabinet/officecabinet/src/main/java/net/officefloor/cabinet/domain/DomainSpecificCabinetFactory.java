package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;

/**
 * Factory for the domain specific {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DomainSpecificCabinetFactory<C> {

	/**
	 * Creates the domain specific {@link OfficeCabinet}.
	 * 
	 * @param session {@link CabinetSession}.
	 * @return Domain specific {@link OfficeCabinet}.
	 */
	C createDomainSpecificCabinet(CabinetSession session);

	/**
	 * Obtains the meta-data regarding the required {@link OfficeCabinetArchive}
	 * necessary to support this {@link DomainSpecificCabinetFactory}.
	 * 
	 * @return Meta-data regarding the required {@link OfficeCabinetArchive}
	 *         necessary to support this {@link DomainSpecificCabinetFactory}.
	 */
	DomainSpecificCabinetDocumentMetaData[] getMetaData();

}