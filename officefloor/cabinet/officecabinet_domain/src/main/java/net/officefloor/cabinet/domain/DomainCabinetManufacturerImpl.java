package net.officefloor.cabinet.domain;

/**
 * {@link DomainCabinetManufacturer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class DomainCabinetManufacturerImpl implements DomainCabinetManufacturer {

	/*
	 * ==================== DomainCabinetManufacturer =====================
	 */

	@Override
	public <C> DomainCabinetFactory<C> createDomainCabinetFactory(Class<C> cabinetType) {
		// TODO implement DomainCabinetManufacturer.createDomainSpecificCabinetFactory
		throw new UnsupportedOperationException(
				"TODO implement DomainCabinetManufacturer.createDomainSpecificCabinetFactory");
	}

}