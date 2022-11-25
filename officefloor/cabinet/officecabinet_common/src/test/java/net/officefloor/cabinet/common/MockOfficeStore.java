package net.officefloor.cabinet.common;

import java.util.function.Function;

import net.officefloor.cabinet.AttributeTypesDocument;
import net.officefloor.cabinet.HierarchicalDocument;
import net.officefloor.cabinet.ReferencedDocument;
import net.officefloor.cabinet.ReferencingDocument;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Mock {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockOfficeStore extends AbstractOfficeStore {

	/*
	 * ======================== AbstractOfficeStore ===============================
	 */

	@Override
	protected <D> OfficeCabinetArchive<D> createOfficeCabinetArchive(Class<D> documentType, Index... indexes)
			throws Exception {

		// Obtain the key from document
		Function<D, String> getKey;
		if (documentType.equals(AttributeTypesDocument.class)) {
			getKey = (document) -> ((AttributeTypesDocument) document).getKey();
		} else if (documentType.equals(HierarchicalDocument.class)) {
			getKey = (document) -> ((HierarchicalDocument) document).getKey();
		} else if (documentType.equals(ReferencingDocument.class)) {
			getKey = (document) -> ((ReferencingDocument) document).getKey();
		} else if (documentType.equals(ReferencedDocument.class)) {
			getKey = (document) -> ((ReferencedDocument) document).getKey();
		} else {
			throw new IllegalStateException("Unknown document type " + documentType.getName());
		}

		// Create and return archive
		return new MockOfficeCabinetArchive<>(documentType, getKey);
	}

}