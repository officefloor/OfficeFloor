package net.officefloor.nosql.firestore.test;

import org.junit.ClassRule;
import org.junit.Test;

import net.officefloor.test.skip.SkipJUnit4;

/**
 * Tests the {@link FirestoreRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreRuleTest extends AbstractFirestoreTestCase {

	public static final @ClassRule FirestoreRule firestore = new FirestoreRule();

	@Test
	public void firestore() throws Exception {
		SkipJUnit4.skipDocker();
		this.doTest(firestore.getFirestore());
	}
}
