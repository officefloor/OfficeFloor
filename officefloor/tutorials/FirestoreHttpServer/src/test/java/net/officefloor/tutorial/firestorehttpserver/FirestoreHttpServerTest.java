package net.officefloor.tutorial.firestorehttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import net.officefloor.nosql.firestore.test.FirestoreExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the {@link Firestore} HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class FirestoreHttpServerTest {

	// START SNIPPET: tutorial
	@Order(1)
	@RegisterExtension
	public final FirestoreExtension firestore = new FirestoreExtension();

	@Order(2)
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void ensureCreatePost() throws Exception {

		// Have server create the post
		Post post = new Post(null, "TEST");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/posts", post));
		response.assertResponse(204, "");

		// Ensure post created
		Post[] created = firestore.getFirestore().collection(Post.class.getSimpleName()).get().get().getDocuments()
				.stream().map((document) -> document.toObject(Post.class)).toArray(Post[]::new);
		assertEquals(1, created.length, "Should only be one created post");
		assertEquals("TEST", created[0].getMessage(), "Incorrect post");
	}
	// END SNIPPET: tutorial

	@Test
	public void ensureRetrievePost() throws Exception {

		// Create the post
		DocumentReference docRef = firestore.getFirestore().collection(Post.class.getSimpleName()).document();
		Post post = new Post(docRef.getId(), "TEST");
		docRef.create(post).get();

		// Ensure retrieve the post
		MockWoofResponse response = this.server.send(MockHttpServer.mockRequest("/posts/" + post.getId()));
		response.assertJson(200, post);
	}

	@Test
	public void ensureRetrieveAllPosts() throws Exception {

		// Create the post
		DocumentReference docRef = firestore.getFirestore().collection(Post.class.getSimpleName()).document();
		Post post = new Post(docRef.getId(), "TEST");
		docRef.create(post).get();

		// Ensure retrieve the post
		MockWoofResponse response = this.server.send(MockHttpServer.mockRequest("/posts/"));
		response.assertJson(200, new Post[] { post });
	}

}