package net.officefloor.cabinet.firestore;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.OfficeCabinet;

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