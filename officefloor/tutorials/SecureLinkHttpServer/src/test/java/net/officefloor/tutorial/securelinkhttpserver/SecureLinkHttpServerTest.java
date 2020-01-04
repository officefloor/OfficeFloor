package net.officefloor.tutorial.securelinkhttpserver;

import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests the Secure Link.
 * 
 * @author Daniel Sagenschneider
 */
public class SecureLinkHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: tutorial
	@Rule
	public OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Rule
	public HttpClientRule client = new HttpClientRule();

	@Test
	public void ensureLinkRenderedSecure() throws Exception {

		// Obtain the page
		HttpResponse response = this.client.execute(new HttpGet("http://localhost:7878"));
		String renderedPage = EntityUtils.toString(response.getEntity());

		// Ensure login form (link) is secure
		assertTrue("Login form should be secure", renderedPage.contains("form action=\"https://localhost:7979/+login"));
	}
	// END SNIPPET: tutorial

}