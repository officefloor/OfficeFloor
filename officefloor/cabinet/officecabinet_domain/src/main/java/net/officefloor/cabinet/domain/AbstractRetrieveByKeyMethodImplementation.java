package net.officefloor.cabinet.domain;

import java.util.Optional;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Abstract {@link MethodImplementation} to retrieve a {@link Document} by
 * {@link Key}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractRetrieveByKeyMethodImplementation<D> implements MethodImplementation {

	/**
	 * {@link Document} type.
	 */
	private final Class<D> documentType;

	/**
	 * Instantiate.
	 * 
	 * @param documentType {@link Document} type.
	 */
	public AbstractRetrieveByKeyMethodImplementation(Class<D> documentType) {
		this.documentType = documentType;
	}

	/**
	 * Retrieves by {@link Key}.
	 * 
	 * @param cabinetManager {@link CabinetManager}.
	 * @param arguments      Arguments.
	 * @return {@link Optional} for possible retrieved {@link Document}.
	 * @throws Exception If fails to retrieve.
	 */
	protected Optional<D> retrieveByKey(CabinetManager cabinetManager, Object[] arguments) throws Exception {

		// Obtain the office cabinet
		OfficeCabinet<D> cabinet = cabinetManager.getOfficeCabinet(this.documentType);

		// Retrieve by the key
		return cabinet.retrieveByKey((String) arguments[0]);
	}

}