package net.officefloor.nosql.firestore.test;

import org.junit.jupiter.api.extension.RegisterExtension;

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
		this.doTest(this.firestore.getFirestore());
	}

}
