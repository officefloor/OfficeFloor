package net.officefloor.cabinet.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.DocumentBundle;
import net.officefloor.cabinet.admin.OfficeCabinetAdmin;
import net.officefloor.cabinet.common.adapt.InternalRange;
import net.officefloor.cabinet.common.adapt.StartAfterDocumentValueGetter;
import net.officefloor.cabinet.common.manage.ManagedDocument;
import net.officefloor.cabinet.common.manage.ManagedDocumentState;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Range;

/**
 * Abstract {@link OfficeCabinet} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinet<R, S, D, M extends AbstractDocumentMetaData<R, S, ?, D>>
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
	 * Retrieves the internal {@link Document} instances by {@link Query}.
	 * 
	 * @param query {@link Query} of the {@link InternalDocument} instances.
	 * @param range {@link InternalRange} to limit {@link InternalDocument}
	 *              instances.
	 * @return {@link InternalDocument} instances for the {@link Query}.
	 */
	protected abstract InternalDocumentBundle<R> retrieveInternalDocuments(Query query, InternalRange<R> range);

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
				document = this.metaData.createManagedDocument(internalDocument, new ManagedDocumentState());

				// Capture in session
				this.session.put(key, document);
			}
		}

		// Return the document
		return document != null ? Optional.of(document) : Optional.empty();
	}

	@Override
	public DocumentBundle<D> retrieveByQuery(Query query, Range<D> range) {

		// Retrieve the internal documents
		InternalRange<R> initialRange;
		int bundleLimit;
		if (range == null) {
			// No range
			initialRange = null;
			bundleLimit = -1;

		} else {
			// +1 on limit to know if have all documents (avoid getting empty next bundle)
			bundleLimit = range.getLimit();
			int limit = bundleLimit > 0 ? bundleLimit + 1 : bundleLimit; // handle no limit

			// Determine if starting document
			D startAfterDocument = range.getStartAfterDocument();
			StartAfterDocumentValueGetter startAfterDocumentValueGetter = startAfterDocument != null
					? this.metaData.createStartAfterDocumentValueGetter(startAfterDocument)
					: null;

			// Create the initial range
			initialRange = new InternalRange<R>(range.getFieldName(), range.getDirection(), limit,
					startAfterDocumentValueGetter);
		}
		InternalDocumentBundle<R> internalDocumentBundle = this.retrieveInternalDocuments(query, initialRange);

		// Return the document bundle
		return new DocumentBundleWrapper(internalDocumentBundle, bundleLimit);
	}

	@Override
	public void store(D document) {

		// Create internal document to store
		InternalDocument<S> internalDocument = this.metaData.createInternalDocument(document);

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
				if (managed.get$$OfficeFloor$$_managedDocumentState().isDirty) {

					// Dirty so store
					this.store(document);
				}
			}
		}
	}

	/**
	 * Wrapper of raw {@link Document} to {@link Document}.
	 */
	private class DocumentBundleWrapper implements DocumentBundle<D> {

		/**
		 * {@link InternalDocumentBundle}.
		 */
		private final InternalDocumentBundle<R> internalBundle;

		/**
		 * Limit.
		 */
		private final int limit;

		/**
		 * Number already iterated over.
		 */
		private int iterated = 0;

		/**
		 * Last {@link InternalDocument}.
		 */
		private R lastInternalDocument = null;

		/**
		 * Instantiate.
		 * 
		 * @param internalBundle {@link InternalDocumentBundle}.
		 * @param limit          Limit for this {@link DocumentBundleWrapper}.
		 */
		private DocumentBundleWrapper(InternalDocumentBundle<R> internalBundle, int limit) {
			this.internalBundle = internalBundle;
			this.limit = limit;
		}

		/*
		 * ================== DocumentBundle ====================
		 */

		@Override
		public boolean hasNext() {

			// Determine if reached limit
			if (this.limit > 0) {

				// Determine if already at limit
				if (this.iterated >= this.limit) {
					return false; // limit reached
				}

				// Increment for next
				this.iterated++;
			}

			// Determine if has next
			return this.internalBundle.hasNext();
		}

		@Override
		public D next() {

			// Easy access to cabinet
			@SuppressWarnings("resource")
			AbstractOfficeCabinet<R, S, D, M> cabinet = AbstractOfficeCabinet.this;

			// Obtain the next internal document
			R internalDocument = this.internalBundle.next();

			// Keep track of last document
			this.lastInternalDocument = internalDocument;

			// Obtain the key for the internal document
			String key = cabinet.metaData.getKey(internalDocument);

			// Determine if in session
			D document = cabinet.session.get(key);
			if (document == null) {

				// Not in session, so load document
				document = cabinet.metaData.createManagedDocument(internalDocument, new ManagedDocumentState());

				// Capture in session
				cabinet.session.put(key, document);
			}

			// Return the document
			return document;
		}

		@Override
		public DocumentBundle<D> nextDocumentBundle() {

			// Easy access to cabinet
			@SuppressWarnings("resource")
			AbstractOfficeCabinet<R, S, D, M> cabinet = AbstractOfficeCabinet.this;

			// Consume all the documents
			while (this.hasNext()) {
				this.next();
			}

			// Determine if further bundles
			if ((this.limit <= 0) || (!this.internalBundle.hasNext())) {
				return null; // no further document bundles
			}

			// Obtain the next start after document
			StartAfterDocumentValueGetter startAfterDocumentValueGetter;
			if (this.lastInternalDocument == null) {
				// No start after document
				startAfterDocumentValueGetter = null;

			} else {
				// Create start after document value getter
				D document = cabinet.metaData.createManagedDocument(this.lastInternalDocument, null);
				startAfterDocumentValueGetter = cabinet.metaData.createStartAfterDocumentValueGetter(document);
			}

			// Obtain the next document bundle
			InternalDocumentBundle<R> nextInternalBundle = this.internalBundle
					.nextDocumentBundle(startAfterDocumentValueGetter);
			return nextInternalBundle != null ? new DocumentBundleWrapper(nextInternalBundle, this.limit) : null;
		}
	}

}