package net.officefloor.cabinet.common;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.cabinet.AttributeTypesDocument;
import net.officefloor.cabinet.Document;
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
public class MockOfficeStore implements OfficeStore {

	/**
	 * {@link OfficeCabinetArchive} instances by their {@link Document} type.
	 */
	private final Map<Class<?>, OfficeCabinetArchive<?>> archives = new HashMap<>();

	/*
	 * ======================== OfficeStore ===============================
	 */

	@Override
	public <D> OfficeCabinetArchive<D> setupOfficeCabinetArchive(Class<D> documentType, Index... indexes)
			throws Exception {

		// Ensure archive not already created
		if (this.archives.containsKey(documentType)) {
			throw new IllegalStateException(OfficeCabinetArchive.class.getSimpleName()
					+ " already created for document type " + documentType.getName());
		}

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

		// Create and register archive
		OfficeCabinetArchive<D> archive = new MockOfficeCabinetArchive<>(documentType, getKey);
		this.archives.put(documentType, archive);

		// Return the archive
		return archive;
	}

}