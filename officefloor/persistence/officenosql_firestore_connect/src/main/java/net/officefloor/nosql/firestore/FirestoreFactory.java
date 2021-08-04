package net.officefloor.nosql.firestore;

import com.google.cloud.firestore.Firestore;

/**
 * Factory for {@link Firestore} connection.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FirestoreFactory {

	/**
	 * Creates the {@link Firestore}.
	 * 
	 * @return {@link Firestore}.
	 * @throws Exception If fails to create {@link Firestore}.
	 */
	Firestore createFirestore() throws Exception;

}
