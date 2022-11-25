package net.officefloor.cabinet.common;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;

/**
 * {@link CabinetManager} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class CabinetManagerImpl implements CabinetManager {

	/**
	 * {@link OfficeCabinetArchive} instances by their {@link Document} type.
	 */
	private final Map<Class<?>, OfficeCabinetArchive<?>> archives;

	/**
	 * {@link OfficeCabinet} instances by their {@link Document} type.
	 */
	private final Map<Class<?>, OfficeCabinet<?>> cabinets = new HashMap<>();

	/**
	 * Instantiate.
	 * 
	 * @param archives {@link OfficeCabinetArchive} instances by their
	 *                 {@link Document} type.
	 */
	public CabinetManagerImpl(Map<Class<?>, OfficeCabinetArchive<?>> archives) {
		this.archives = archives;
	}

	/*
	 * ====================== CabinetManager ======================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public <D> OfficeCabinet<D> getOfficeCabinet(Class<D> documentType) {

		// Determine if cached
		OfficeCabinet<?> cabinet = this.cabinets.get(documentType);
		if (cabinet == null) {

			// Obtain the archive
			OfficeCabinetArchive<?> archive = this.archives.get(documentType);
			if (archive == null) {
				throw new IllegalArgumentException("No " + OfficeCabinetArchive.class.getSimpleName()
						+ " configured for document " + documentType.getName());
			}

			// Create the cabinet
			cabinet = archive.createOfficeCabinet();

			// Cache cabinet for further use
			this.cabinets.put(documentType, cabinet);
		}

		// Return the cabinet
		return (OfficeCabinet<D>) cabinet;
	}

}
