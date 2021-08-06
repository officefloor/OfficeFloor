package net.officefloor.tutorial.firestorehttpserver;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.google.cloud.firestore.Firestore;

import net.officefloor.nosql.firestore.test.FirestoreRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.test.SkipUtil;
import net.officefloor.test.skip.SkipRule;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link Firestore} HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreHttpServerJUnit4Test {

	@ClassRule
	public static SkipRule ensureDockerAvailable = new SkipRule(SkipUtil.isSkipTestsUsingDocker(),
			"Docker not available");

	// START SNIPPET: tutorial
	private final FirestoreRule firestore = new FirestoreRule();

	private final MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public final RuleChain ordered = RuleChain.outerRule(this.firestore).around(this.server);

	@Test
	public void ensureCreatePost() throws Exception {

		// Have server create the post
		Post post = new Post(null, "TEST");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/posts", post));
		response.assertResponse(204, "");

		// Ensure post created
		Post[] created = firestore.getFirestore().collection(Post.class.getSimpleName()).get().get().getDocuments()
				.stream().map((document) -> document.toObject(Post.class)).toArray(Post[]::new);
		assertEquals("Should only be one created post", 1, created.length);
		assertEquals("Incorrect post", "TEST", created[0].getMessage());
	}
	// END SNIPPET: tutorial

}