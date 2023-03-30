package net.officefloor.cabinet.common;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.CabinetManagerChange;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.util.CabinetUtil;

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
			cabinet = this.officeStore.createOfficeCabinet(documentMetaData, this);

			// Cache cabinet for further use
			this.cabinets.put(documentType, cabinet);
		}

		// Return the cabinet
		return (OfficeCabinet<D>) cabinet;
	}

	@Override
	public void flush() {

		// Create the change
		CabinetManagerChange change = new CabinetManagerChangeImpl();

		// Flush changes to persistent store
		for (OfficeCabinet<?> cabinet : this.cabinets.values()) {
			cabinet.flush(change);
		}
	}

	/**
	 * {@link CabinetManagerChange} implementation.
	 */
	private class CabinetManagerChangeImpl implements CabinetManagerChange {

		/*
		 * ===================== CabinetManagerChange ========================
		 */

		@Override
		public String registerDocument(Object document, boolean isDelete) {

			// TODO handle delete

			// Obtain the meta-data for document
			Class<?> documentType = document.getClass();
			DocumentMetaData metaData = CabinetManagerImpl.this.documentMetaDatas.get(documentType);

			// Obtain the key for the document
			String key = metaData.getDocumentKey(document);
			if (key == null) {

				// Generate the key
				key = CabinetUtil.newKey();

				// Load the key
				metaData.setDocumentKey(document, key);
			}

			// Return the key
			return key;
		}
	}

}
