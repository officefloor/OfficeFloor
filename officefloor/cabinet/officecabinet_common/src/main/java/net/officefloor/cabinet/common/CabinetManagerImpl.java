package net.officefloor.cabinet.common;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * {@link CabinetManager} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class CabinetManagerImpl<E> implements CabinetManager {

	/**
	 * {@link DocumentMetaData} instances by their {@link Document} type.
	 */
	private final Map<Class<?>, DocumentMetaData<?, ?, ?, E>> documentMetaDatas;

	/**
	 * {@link AbstractOfficeStore}.
	 */
	private final AbstractOfficeStore<E> officeStore;

	/**
	 * {@link OfficeCabinet} instances by their {@link Document} type.
	 */
	private final Map<Class<?>, OfficeCabinet<?>> cabinets = new HashMap<>();

	/**
	 * Instantiate.
	 * 
	 * @param documentMetaDatas {@link DocumentMetaData} instances by their
	 *                          {@link Document} type.
	 * @param officeStore       {@link AbstractOfficeStore}.
	 */
	public CabinetManagerImpl(Map<Class<?>, DocumentMetaData<?, ?, ?, E>> documentMetaDatas,
			AbstractOfficeStore<E> officeStore) {
		this.documentMetaDatas = documentMetaDatas;
		this.officeStore = officeStore;
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

			// Obtain the meta-data
			DocumentMetaData<?, ?, ?, E> documentMetaData = this.documentMetaDatas.get(documentType);
			if (documentMetaData == null) {
				throw new IllegalArgumentException("No " + OfficeCabinet.class.getSimpleName()
						+ " configured for document " + documentType.getName());
			}

			// Create the cabinet
			cabinet = this.officeStore.createOfficeCabinet(documentMetaData);

			// Cache cabinet for further use
			this.cabinets.put(documentType, cabinet);
		}

		// Return the cabinet
		return (OfficeCabinet<D>) cabinet;
	}

}
