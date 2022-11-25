package net.officefloor.cabinet.common;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Abstract {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeStore implements OfficeStore {

	/**
	 * {@link OfficeCabinetArchive} instances by their {@link Document} type.
	 */
	protected final Map<Class<?>, OfficeCabinetArchive<?>> archives = new HashMap<>();

	/**
	 * Creates the {@link OfficeCabinetArchive}.
	 * 
	 * @param <D>          {@link Document} type.
	 * @param documentType {@link Document} type.
	 * @param indexes      {@link Index} instances.
	 * @return {@link OfficeCabinetArchive}.
	 * @throws Exception If fails to create {@link OfficeCabinetArchive}.
	 */
	protected abstract <D> OfficeCabinetArchive<D> createOfficeCabinetArchive(Class<D> documentType, Index... indexes)
			throws Exception;

	/*
	 * ====================== OfficeStore =========================
	 */

	@Override
	public <D> void setupOfficeCabinet(Class<D> documentType, Index... indexes) throws Exception {

		// Ensure archive not already created
		if (this.archives.containsKey(documentType)) {
			throw new IllegalStateException(
					OfficeCabinet.class.getSimpleName() + " already setup for document type " + documentType.getName());
		}

		// Create the cabinet archive
		OfficeCabinetArchive<?> archive = this.createOfficeCabinetArchive(documentType, indexes);

		// Register the archive
		this.archives.put(documentType, archive);
	}

	@Override
	public CabinetManager createCabinetManager() {
		return new CabinetManagerImpl(this.archives);
	}

}