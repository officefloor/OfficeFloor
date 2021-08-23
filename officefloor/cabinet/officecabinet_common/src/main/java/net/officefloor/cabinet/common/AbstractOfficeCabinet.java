package net.officefloor.cabinet.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.admin.OfficeCabinetAdmin;

/**
 * Abstract {@link OfficeCabinet} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinet<D, M extends AbstractOfficeCabinetMetaData<D>>
		implements OfficeCabinet<D>, OfficeCabinetAdmin {

	/**
	 * Instances used within the {@link OfficeCabinet} session.
	 */
	private final Map<String, D> session = new HashMap<>();

	/**
	 * {@link AbstractOfficeCabinetMetaData}.
	 */
	protected final M metaData;

	/**
	 * Instantiate.
	 * 
	 * @param metaData {@link AbstractOfficeCabinetMetaData}.
	 */
	public AbstractOfficeCabinet(M metaData) {
		this.metaData = metaData;
	}

	/**
	 * Retrieves the {@link Document} by the key.
	 * 
	 * @param key Key for the {@link Document}.
	 * @return {@link Document} or <code>null</code> if not exists.
	 */
	protected abstract D _retrieveByKey(String key);

	/**
	 * Stores the {@link Document}.
	 * 
	 * @param document {@link Document}.
	 * @return {@link Key} to the stored {@link Document}.
	 */
	protected abstract String _store(D document);

	/**
	 * Creates an instance {@link ManagedDocument} instance.
	 * 
	 * @return {@link ManagedDocument} instance.
	 */
	protected D createManagedDocument() {
		try {
			return this.metaData.managedDocumentType.getConstructor().newInstance();
		} catch (Exception ex) {
			throw new IllegalStateException("Should be able to create " + ManagedDocument.class.getSimpleName()
					+ " instance for " + this.metaData.documentType.getName(), ex);
		}
	}

	/*
	 * ==================== OfficeCabinet ========================
	 */

	@Override
	public Optional<D> retrieveByKey(String key) {

		// Determine if have in session
		D document = this.session.get(key);
		if (document == null) {

			// Not in session, so attempt to retrieve
			document = this._retrieveByKey(key);
			if (document != null) {

				// Capture in session
				this.session.put(key, document);
			}
		}

		// Return the document
		return document != null ? Optional.of(document) : Optional.empty();
	}

	@Override
	public void store(D document) {

		// Store the changes
		String key = this._store(document);

		// Update session with document
		this.session.put(key, document);
	}

	/*
	 * ================= OfficeCabinetAdmin ========================
	 */

	@Override
	public void close() throws Exception {

		// Store the dirty documents
		for (D document : this.session.values()) {

			// Determine if managed, so can determine if dirty
			if (document instanceof ManagedDocument) {
				ManagedDocument managed = (ManagedDocument) document;
				if (managed.$$OfficeFloor$$_getManagedDocumentState().isDirty) {

					// Dirty so store
					this.store(document);
				}
			}
		}
	}

}