package net.officefloor.cabinet.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.DocumentBundle;
import net.officefloor.cabinet.common.adapt.InternalRange;
import net.officefloor.cabinet.common.adapt.StartAfterDocumentValueGetter;
import net.officefloor.cabinet.common.manage.ManagedDocumentState;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Query.QueryField;
import net.officefloor.cabinet.spi.Range;

/**
 * Abstract {@link OfficeCabinet} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinet<R, S, D, E, T> implements OfficeCabinet<D> {

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Instances used within the {@link OfficeCabinet} session.
	 */
	private final Map<String, D> session = new HashMap<>();

	/**
	 * {@link DocumentMetaData}.
	 */
	protected final DocumentMetaData<R, S, D, E, T> metaData;

	/**
	 * Indicates if check for next {@link DocumentBundle} by retrieving an extra
	 * {@link InternalDocument}.
	 */
	private final boolean isCheckNextBundleViaExtraDocument;

	/**
	 * {@link CabinetManager}.
	 */
	private final CabinetManager cabinetManager;

	/**
	 * Instantiate.
	 * 
	 * @param metaData                          {@link DocumentMetaData}.
	 * @param isCheckNextBundleViaExtraDocument Indicates if check for next
	 *                                          {@link DocumentBundle} by retrieving
	 *                                          an extra {@link InternalDocument}.
	 * @param cabinetManager                    {@link CabinetManager}.
	 */
	public AbstractOfficeCabinet(DocumentMetaData<R, S, D, E, T> metaData, boolean isCheckNextBundleViaExtraDocument,
			CabinetManager cabinetManager) {
		this.metaData = metaData;
		this.isCheckNextBundleViaExtraDocument = isCheckNextBundleViaExtraDocument;
		this.cabinetManager = cabinetManager;
	}

	/**
	 * Obtains the key name.
	 * 
	 * @return Key name.
	 */
	public String getKeyName() {
		return this.metaData.getKeyName();
	}

	/**
	 * Deserialises the next {@link DocumentBundle} token.
	 * 
	 * @param nextDocumentToken Next {@link DocumentBundle} token.
	 * @return Deserialised {@link Map} of values.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, String> deserialiseNextDocumentToken(String nextDocumentToken) {
		Map values;
		try {
			values = mapper.readValue(nextDocumentToken, Map.class);
		} catch (Exception ex) {

			// TODO log failure to deserialise next document token

			values = null;
		}
		return values;
	}

	/**
	 * Obtains the deserialised field value for the {@link InternalDocument}.
	 * 
	 * @param fieldName Name of the field.
	 * @param value     Serialised value.
	 * @return Deserialised field value.
	 */
	public Object getDeserialisedFieldValue(String fieldName, String serialisedValue) {
		return this.metaData.deserialisedFieldValue(fieldName, serialisedValue);
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
	protected abstract InternalDocumentBundle<R> retrieveInternalDocuments(Query query, InternalRange range);

	/**
	 * Stores the {@link InternalDocument} instances.
	 * 
	 * @param internalDocuments {@link InternalDocument} instances to store.
	 * @param transaction       Transaction with data store.
	 */
	public abstract void storeInternalDocuments(List<InternalDocument<S>> internalDocuments, T transaction);

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
				document = this.metaData.createManagedDocument(internalDocument, new ManagedDocumentState(),
						this.cabinetManager);

				// Capture in session
				this.session.put(key, document);
			}
		}

		// Return the document
		return document != null ? Optional.of(document) : Optional.empty();
	}

	@Override
	public DocumentBundle<D> retrieveByQuery(Query query, Range range) {

		// Retrieve the internal documents
		InternalRange internalRange;
		int bundleLimit;
		if (range == null) {
			// No range
			internalRange = null;
			bundleLimit = -1;

		} else {
			// +1 on limit to know if have all documents (avoid getting empty next bundle)
			bundleLimit = range.getLimit();
			int limit = (isCheckNextBundleViaExtraDocument && (bundleLimit > 0)) ? bundleLimit + 1 : bundleLimit;

			// Create the initial range
			internalRange = new InternalRange(range.getFieldName(), range.getDirection(), limit,
					range.getNextDocumentBundleToken(), this);
		}
		InternalDocumentBundle<R> internalDocumentBundle = this.retrieveInternalDocuments(query, internalRange);

		// Determine if document bundle
		if (internalDocumentBundle == null) {
			return null; // no document bundle
		}

		// Return the document bundle
		return new DocumentBundleWrapper(internalDocumentBundle, bundleLimit, query, internalRange);
	}

	@Override
	public void store(D document) {

		// Obtain the key for the document
		String key = this.metaData.getOrLoadDocumentKey(document);

		// Update session with document
		this.session.put(key, document);
	}

	/**
	 * Flushes the {@link Document} instances into the {@link CabinetManagerChange}.
	 * 
	 * @param change {@link CabinetManagerChange}.
	 */
	public void flush(CabinetManagerChange<T> change) {

		// Create the internal documents
		List<InternalDocument<S>> internalDocuments = new ArrayList<>(this.session.size());
		for (D document : this.session.values()) {

			// Create internal document to store
			InternalDocument<S> internalDocument = this.metaData.createInternalDocument(document, change);

			// Add the internal document
			internalDocuments.add(internalDocument);
		}

		// Store the changes
		T transaction = change.getTransaction();
		this.storeInternalDocuments(internalDocuments, transaction);
	}

	public void flushDocument(D document, CabinetManagerChange<T> change) {

		// Determine if already flushed document
		String key = this.metaData.getOrLoadDocumentKey(document);
		if (this.session.containsKey(key)) {
			return; // already flushed document
		}

		// Register document as flushed
		this.session.put(key, document);

		// Create internal document to store
		InternalDocument<S> internalDocument = this.metaData.createInternalDocument(document, change);

		// Store the change
		T transaction = change.getTransaction();
		this.storeInternalDocuments(Arrays.asList(internalDocument), transaction);
	}

	/**
	 * Caches the {@link InternalDocument} instances for the {@link DocumentBundle}.
	 */
	private class CacheDocumentBundleIterator {

		/**
		 * {@link InternalDocumentBundle}.
		 */
		private final InternalDocumentBundle<R> internalBundle;

		/**
		 * Limit.
		 */
		private final int limit;

		/**
		 * {@link Query}.
		 */
		private final Query query;

		/**
		 * {@link InternalRange}.
		 */
		private final InternalRange range;

		/**
		 * Cache of the {@link InternalDocument} instances.
		 */
		private final R[] cache;

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
		 * @param query          {@link Query}.
		 */
		@SuppressWarnings("unchecked")
		private CacheDocumentBundleIterator(InternalDocumentBundle<R> internalBundle, int limit, Query query,
				InternalRange range) {
			this.internalBundle = internalBundle;
			this.limit = limit;
			this.query = query;
			this.range = range;

			// Create cache to size
			this.cache = (this.limit > 0) ? (R[]) new Object[this.limit] : null;
		}

		/**
		 * Indicates has next for particular index.
		 * 
		 * @return <code>true</code> if has next for index.
		 */
		public boolean hasNext(int index) {

			// Determine if reached limit
			if (this.limit > 0) {

				// Determine if already iterated past
				if (index < this.iterated) {
					return true; // already iterated
				}

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

		/**
		 * Obtains next {@link Document}.
		 * 
		 * @param index
		 * @return
		 */
		public R next(int index) {

			// Determine if cached
			if (this.cache != null) {
				R entry = this.cache[index];
				if (entry != null) {
					return entry;
				}
			}

			// Obtain the next internal document
			R internalDocument = this.internalBundle.next();

			// Keep track of last document
			this.lastInternalDocument = internalDocument;

			// Possibly cache the entry
			if (this.cache != null) {
				this.cache[index] = internalDocument;
			}

			// Return the internal document
			return internalDocument;
		}
	}

	/**
	 * {@link Iterator} over the {@link Document} instances for the
	 * {@link DocumentBundle}.
	 */
	private class DocumentBundleIterator implements Iterator<D> {

		/**
		 * {@link CacheDocumentBundleIterator}.
		 */
		private final CacheDocumentBundleIterator cacheIterator;

		/**
		 * Current index.
		 */
		private int index = 0;

		/**
		 * Instantiate.
		 * 
		 * @param cacheIterator {@link CacheDocumentBundleIterator}.
		 */
		private DocumentBundleIterator(AbstractOfficeCabinet<R, S, D, E, T>.CacheDocumentBundleIterator cacheIterator) {
			this.cacheIterator = cacheIterator;
		}

		/*
		 * ======================= Iterator ============================
		 */

		@Override
		public boolean hasNext() {
			return this.cacheIterator.hasNext(this.index);
		}

		@Override
		public D next() {

			// Easy access to cabinet
			@SuppressWarnings("resource")
			AbstractOfficeCabinet<R, S, D, E, T> cabinet = AbstractOfficeCabinet.this;

			// Obtain the next internal document
			R internalDocument = this.cacheIterator.next(this.index++);

			// Obtain the key for the internal document
			String key = cabinet.metaData.getInternalDocumentKey(internalDocument);

			// Determine if in session
			D document = cabinet.session.get(key);
			if (document == null) {

				// Not in session, so load document
				document = cabinet.metaData.createManagedDocument(internalDocument, new ManagedDocumentState(),
						cabinet.cabinetManager);

				// Capture in session
				cabinet.session.put(key, document);
			}

			// Return the document
			return document;
		}
	}

	/**
	 * Wrapper of raw {@link Document} to {@link Document}.
	 */
	private class DocumentBundleWrapper implements DocumentBundle<D> {

		/**
		 * {@link DocumentBundleIterator}.
		 */
		private final DocumentBundleIterator iterator;

		/**
		 * Instantiate.
		 * 
		 * @param internalBundle {@link InternalDocumentBundle}.
		 * @param limit          Limit for this {@link DocumentBundleWrapper}.
		 * @param query          {@link Query}.
		 * @param range          {@link InternalRange}.
		 */
		private DocumentBundleWrapper(InternalDocumentBundle<R> internalBundle, int limit, Query query,
				InternalRange range) {
			CacheDocumentBundleIterator cacheIterator = new CacheDocumentBundleIterator(internalBundle, limit, query,
					range);
			this.iterator = new DocumentBundleIterator(cacheIterator);
		}

		/*
		 * ================== DocumentBundle ====================
		 */

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public D next() {
			return this.iterator.next();
		}

		@Override
		public Iterator<D> iterator() {
			return new DocumentBundleIterator(this.iterator.cacheIterator);
		}

		@Override
		public DocumentBundle<D> nextDocumentBundle() {

			// Easy access to cabinet
			@SuppressWarnings("resource")
			AbstractOfficeCabinet<R, S, D, E, T> cabinet = AbstractOfficeCabinet.this;

			// Obtain the last internal document
			R lastInternalDocument = this.getLastInternalDocument();
			if (lastInternalDocument == null) {
				// No start after document, so no further bundles
				return null;
			}

			// Create start after document value getter
			D document = cabinet.metaData.createManagedDocument(lastInternalDocument, null, cabinet.cabinetManager);
			StartAfterDocumentValueGetter startAfterDocumentValueGetter = cabinet.metaData
					.createStartAfterDocumentValueGetter(document);

			// Obtain the cache iterator
			CacheDocumentBundleIterator cacheIterator = this.iterator.cacheIterator;

			// Obtain the next document bundle
			NextDocumentBundleContext nextDocumentBundleContext = new NextDocumentBundleContext() {

				@Override
				public StartAfterDocumentValueGetter getStartAfterDocumentValueGetter() {
					return startAfterDocumentValueGetter;
				}

				@Override
				public String getNextDocumentBundleToken() {
					return DocumentBundleWrapper.this.getNextDocumentBundleToken();
				}
			};
			InternalDocumentBundle<R> nextInternalBundle = cacheIterator.internalBundle
					.nextDocumentBundle(nextDocumentBundleContext);
			return nextInternalBundle != null
					? new DocumentBundleWrapper(nextInternalBundle, cacheIterator.limit, cacheIterator.query,
							cacheIterator.range)
					: null;
		}

		@Override
		public String getNextDocumentBundleToken() {

			// Easy access to cabinet
			@SuppressWarnings("resource")
			AbstractOfficeCabinet<R, S, D, E, T> cabinet = AbstractOfficeCabinet.this;

			// Obtain the cache iterator
			CacheDocumentBundleIterator cacheIterator = this.iterator.cacheIterator;

			// Obtain the last document
			R lastInternalDocument = this.getLastInternalDocument();

			// Return the next document bundle token
			NextDocumentBundleTokenContext<R> nextDocumentBundleTokenContext = new NextDocumentBundleTokenContext<R>() {

				@Override
				public R getLastInternalDocument() {
					return lastInternalDocument;
				}

				@Override
				public String getLastInternalDocumentToken() {

					// Ensure have last internal document token
					if (lastInternalDocument == null) {
						return null;
					}

					// Capture all values for query
					Map<String, String> serialisedQueryValues = new HashMap<>(cacheIterator.query.getFields().length);

					// Obtain all values for query
					for (QueryField field : cacheIterator.query.getFields()) {
						String fieldName = field.fieldName;

						// Obtain the serialised field value
						String value = cabinet.metaData.serialisedFieldValue(fieldName, lastInternalDocument);

						// Capture the value
						serialisedQueryValues.put(fieldName, value);
					}

					// Include possible sort key
					if (cacheIterator.range != null) {
						String sortFieldName = cacheIterator.range.getFieldName();

						// Obtain the serialised field value
						String value = cabinet.metaData.serialisedFieldValue(sortFieldName, lastInternalDocument);

						// Capture the value
						serialisedQueryValues.put(sortFieldName, value);
					}

					// Include the key
					String keyName = cabinet.metaData.getKeyName();
					String keyValue = cabinet.metaData.getInternalDocumentKey(lastInternalDocument);
					serialisedQueryValues.put(keyName, keyValue);

					// Return the token
					try {
						return mapper.writeValueAsString(serialisedQueryValues);
					} catch (JsonProcessingException ex) {
						// Should never fail on string values
						throw new IllegalStateException("Failed to serialise values into token", ex);
					}
				}
			};
			return cacheIterator.internalBundle.getNextDocumentBundleToken(nextDocumentBundleTokenContext);
		}

		/**
		 * Obtains the last {@link InternalDocument} of {@link InternalDocumentBundle}.
		 * 
		 * @return Last {@link InternalDocument} of {@link InternalDocumentBundle}.
		 */
		private R getLastInternalDocument() {

			// Easy access to cabinet
			@SuppressWarnings("resource")
			AbstractOfficeCabinet<R, S, D, E, T> cabinet = AbstractOfficeCabinet.this;

			// Consume all the documents
			while (this.hasNext()) {
				this.next();
			}

			// Obtain the cache iterator
			CacheDocumentBundleIterator cacheIterator = this.iterator.cacheIterator;

			// Determine if further bundles
			if ((cacheIterator.limit <= 0)
					|| (cabinet.isCheckNextBundleViaExtraDocument && !cacheIterator.internalBundle.hasNext())) {
				return null; // no further document bundles
			}

			// Return the last document
			return cacheIterator.lastInternalDocument;
		}
	}

}