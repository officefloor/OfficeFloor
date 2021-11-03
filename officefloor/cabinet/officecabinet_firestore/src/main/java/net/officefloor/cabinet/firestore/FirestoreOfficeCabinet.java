package net.officefloor.cabinet.firestore;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import net.officefloor.cabinet.common.AbstractOfficeCabinet;
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
	protected Iterator<DocumentSnapshot> retrieveInternalDocuments(Query query, Range<D> range) {

		// TODO handle multiple fields
		String fieldName = query.getFields()[0].fieldName;
		Object fieldValue = query.getFields()[0].fieldValue;

		try {
			com.google.cloud.firestore.Query firestoreQuery = this.metaData.firestore
					.collection(this.metaData.collectionId).whereEqualTo(FieldPath.of(fieldName), fieldValue);
			if (range != null) {
				firestoreQuery = firestoreQuery.orderBy(FieldPath.of(range.getFieldName()),
						range.getDirection() == net.officefloor.cabinet.spi.Range.Direction.Ascending
								? Direction.ASCENDING
								: Direction.DESCENDING);
			}

			QuerySnapshot result = firestoreQuery.get().get();
			Iterator<QueryDocumentSnapshot> iterator = result.getDocuments().iterator();
			return new Iterator<DocumentSnapshot>() {

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public DocumentSnapshot next() {
					return iterator.next();
				}
			};
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to obtain document " + this.metaData.documentType.getName() + " by query " + query, ex);
		}
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

}