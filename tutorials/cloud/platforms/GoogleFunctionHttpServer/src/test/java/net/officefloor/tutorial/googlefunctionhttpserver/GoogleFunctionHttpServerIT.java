package net.officefloor.tutorial.googlefunctionhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;

import net.officefloor.nosql.firestore.test.AbstractFirestoreConnectJunit.Configuration;
import net.officefloor.nosql.firestore.test.FirestoreConnectExtension;
import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.tutorial.googlefunctionhttpserver.GoogleFunctionLogic.Post;

/**
 * Tests the Google Function HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class GoogleFunctionHttpServerIT {

	// START SNIPPET: tutorial
	public static final @RegisterExtension HttpClientExtension serverClient = new HttpClientExtension(false, 8381)
			.timeout(30_000);

	public static final @Order(1) @RegisterExtension FirestoreConnectExtension firestore = new FirestoreConnectExtension(
			new Configuration().port(8383));

	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void createPost() throws Exception {

		// Create the entity
		HttpPost request = new HttpPost(serverClient.url("/posts"));
		request.setHeader("Content-Type", "application/json");
		request.setEntity(new StringEntity(mapper.writeValueAsString(new Post("TEST"))));
		HttpResponse response = serverClient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseBody);
		PostEntity entity = mapper.readValue(responseBody, PostEntity.class);

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
		HttpGet request = new HttpGet(serverClient.url("/posts"));
		request.setHeader("Accept", "application/json");
		HttpResponse response = serverClient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseBody);
		PostEntity[] retrieved = mapper.readValue(responseBody, PostEntity[].class);

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
		PostEntity entity = new PostEntity(docRef.getId(), "TEST");
		docRef.create(entity).get();

		// Obtain the entity
		HttpGet request = new HttpGet(serverClient.url("/posts/" + entity.getId()));
		request.setHeader("Accept", "application/json");
		HttpResponse response = serverClient.execute(request);
		String responseBody = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseBody);
		PostEntity retrieved = mapper.readValue(responseBody, PostEntity.class);

		// Ensure correct
		assertEquals("TEST", retrieved.getMessage(), "Incorrect entity");
	}

	@Test
	public void index() throws Exception {
		HttpGet request = new HttpGet(serverClient.url("/"));
		request.addHeader("Accept", "text/html");
		HttpResponse response = serverClient.execute(request);
		String html = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + html);
		assertTrue(html.contains("<title>GoogleFunctionHttpServer</title>"), "Should obtain index.html");
	}
}