package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Meta-data for a particular {@link Document} used be a domain specific
 * {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class DomainSpecificCabinetDocumentMetaData {

	private final Class<?> documentType;

	private final Index[] indexes;

	public DomainSpecificCabinetDocumentMetaData(Class<?> documentType, Index[] indexes) {
		this.documentType = documentType;
		this.indexes = indexes;
	}

	public Class<?> getDocumentType() {
		return documentType;
	}

	public Index[] getIndexes() {
		return indexes;
	}

}
