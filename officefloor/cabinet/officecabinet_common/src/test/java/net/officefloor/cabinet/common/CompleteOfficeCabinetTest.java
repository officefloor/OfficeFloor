package net.officefloor.cabinet.common;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Function;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.AttributeTypesDocument;
import net.officefloor.cabinet.domain.DomainCabinetManufacturer;
import net.officefloor.cabinet.domain.DomainCabinetManufacturerImpl;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;

/**
 * Tests the {@link AbstractOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompleteOfficeCabinetTest extends AbstractOfficeCabinetTest {

	/*
	 * ======================== AbstractOfficeCabinetTest =====================
	 */

	@Override
	protected <D> OfficeCabinetArchive<D> getOfficeCabinetArchive(Class<D> documentType, Index... indexes)
			throws Exception {

		// Obtain the key from document
		Function<D, String> getKey;
		if (documentType.equals(AttributeTypesDocument.class)) {
			getKey = (document) -> ((AttributeTypesDocument) document).getKey();
		} else {
			return fail("Unknown document type " + documentType.getName());
		}

		// Return the archive
		return new MockOfficeCabinetArchive<>(documentType, getKey);
	}

	@Override
	protected DomainCabinetManufacturer getDomainSpecificCabinetManufacturer() {
		return new DomainCabinetManufacturerImpl(this.getClass().getClassLoader());
	}

}