package net.officefloor.nosql.firestore.test;

import com.google.cloud.firestore.Firestore;

/**
 * {@link RuntimeException} for {@link Firestore} timing out to start up.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreStartTimeoutException extends RuntimeException {

	/**
	 * Initiate.
	 * 
	 * @param message Timeout message.
	 * @param cause   Possible error causing timeout.
	 */
	public FirestoreStartTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
}