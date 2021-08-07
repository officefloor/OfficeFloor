package net.officefloor.nosql.firestore.test;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.firestore.Firestore;

import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link FirestoreExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreExtensionTest extends AbstractFirestoreTestCase {

	public final @RegisterExtension FirestoreExtension firestore = new FirestoreExtension();

	@UsesDockerTest
	public void firestore() throws Exception {
		Firestore firestore = this.firestore.getFirestore();
		this.doTest(firestore);
	}

}
