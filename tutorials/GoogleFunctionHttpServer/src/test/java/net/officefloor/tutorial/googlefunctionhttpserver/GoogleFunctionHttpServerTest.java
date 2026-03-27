package net.officefloor.tutorial.googlefunctionhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.firestore.DocumentReference;

import net.officefloor.nosql.firestore.test.FirestoreExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.tutorial.googlefunctionhttpserver.GoogleFunctionLogic.Post;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the Google Function HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class GoogleFunctionHttpServerTest {

	// START SNIPPET: tutorial
	public static final @Order(1) @RegisterExtension FirestoreExtension firestore = new FirestoreExtension();

	public static final @Order(2) @RegisterExtension MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void createPost() throws Exception {

		// Create the entity
		MockWoofResponse response = server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/posts", new Post("TEST")));
		PostEntity entity = response.getJson(200, PostEntity.class);

		// Ensure in store
		PostEntity post = firestore.getFirestore().collection(PostEntity.class.getSimpleName()).document(entity.getId())
				.get().get().toObject(PostEntity.class);
		assertNotNull(post, "Should find entity in Firestore " + entity.getId());
		assertEquals("TEST", post.getMessage(), "Incorrent entity");
	}
	// END SNIPPET: tutorial

	@Test
	public void getPosts() throws Exception {

		// Create the entity
		DocumentReference docRef = firestore.getFirestore().collection(PostEntity.class.getSimpleName()).document();
		docRef.create(new PostEntity(docRef.getId(), "LIST")).get();

		// Obtain the entities
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/posts"));
		PostEntity[] retrieved = response.getJson(200, PostEntity[].class);

		// Ensure correct
		assertTrue(retrieved.length > 0, "Should have posts");
		PostEntity list = null;
		for (PostEntity post : retrieved) {
			if ("LIST".equals(post.getMessage())) {
				list = post;
			}
		}
		assertNotNull(list, "Should find LIST post");
	}

	@Test
	public void getPost() throws Exception {

		// Create the entity
		DocumentReference docRef = firestore.getFirestore().collection(PostEntity.class.getSimpleName()).document();
		docRef.create(new PostEntity(docRef.getId(), "TEST")).get();

		// Obtain the entity
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/posts/" + docRef.getId()));
		PostEntity retrieved = response.getJson(200, PostEntity.class);
		assertEquals("TEST", retrieved.getMessage(), "Incorrect entity");
	}

	@Test
	public void index() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest());
		response.assertStatus(200);
		assertTrue(response.getEntity(null).contains("<title>GoogleFunctionHttpServer</title>"),
				"Should obtain index.html");
	}
}