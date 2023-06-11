package net.officefloor.cabinet.firestore;

import java.util.List;
import java.util.Map;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;

import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * {@link Transaction} for {@link Firestore}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreTransaction {

	/**
	 * {@link Firestore}.
	 */
	private final Firestore firestore;

	/**
	 * {@link Transaction}.
	 */
	private final Transaction transaction;

	/**
	 * Instantiate.
	 * 
	 * @param firestore   {@link Firestore}.
	 * @param transaction {@link Transaction}.
	 */
	public FirestoreTransaction(Firestore firestore, Transaction transaction) {
		this.firestore = firestore;
		this.transaction = transaction;
	}

	/**
	 * Adds documents to the {@link Transaction}.
	 * 
	 * @param collectionId      Collection Id for the {@link InternalDocument}
	 *                          instances.
	 * @param internalDocuments {@link InternalDocument} instances.
	 */
	public void add(String collectionId, List<InternalDocument<Map<String, Object>>> internalDocuments) {
		for (InternalDocument<Map<String, Object>> internalDocument : internalDocuments) {
			String key = internalDocument.getKey();
			DocumentReference docRef = this.firestore.collection(collectionId).document(key);
			Map<String, Object> fields = internalDocument.getInternalDocument();
			if (internalDocument.isNew()) {
				this.transaction.create(docRef, fields);
			} else {
				this.transaction.set(docRef, fields);
			}
		}
	}

}
