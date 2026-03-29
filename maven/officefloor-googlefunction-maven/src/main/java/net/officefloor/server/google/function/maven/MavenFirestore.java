package net.officefloor.server.google.function.maven;

import com.google.cloud.firestore.Firestore;

import net.officefloor.nosql.firestore.test.AbstractFirestoreJunit;

/**
 * Run {@link Firestore} for maven.
 * 
 * @author Daniel Sagenschneider
 */
public class MavenFirestore extends AbstractFirestoreJunit<MavenFirestore> {

	/**
	 * Instantiate.
	 * 
	 * @param firestorePort {@link Firestore} port.
	 */
	public MavenFirestore(int firestorePort) {
		super(new Configuration().port(firestorePort));
	}

	/*
	 * ================= AbstractFirestoreJunit ====================
	 */

	@Override
	public void startFirestore() throws Exception {
		super.startFirestore();
	}

	@Override
	public void stopFirestore() throws Exception {
		super.stopFirestore();
	}

}
