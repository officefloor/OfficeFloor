package net.officefloor.tutorial.objectifyhttpserver;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.objectify.Objectify;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.SkipRule;
import net.officefloor.nosql.objectify.mock.ObjectifyRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link Objectify} HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyHttpServerTest {

	@ClassRule
	public static SkipRule ensureGCloudAvailable = new SkipRule(OfficeFrameTestCase.isSkipTestsUsingGCloud(),
			"GCloud not available");

	// START SNIPPET: tutorial
	private ObjectifyRule objectify = new ObjectifyRule();

	private MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public RuleChain ordered = RuleChain.outerRule(this.objectify).around(this.server);

	private static ObjectMapper mapper = new ObjectMapper();

	@Test
	public void ensureCreatePost() throws Exception {

		// Have server create the post
		Post post = new Post(null, "TEST");
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/posts").method(HttpMethod.POST)
				.header("Content-Type", "application/json").entity(mapper.writeValueAsString(post)));
		response.assertResponse(204, "");

		// Ensure post created
		Post created = this.objectify.get(Post.class);
		assertEquals("Incorrect post", "TEST", created.getMessage());
	}
	// END SNIPPET: tutorial

	@Test
	public void ensureRetrievePost() throws Exception {

		// Create the post
		Post post = new Post();
		this.objectify.ofy().save().entities(post).now();

		// Obtain the identifier
		post = this.objectify.get(Post.class);

		// Ensure retrieve the post
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/posts/" + post.getId()));
		response.assertResponse(200, mapper.writeValueAsString(post), "Content-Type", "application/json");
	}

	@Test
	public void ensureRetrieveAllPosts() throws Exception {

		// Create the post
		Post post = new Post();
		this.objectify.ofy().save().entities(post).now();

		// Obtain the identifier
		post = this.objectify.get(Post.class);

		// Ensure retrieve the post
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/posts/"));
		response.assertResponse(200, mapper.writeValueAsString(new Post[] { post }), "Content-Type",
				"application/json");
	}

}