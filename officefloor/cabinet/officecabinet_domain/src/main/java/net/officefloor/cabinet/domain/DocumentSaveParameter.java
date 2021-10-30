package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * {@link SaveParameter} for single {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public class DocumentSaveParameter<D> implements SaveParameter {

	private final Class<D> documentType;

	public DocumentSaveParameter(Class<D> documentType) {
		this.documentType = documentType;
	}

	/*
	 * ==================== SaveParameter ===================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void save(CabinetSession session, Object parameter) {

		// Obtain the cabinet
		OfficeCabinet<D> cabinet = session.getOfficeCabinet(this.documentType);

		// Store the document
		cabinet.store((D) parameter);
	}

}