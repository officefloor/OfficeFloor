package net.officefloor.cabinet.inmemory;

import net.officefloor.cabinet.AbstractOfficeCabinetTestCase;
import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Tests the {@link AbstractOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class InMemoryOfficeCabinetTest extends AbstractOfficeCabinetTestCase {

	/*
	 * ======================== AbstractOfficeCabinetTest =====================
	 */

	@Override
	protected OfficeStore getOfficeStore() {
		return new InMemoryOfficeStore();
	}

}