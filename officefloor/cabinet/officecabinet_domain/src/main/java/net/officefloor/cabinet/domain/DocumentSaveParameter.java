package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.CabinetManager;
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
	public void save(CabinetManager cabinetManager, Object parameter) {

		// Obtain the cabinet
		OfficeCabinet<D> cabinet = cabinetManager.getOfficeCabinet(this.documentType);

		// Store the document
		cabinet.store((D) parameter);
	}

}