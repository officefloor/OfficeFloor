package net.officefloor.tutorial.interactivehttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.OfficeFloorExtension;

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
	@RegisterExtension
	public final OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	@RegisterExtension
	public final HttpClientExtension client = new HttpClientExtension();

	@Test
	public void pageInteraction() throws Exception {

		// Request the initial page
		HttpResponse response = this.client.execute(new HttpGet(this.client.url("/example")));
		assertEquals(200, response.getStatusLine().getStatusCode(), "Request should be successful");
		String responseEntity = EntityUtils.toString(response.getEntity());
		assertFalse(responseEntity.contains("Daniel"), "Should not have entry");

		// Send form submission
		HttpPost post = new HttpPost(this.client.url("/example+handleSubmission"));
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		post.setEntity(new StringEntity("name=Daniel&description=founder"));
		response = this.client.execute(post);
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should submit successfully");
		responseEntity = EntityUtils.toString(response.getEntity());
		assertTrue(responseEntity.contains("<p>Thank you Daniel</p>"), "Should indicate added");
	}
	// END SNIPPET: test

}