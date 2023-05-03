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
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Range.Direction;

/**
 * Mock {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockOfficeCabinet<D> extends
		AbstractOfficeCabinet<Map<String, Object>, Map<String, Object>, D, Map<String, Map<String, Object>>, MockTransaction> {

	private static <D> MockInternalDocumentBundle<D> createMockInternalDocumentBundle(
			Map<String, Map<String, Object>> documents, Query query, InternalRange range,
			DocumentMetaData<Map<String, Object>, Map<String, Object>, D, Map<String, Map<String, Object>>, MockTransaction> metaData) {

		// Obtain entries into a list
		List<Map<String, Object>> sortedDocuments = new LinkedList<>(documents.values());

		// Remaining documents (if limiting)
		Map<String, Map<String, Object>> remainingDocuments = new HashMap<>();

		// Apply possible range
		if (range != null) {

			// Provide appropriate sorting
			String sortFieldName = range.getFieldName();
			sortedDocuments.sort((a, b) -> {

				// Obtain the values
				Object aSortValue = a.get(sortFieldName);
				Object bSortValue = b.get(sortFieldName);

				// Provide sorting based on type
				Class<?> fieldType = aSortValue.getClass();
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
				Iterator<Map<String, Object>> sortedDocumentIterator = sortedDocuments.iterator();
				FOUND_KEY: while (sortedDocumentIterator.hasNext()) {

					// Remove the document (as key is for last bundle)
					Map<String, Object> document = sortedDocumentIterator.next();
					sortedDocumentIterator.remove();

					// Determine if found key
					String documentKey = metaData.getInternalDocumentKey(document);
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
					Map<String, Object> document = sortedDocuments.get(i);
					String key = metaData.getInternalDocumentKey(document);
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
	private static class MockInternalDocumentBundle<D> implements InternalDocumentBundle<Map<String, Object>> {

		private final Iterator<Map<String, Object>> bundleDocuments;

		private final Query query;

		private final InternalRange range;

		private final Map<String, Map<String, Object>> remainingDocuments;

		private final DocumentMetaData<Map<String, Object>, Map<String, Object>, D, Map<String, Map<String, Object>>, MockTransaction> metaData;

		private MockInternalDocumentBundle(Query query, InternalRange range,
				Iterator<Map<String, Object>> bundleDocuments, Map<String, Map<String, Object>> remainingDocuments,
				DocumentMetaData<Map<String, Object>, Map<String, Object>, D, Map<String, Map<String, Object>>, MockTransaction> metaData) {
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
		public Map<String, Object> next() {
			return this.bundleDocuments.next();
		}

		@Override
		public InternalDocumentBundle<Map<String, Object>> nextDocumentBundle(NextDocumentBundleContext context) {
			return ((this.remainingDocuments != null) && (this.remainingDocuments.size() > 0))
					? createMockInternalDocumentBundle(this.remainingDocuments, this.query, this.range, this.metaData)
					: null;
		}

		@Override
		public String getNextDocumentBundleToken(NextDocumentBundleTokenContext<Map<String, Object>> context) {
			return context.getLastInternalDocumentToken();
		}
	}

	/**
	 * Instantiate.
	 * 
	 * @param metaData       {@link MockDocumentMetaData}.
	 * @param cabinetManager {@link CabinetManager}.
	 */
	public MockOfficeCabinet(
			DocumentMetaData<Map<String, Object>, Map<String, Object>, D, Map<String, Map<String, Object>>, MockTransaction> metaData,
			CabinetManager cabinetManager) {
		super(metaData, false, cabinetManager);
	}

	/*
	 * ===================== AbstractOfficeCabinet =======================
	 */

	@Override
	protected Map<String, Object> retrieveInternalDocument(String key) {
		Map<String, Object> document = this.metaData.extra.get(key);
		return document;
	}

	@Override
	protected InternalDocumentBundle<Map<String, Object>> retrieveInternalDocuments(Query query, InternalRange range) {
		return createMockInternalDocumentBundle(this.metaData.extra, query, range, this.metaData);
	}

	@Override
	public void storeInternalDocuments(List<InternalDocument<Map<String, Object>>> internalDocuments,
			MockTransaction transaction) {
		transaction.add(internalDocuments, this.metaData);
	}

}
