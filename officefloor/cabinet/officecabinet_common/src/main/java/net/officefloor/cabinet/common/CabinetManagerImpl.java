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
public class CabinetManagerImpl<E, T> implements CabinetManager {

	/**
	 * {@link DocumentMetaData} instances by their {@link Document} type.
	 */
	private final Map<Class<?>, DocumentMetaData<?, ?, ?, E, T>> documentMetaDatas;

	/**
	 * {@link AbstractOfficeStore}.
	 */
	private final AbstractOfficeStore<E, T> officeStore;

	/**
	 * {@link OfficeCabinet} instances by their {@link Document} type.
	 */
	private final Map<Class<?>, AbstractOfficeCabinet<?, ?, ?, E, T>> cabinets = new HashMap<>();

	/**
	 * Instantiate.
	 * 
	 * @param documentMetaDatas {@link DocumentMetaData} instances by their
	 *                          {@link Document} type.
	 * @param officeStore       {@link AbstractOfficeStore}.
	 */
	public CabinetManagerImpl(Map<Class<?>, DocumentMetaData<?, ?, ?, E, T>> documentMetaDatas,
			AbstractOfficeStore<E, T> officeStore) {
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
		AbstractOfficeCabinet<?, ?, ?, E, T> cabinet = this.cabinets.get(documentType);
		if (cabinet == null) {

			// Obtain the meta-data
			DocumentMetaData<?, ?, ?, E, T> documentMetaData = this.documentMetaDatas.get(documentType);
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
	public void flush() throws Exception {

		// Undertake changes within possible transaction
		this.officeStore.transact((transaction) -> {

			// Create the change
			CabinetManagerChange change = new CabinetManagerChangeImpl(transaction);

			// Flush changes to persistent store
			for (AbstractOfficeCabinet<?, ?, ?, E, T> cabinet : this.cabinets.values()) {
				cabinet.flush(change);
			}
		});
	}

	/**
	 * {@link CabinetManagerChange} implementation.
	 */
	private class CabinetManagerChangeImpl implements CabinetManagerChange<T> {

		/**
		 * Transaction.
		 */
		private final T transaction;

		/**
		 * Instantiate.
		 * 
		 * @param transaction Transaction.
		 */
		private CabinetManagerChangeImpl(T transaction) {
			this.transaction = transaction;
		}

		/*
		 * ===================== CabinetManagerChange ========================
		 */

		@Override
		public String registerDocument(Object document) {

			// Obtain the meta-data for document
			Class<?> documentType = document.getClass();
			DocumentMetaData metaData = CabinetManagerImpl.this.documentMetaDatas.get(documentType);

			// Obtain the key for the document
			String key = metaData.getOrLoadDocumentKey(document);

			// Store the document
			AbstractOfficeCabinet cabinet = (AbstractOfficeCabinet) CabinetManagerImpl.this
					.getOfficeCabinet(documentType);
			cabinet.flushDocument(document, this);

			// Return the key
			return key;
		}

		@Override
		public void deleteDocument(Object document) {
			// TODO implement
			throw new UnsupportedOperationException("TODO implement deleteDocument");
		}

		@Override
		public T getTransaction() {
			return this.transaction;
		}
	}

}
