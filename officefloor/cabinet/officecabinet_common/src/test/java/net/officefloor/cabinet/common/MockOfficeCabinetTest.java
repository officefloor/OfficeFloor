package net.officefloor.cabinet.common;

import net.officefloor.cabinet.AbstractOfficeCabinetTestCase;
import net.officefloor.cabinet.domain.DomainCabinetManufacturer;
import net.officefloor.cabinet.domain.DomainCabinetManufacturerImpl;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Tests the {@link AbstractOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockOfficeCabinetTest extends AbstractOfficeCabinetTestCase {

	/*
	 * ======================== AbstractOfficeCabinetTest =====================
	 */

	@Override
	protected OfficeStore getOfficeStore() {
		return new MockOfficeStore();
	}

	@Override
	protected DomainCabinetManufacturer getDomainSpecificCabinetManufacturer() {
		return new DomainCabinetManufacturerImpl(this.getClass().getClassLoader());
	}

}