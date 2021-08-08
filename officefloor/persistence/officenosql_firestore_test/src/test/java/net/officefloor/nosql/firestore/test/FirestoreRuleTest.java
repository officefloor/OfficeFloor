package net.officefloor.nosql.firestore.test;

import org.junit.Rule;
import org.junit.Test;

import com.google.cloud.firestore.Firestore;

import net.officefloor.test.skip.SkipJUnit4;

/**
 * Tests the {@link FirestoreRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreRuleTest extends AbstractFirestoreTestCase {

	public final @Rule FirestoreRule firestore = new FirestoreRule();

	@Test
	public void firestore() throws Exception {
		SkipJUnit4.skipDocker();
		Firestore firestore = this.firestore.getFirestore();
		this.doTest(firestore);
	}
}
