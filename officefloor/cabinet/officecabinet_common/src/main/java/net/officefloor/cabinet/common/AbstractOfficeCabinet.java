package net.officefloor.cabinet.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.admin.OfficeCabinetAdmin;
import net.officefloor.cabinet.common.manage.ManagedDocument;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * Abstract {@link OfficeCabinet} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinet<R, S, D, M extends AbstractDocumentMetaData<R, S, D>>
		implements OfficeCabinet<D>, OfficeCabinetAdmin {

	/**
	 * Instances used within the {@link OfficeCabinet} session.
	 */
	private final Map<String, D> session = new HashMap<>();

	/**
	 * {@link AbstractDocumentMetaData}.
	 */
	protected final M metaData;

	/**
	 * Instantiate.
	 * 
	 * @param metaData {@link AbstractDocumentMetaData}.
	 */
	public AbstractOfficeCabinet(M metaData) {
		this.metaData = metaData;
	}

	/**
	 * Retrieves the internal {@link Document} by the key.
	 * 
	 * @param key Key for the {@link Document}.
	 * @return Internal {@link Document} or <code>null</code> if not exists.
	 */
	protected abstract R retrieveInternalDocument(String key);

	/**
	 * Stores the {@link InternalDocument}.
	 * 
	 * @param internalDocument {@link InternalDocument}.
	 */
	protected abstract void storeInternalDocument(InternalDocument<S> internalDocument);

	/*
	 * ==================== OfficeCabinet ========================
	 */

	@Override
	public Optional<D> retrieveByKey(String key) {

		// Determine if have in session
		D document = this.session.get(key);
		if (document == null) {

			// Not in session, so attempt to retrieve
			R internalDocument = this.retrieveInternalDocument(key);
			if (internalDocument != null) {

				// Obtain the document
				document = this.metaData.createManagedDocument(internalDocument);

				// Capture in session
				this.session.put(key, document);
			}
		}

		// Return the document
		return document != null ? Optional.of(document) : Optional.empty();
	}

	@Override
	public void store(D document) {

		// Create internal document to store
		InternalDocument<S> internalDocument = this.metaData.createInternalDocumnet(document);

		// Store the changes
		this.storeInternalDocument(internalDocument);

		// Update session with document
		this.session.put(internalDocument.getKey(), document);
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