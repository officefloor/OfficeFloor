package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.spi.CabinetManager;

/**
 * {@link MethodImplementation} to retrieve a {@link Document} by {@link Key}.
 * 
 * @author Daniel Sagenschneider
 */
public class RetrieveNullableByKeyMethodImplementation<D> extends AbstractRetrieveByKeyMethodImplementation<D> {

	public RetrieveNullableByKeyMethodImplementation(Class<D> documentType) {
		super(documentType);
	}

	/*
	 * ===================== MethodImplementation ==========================
	 */

	@Override
	public Object invoke(CabinetManager cabinetManager, Object[] arguments) throws Exception {
		return this.retrieveByKey(cabinetManager, arguments).orElse(null);
	}

}