package net.officefloor.tutorial.interactivehttpserver;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link PageFlowHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class InteractiveHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: test
	/**
	 * See {@link MockWoofServerRule} for faster tests that avoid sending requests
	 * over sockets. However, for this tutorial we are demonstrating running the
	 * full application for testing.
	 */
	@Rule
	public OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Rule
	public HttpClientRule client = new HttpClientRule();

	@Test
	public void pageInteraction() throws Exception {

		// Request the initial page
		HttpResponse response = this.client.execute(new HttpGet(this.client.url("/example")));
		assertEquals("Request should be successful", 200, response.getStatusLine().getStatusCode());
		response.getEntity().writeTo(System.out);

		// Send form submission
		HttpPost post = new HttpPost(this.client.url("/example+handleSubmission"));
		post.setEntity(new StringEntity("name=Daniel&description=founder"));
		response = this.client.execute(post);
		assertEquals("Should submit successfully", 200, response.getStatusLine().getStatusCode());
		response.getEntity().writeTo(System.out);
	}
	// END SNIPPET: test

}