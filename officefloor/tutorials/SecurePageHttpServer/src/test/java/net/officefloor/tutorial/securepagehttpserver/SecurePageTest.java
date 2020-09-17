package net.officefloor.tutorial.securepagehttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Tests the Secure Page.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@ExtendWith(OfficeFloorExtension.class)
public class SecurePageTest {

	@RegisterExtension
	public final HttpClientExtension client = new HttpClientExtension(true);

	@Test
	public void testSecurePage() throws Exception {

		// Ensure redirect to secure access to page
		this.assertHttpRequest("http://localhost:7878/card");

		// Ensure redirect to secure link access to page
		this.assertHttpRequest("http://localhost:7878/main+card");
	}

	private void assertHttpRequest(String url) throws IOException {
		HttpResponse response = this.client.execute(new HttpGet(url));
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful (after possible redirect)");
		String entity = EntityUtils.toString(response.getEntity());
		assertTrue(entity.contains("<h1>Enter card details</h1>"),
				"Should be rendering page as secure (and not exception)");
	}

}
// END SNIPPET: tutorial
