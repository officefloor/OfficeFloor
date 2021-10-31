package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Creates the {@link DomainCabinetFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DomainCabinetManufacturer {

	/**
	 * Creates the {@link DomainCabinetFactory}.
	 * 
	 * @param cabinetType Domain specific {@link OfficeCabinet} type.
	 * @return {@link DomainCabinetFactory}.
	 * @throws Exception If fails to create {@link DomainCabinetFactory}.
	 */
	<C> DomainCabinetFactory<C> createDomainCabinetFactory(Class<C> cabinetType) throws Exception;

}