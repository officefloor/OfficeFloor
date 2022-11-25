package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * {@link MethodImplementation} to retrieve a {@link Document} by {@link Key}.
 * 
 * @author Daniel Sagenschneider
 */
public class RetrieveByKeyMethodImplementation<D> implements MethodImplementation {

	private final Class<D> documentType;

	public RetrieveByKeyMethodImplementation(Class<D> documentType) {
		this.documentType = documentType;
	}

	/*
	 * ===================== MethodImplementation ==========================
	 */

	@Override
	public Object invoke(CabinetManager cabinetManager, Object[] arguments) throws Exception {

		// Obtain the office cabinet
		OfficeCabinet<D> cabinet = cabinetManager.getOfficeCabinet(this.documentType);

		// Retrieve by the key
		return cabinet.retrieveByKey((String) arguments[0]);
	}

}