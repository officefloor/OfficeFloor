package net.officefloor.tutorial.objectifyhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.googlecode.objectify.Objectify;

import net.officefloor.nosql.objectify.mock.ObjectifyExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.UsesGCloudTest;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the {@link Objectify} HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
@UsesGCloudTest
public class ObjectifyHttpServerTest {

	// START SNIPPET: tutorial
	@Order(1)
	@RegisterExtension
	public final ObjectifyExtension objectify = new ObjectifyExtension();

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
		Post created = this.objectify.get(Post.class);
		assertEquals("TEST", created.getMessage(), "Incorrect post");
	}
	// END SNIPPET: tutorial

	@Test
	public void ensureRetrievePost() throws Exception {

		// Create the post
		Post post = new Post(null, "TEST");
		this.objectify.ofy().save().entities(post).now();

		// Obtain the identifier
		post = this.objectify.get(Post.class);

		// Ensure retrieve the post
		MockWoofResponse response = this.server.send(MockHttpServer.mockRequest("/posts/" + post.getId()));
		response.assertJson(200, post);
	}

	@Test
	public void ensureRetrieveAllPosts() throws Exception {

		// Create the post
		Post post = new Post(null, "TEST");
		this.objectify.ofy().save().entities(post).now();

		// Obtain the identifier
		post = this.objectify.get(Post.class);

		// Ensure retrieve the post
		MockWoofResponse response = this.server.send(MockHttpServer.mockRequest("/posts/"));
		response.assertJson(200, new Post[] { post });
	}

}