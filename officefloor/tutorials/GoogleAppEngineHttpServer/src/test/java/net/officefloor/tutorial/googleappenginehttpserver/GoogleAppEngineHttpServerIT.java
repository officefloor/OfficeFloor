package net.officefloor.tutorial.googleappenginehttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.datastore.Datastore;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import net.officefloor.maven.IntegrationAppEngine;
import net.officefloor.server.http.HttpClientExtension;

/**
 * Tests the GCP HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleAppEngineHttpServerIT {

	// START SNIPPET: simple
	@RegisterExtension
	public final HttpClientExtension client = new HttpClientExtension(false, 8381);

	@Test
	public void ensureGetResource() throws Exception {
		this.doTest("/index.html", "<html><body>Hello from GCP</body></html>");
	}

	@Test
	public void ensureRestEndPoint() throws Exception {
		this.doTest("/rest", "{\"message\":\"Hello from GCP\"}");
	}

	private void doTest(String path, String entity) throws IOException {

		// Obtain the resource
		HttpGet get = new HttpGet(this.client.url(path));
		HttpResponse response = this.client.execute(get);

		// Ensure obtained resource
		String actualEntity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + actualEntity);
		assertEquals(entity, actualEntity);
	}
	// END SNIPPET: simple

	// START SNIPPET: datastore
	@Test
	public void ensureDatastoreEndPoint() throws Exception {

		// Obtain the datastore from integration setup
		Datastore datastore = IntegrationAppEngine.getDatastore();

		// Initialise datastore
		ObjectifyService.init(new ObjectifyFactory(datastore));
		ObjectifyService.register(Post.class);
		Post post = new Post(null, "TEST MESSAGE");
		Map<Key<Post>, Post> data = ObjectifyService.run(() -> {
			return ObjectifyService.ofy().save().entities(post).now();
		});
		assertEquals(1, data.size(), "Should persist data to retrieve");

		// Ensure able to retrieve the data
		HttpGet get = new HttpGet(this.client.url("/post/" + post.getId()));
		HttpResponse response = this.client.execute(get);

		// Ensure obtained resource
		String actualEntity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + actualEntity);
		Post retrievePost = new ObjectMapper().readValue(actualEntity, Post.class);
		assertEquals(post.getMessage(), retrievePost.getMessage(), "Incorrect post retrieved");
	}
	// END SNIPPET: datastore

	// START SNIPPET: secure
	@Test
	public void ensureSecureEndPoint() throws Exception {

		// Obtain the secure resource (made accessible for testing)
		HttpGet get = new HttpGet("http://localhost:8381/secure");
		HttpResponse response = this.client.execute(get);

		// Ensure obtained resource
		String actualEntity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + actualEntity);
		assertEquals("{\"message\":\"Secure hello from GCP\"}", actualEntity);
	}
	// END SNIPPET: secure

	@Test
	public void ensureGetDefaultResource() throws Exception {
		this.doTest("/", "<html><body>Hello from GCP</body></html>");
	}

	@Test
	public void ensureGetDirectoryDefaultResource() throws Exception {
		this.doTest("/sub", "<html><body>Hello from GCP sub directory</body></html>");
	}

	@Test
	public void ensureGetAsDirectoryDefaultResource() throws Exception {
		this.doTest("/sub/", "<html><body>Hello from GCP sub directory</body></html>");
	}

	@Test
	public void ensureGetDirectoryResource() throws Exception {
		this.doTest("/sub/index.html", "<html><body>Hello from GCP sub directory</body></html>");
	}

}