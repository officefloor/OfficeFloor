package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Creates the {@link DomainSpecificCabinetFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DomainSpecificCabinetManufacturer {

	/**
	 * Creates the {@link DomainSpecificCabinetFactory}.
	 * 
	 * @param cabinetType Domain specific {@link OfficeCabinet} type.
	 * @return {@link DomainSpecificCabinetFactory}.
	 */
	<C> DomainSpecificCabinetFactory<C> createDomainSpecificCabinetFactory(Class<C> cabinetType);

}