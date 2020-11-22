package net.officefloor.tutorial.googleappenginehttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.googlecode.objectify.Key;

import net.officefloor.nosql.objectify.mock.ObjectifyExtension;
import net.officefloor.test.UsesGCloudTest;
import net.officefloor.tutorial.googleappenginehttpserver.Logic.Message;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the GCP HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
@UsesGCloudTest
public class GoogleAppEngineHttpServerTest {

	@Order(1)
	@RegisterExtension
	public final ObjectifyExtension objectify = new ObjectifyExtension();

	@Order(2)
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void ensureGetDefaultResource() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/"));
		response.assertResponse(200, "<html><body>Hello from GCP</body></html>");
	}

	@Test
	public void ensureGetResource() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/index.html"));
		response.assertResponse(200, "<html><body>Hello from GCP</body></html>");
	}

	@Test
	public void ensureGetDirectoryDefaultResource() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/sub"));
		response.assertResponse(200, "<html><body>Hello from GCP sub directory</body></html>");
	}

	@Test
	public void ensureGetAsDirectoryDefaultResource() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/sub/"));
		response.assertResponse(200, "<html><body>Hello from GCP sub directory</body></html>");
	}

	@Test
	public void ensureGetDirectoryResource() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/sub/index.html"));
		response.assertResponse(200, "<html><body>Hello from GCP sub directory</body></html>");
	}

	@Test
	public void ensureRestEndPoint() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/rest"));
		response.assertJson(200, new Message("Hello from GCP"));
	}

	@Test
	public void ensureDatastoreEndPoint() throws Exception {

		// Save data to retrieve
		Post post = new Post(null, "TEST");
		Map<Key<Post>, Post> data = this.objectify.ofy().save().entities(post).now();
		assertEquals(1, data.size(), "Should persist data to retrieve");

		// Ensure able to retrieve the data
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/post/" + post.getId()));
		Post retrievedPost = response.getJson(200, Post.class);
		assertEquals(post.getMessage(), retrievedPost.getMessage(), "Incorrect retrieved post");
	}

	@Test
	public void ensureSecureEndPoint() throws Exception {

		// Ensure redirect on non-secure request
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/secure"));
		response.assertResponse(307, "", "location", "https://mock.officefloor.net/secure");

		// Ensure able to access on secure connection
		response = this.server.send(MockWoofServer.mockRequest("/secure").secure(true));
		response.assertJson(200, new Message("Secure hello from GCP"));
	}

}