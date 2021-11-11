package net.officefloor.cabinet.firestore;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QuerySnapshot;

import net.officefloor.cabinet.DocumentBundle;
import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.InternalDocumentBundle;
import net.officefloor.cabinet.common.adapt.InternalRange;
import net.officefloor.cabinet.common.adapt.StartAfterDocumentValueGetter;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Range;

/**
 * {@link Firestore} {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreOfficeCabinet<D>
		extends AbstractOfficeCabinet<DocumentSnapshot, Map<String, Object>, D, FirestoreDocumentMetaData<D>> {

	/**
	 * Instantiate.
	 * 
	 * @param metaData {@link FirestoreDocumentMetaData}.
	 */
	public FirestoreOfficeCabinet(FirestoreDocumentMetaData<D> metaData) {
		super(metaData);
	}

	/**
	 * Undertakes the {@link Query}.
	 * 
	 * @param query                         {@link Query}.
	 * @param range                         {@link InternalRange}.
	 * @param startAfterDocumentValueGetter Start after {@link DocumentSnapshot}.
	 *                                      May be <code>null</cod> to start at
	 *                                      beginning.
	 * @return {@link FirestoreDocumentBundle} containing the {@link Query} results.
	 */
	private FirestoreDocumentBundle doQuery(Query query, InternalRange<DocumentSnapshot> range,
			StartAfterDocumentValueGetter startAfterDocumentValueGetter) {

		// TODO handle multiple fields
		String fieldName = query.getFields()[0].fieldName;
		Object fieldValue = query.getFields()[0].fieldValue;

		try {
			com.google.cloud.firestore.Query firestoreQuery = this.metaData.firestore
					.collection(this.metaData.collectionId).whereEqualTo(FieldPath.of(fieldName), fieldValue);
			if (range != null) {

				// Order by range
				String sortFieldName = range.getFieldName();
				firestoreQuery = firestoreQuery.orderBy(FieldPath.of(sortFieldName),
						range.getDirection() == net.officefloor.cabinet.spi.Range.Direction.Ascending
								? Direction.ASCENDING
								: Direction.DESCENDING);

				// Provide paging limit
				int limit = range.getLimit();
				boolean isOrderById = false;
				if (limit > 0) {
					firestoreQuery = firestoreQuery.limit(limit);

					// Always order by id to handle consistent pages
					firestoreQuery = firestoreQuery.orderBy(FieldPath.documentId());
					isOrderById = true;
				}

				// Determine if start after
				if (startAfterDocumentValueGetter != null) {

					// Obtain the order by field value
					Object rangeStartFromValue = startAfterDocumentValueGetter.getValue(sortFieldName);
					if (!isOrderById) {
						// Start after input document
						firestoreQuery = firestoreQuery.startAfter(rangeStartFromValue);
					} else {
						// Start next page
						String keyStartFromValue = startAfterDocumentValueGetter.getKey();
						firestoreQuery = firestoreQuery.startAfter(rangeStartFromValue, keyStartFromValue);
					}
				}
			}

			// Query for the documents
			QuerySnapshot result = firestoreQuery.get().get();
			return new FirestoreDocumentBundle(result.getDocuments().iterator(), query, range);

		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to obtain document " + this.metaData.documentType.getName() + " by query " + query, ex);
		}
	}

	/*
	 * =================== AbstractOfficeCabinet =======================
	 */

	@Override
	protected DocumentSnapshot retrieveInternalDocument(String key) {
		DocumentReference docRef = this.metaData.firestore.collection(this.metaData.collectionId).document(key);
		try {
			return docRef.get().get();
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to obtain document " + this.metaData.documentType.getName() + " by key " + key, ex);
		}
	}

	@Override
	protected InternalDocumentBundle<DocumentSnapshot> retrieveInternalDocuments(Query query,
			InternalRange<DocumentSnapshot> range) {
		StartAfterDocumentValueGetter startAfterDocumentValueGetter = range != null
				? range.getStartAfterDocumentValueGetter()
				: null;
		return this.doQuery(query, range, startAfterDocumentValueGetter);
	}

	@Override
	protected void storeInternalDocument(InternalDocument<Map<String, Object>> internalDocument) {
		String key = internalDocument.getKey();
		try {
			DocumentReference docRef = this.metaData.firestore.collection(this.metaData.collectionId).document(key);
			Map<String, Object> fields = internalDocument.getInternalDocument();
			if (internalDocument.isNew()) {
				docRef.create(fields).get();
			} else {
				docRef.set(fields).get();
			}
		} catch (ExecutionException | InterruptedException ex) {
			throw new IllegalStateException(
					"Failed to store document " + this.metaData.documentType.getName() + " by key " + key, ex);
		}
	}

	/**
	 * {@link Firestore} {@link DocumentBundle}.
	 */
	private class FirestoreDocumentBundle implements InternalDocumentBundle<DocumentSnapshot> {

		/**
		 * {@link Iterator} over {@link InternalDocument} instances.
		 */
		private final Iterator<? extends DocumentSnapshot> iterator;

		/**
		 * {@link Query}.
		 */
		private final Query query;

		/**
		 * {@link InternalRange}.
		 */
		private final InternalRange<DocumentSnapshot> range;

		/**
		 * Instantiate.
		 * 
		 * @param iterator {@link Iterator} over {@link InternalDocument} instances.
		 * @param query    {@link Query}.
		 * @param range    {@link Range}.
		 */
		private FirestoreDocumentBundle(Iterator<? extends DocumentSnapshot> iterator, Query query,
				InternalRange<DocumentSnapshot> range) {
			this.iterator = iterator;
			this.query = query;
			this.range = range;
		}

		/*
		 * ================== DocumentBundle ====================
		 */

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public DocumentSnapshot next() {
			return this.iterator.next();
		}

		@Override
		public InternalDocumentBundle<DocumentSnapshot> nextDocumentBundle(
				StartAfterDocumentValueGetter startAfterDocumentValueGetter) {
			return FirestoreOfficeCabinet.this.doQuery(this.query, this.range, startAfterDocumentValueGetter);
		}
	}

}