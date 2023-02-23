package net.officefloor.cabinet.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.cabinet.common.adapt.InternalRange;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Range.Direction;

/**
 * Mock {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockOfficeCabinet<D> extends AbstractOfficeCabinet<D, D, D, Map<String, D>> {

	private static <D> MockInternalDocumentBundle<D> createMockInternalDocumentBundle(Map<String, D> documents,
			Query query, InternalRange range, DocumentMetaData<D, D, D, Map<String, D>> metaData) {

		// Obtain entries into a list
		List<D> sortedDocuments = new LinkedList<>(documents.values());

		// Remaining documents (if limiting)
		Map<String, D> remainingDocuments = new HashMap<>();

		// Apply possible range
		if (range != null) {

			// Provide appropriate sorting
			String sortFieldName = range.getFieldName();
			sortedDocuments.sort((a, b) -> {

				// Obtain the values
				Object aSortValue = MockDocumentAdapter.getValue(a, sortFieldName);
				Object bSortValue = MockDocumentAdapter.getValue(b, sortFieldName);

				// Provide sorting based on type
				Class<?> fieldType = MockDocumentAdapter.getField(a, sortFieldName).getType();
				if (Number.class.isAssignableFrom(fieldType) || fieldType.isPrimitive()) {
					// Sort numerically
					double difference = ((Number) aSortValue).doubleValue() - ((Number) bSortValue).doubleValue();
					return (difference == 0) ? 0 : (difference < 0) ? -1 : 1;

				} else {
					// Sort on string value
					return String.CASE_INSENSITIVE_ORDER.compare(aSortValue.toString(), bSortValue.toString());

				}
			});

			// Determine if sort descending
			if (Direction.Descending.equals(range.getDirection())) {
				Collections.reverse(sortedDocuments);
			}

			// Determine if next token
			String token = range.getNextDocumentBundleToken();
			if (token != null) {

				// Skip values up to key
				String key = range.getTokenKeyValue();
				Iterator<D> sortedDocumentIterator = sortedDocuments.iterator();
				FOUND_KEY: while (sortedDocumentIterator.hasNext()) {

					// Remove the document (as key is for last bundle)
					D document = sortedDocumentIterator.next();
					sortedDocumentIterator.remove();

					// Determine if found key
					String documentKey = metaData.getKey(document);
					if (key.equals(documentKey)) {
						break FOUND_KEY;
					}
				}
			}

			// Determine if limit results
			int limit = range.getLimit();
			if (limit > 0) {

				// Load up the remaining documents
				int sortedDocumentsSize = sortedDocuments.size();
				for (int i = limit; i < sortedDocumentsSize; i++) {
					D document = sortedDocuments.get(i);
					String key = metaData.getKey(document);
					remainingDocuments.put(key, document);
				}

				// Obtain the limited number of documents
				sortedDocuments = sortedDocuments.subList(0, limit < sortedDocumentsSize ? limit : sortedDocumentsSize);
			}
		}

		// Return the document bundle
		return sortedDocuments.size() > 0
				? new MockInternalDocumentBundle<>(query, range, sortedDocuments.iterator(), remainingDocuments,
						metaData)
				: null;
	}

	/**
	 * Mock {@link InternalDocumentBundle} for testing.
	 */
	private static class MockInternalDocumentBundle<D> implements InternalDocumentBundle<D> {

		private final Iterator<D> bundleDocuments;

		private final Query query;

		private final InternalRange range;

		private final Map<String, D> remainingDocuments;

		private final DocumentMetaData<D, D, D, Map<String, D>> metaData;

		private MockInternalDocumentBundle(Query query, InternalRange range, Iterator<D> bundleDocuments,
				Map<String, D> remainingDocuments, DocumentMetaData<D, D, D, Map<String, D>> metaData) {
			this.query = query;
			this.range = range;
			this.bundleDocuments = bundleDocuments;
			this.remainingDocuments = remainingDocuments;
			this.metaData = metaData;
		}

		/*
		 * ===================== InternalDocumentBundle =====================
		 */

		@Override
		public boolean hasNext() {
			return this.bundleDocuments.hasNext();
		}

		@Override
		public D next() {
			return this.bundleDocuments.next();
		}

		@Override
		public InternalDocumentBundle<D> nextDocumentBundle(NextDocumentBundleContext context) {
			return ((this.remainingDocuments != null) && (this.remainingDocuments.size() > 0))
					? createMockInternalDocumentBundle(this.remainingDocuments, this.query, this.range, this.metaData)
					: null;
		}

		@Override
		public String getNextDocumentBundleToken(NextDocumentBundleTokenContext<D> context) {
			return context.getLastInternalDocumentToken();
		}
	}

	/**
	 * Instantiate.
	 * 
	 * @param metaData {@link MockDocumentMetaData}.
	 */
	public MockOfficeCabinet(DocumentMetaData<D, D, D, Map<String, D>> metaData) {
		super(metaData, false);
	}

	/*
	 * ===================== AbstractOfficeCabinet =======================
	 */

	@Override
	protected D retrieveInternalDocument(String key) {
		D document = this.metaData.extra.get(key);
		return document;
	}

	@Override
	protected InternalDocumentBundle<D> retrieveInternalDocuments(Query query, InternalRange range) {
		return createMockInternalDocumentBundle(this.metaData.extra, query, range, this.metaData);
	}

	@Override
	protected void storeInternalDocument(InternalDocument<D> internalDocument) {
		@SuppressWarnings("resource")
		D document = internalDocument.getInternalDocument();
		String key = this.metaData.getKey(document);
		this.metaData.extra.put(key, document);
	}

}
