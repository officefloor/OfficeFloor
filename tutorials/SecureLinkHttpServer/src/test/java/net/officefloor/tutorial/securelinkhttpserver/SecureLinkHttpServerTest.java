package net.officefloor.tutorial.securelinkhttpserver;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Tests the Secure Link.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@ExtendWith(OfficeFloorExtension.class)
public class SecureLinkHttpServerTest {

	@RegisterExtension
	public final HttpClientExtension client = new HttpClientExtension();

	@Test
	public void ensureLinkRenderedSecure() throws Exception {

		// Obtain the page
		HttpResponse response = this.client.execute(new HttpGet("http://localhost:7878"));
		String renderedPage = EntityUtils.toString(response.getEntity());

		// Ensure login form (link) is secure
		assertTrue(renderedPage.contains("form action=\"https://localhost:7979/+login"), "Login form should be secure");
	}

}
// END SNIPPET: tutorial
