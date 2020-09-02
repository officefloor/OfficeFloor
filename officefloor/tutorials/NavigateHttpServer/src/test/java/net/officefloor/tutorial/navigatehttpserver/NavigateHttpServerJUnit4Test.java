package net.officefloor.tutorial.navigatehttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests the {@link NavigateHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: test
public class NavigateHttpServerJUnit4Test {

	@Rule
	public final OfficeFloorRule officeFloor = new OfficeFloorRule(this);

	@Rule
	public final HttpClientRule client = new HttpClientRule();

	@Test
	public void testNavigate() throws Exception {

		// Request template one
		HttpResponse response = this.client.execute(new HttpGet(this.client.url("/one")));
		assertEquals("First page should be successful", 200, response.getStatusLine().getStatusCode());
		assertTrue("Should obtain first page", EntityUtils.toString(response.getEntity()).contains("Page One"));

		// Click on link on template one
		response = this.client.execute(new HttpGet(this.client.url("/one+navigate")));
		assertEquals("Second page should be successful", 200, response.getStatusLine().getStatusCode());
		assertTrue("Should navigate to second page", EntityUtils.toString(response.getEntity()).contains("Page Two"));

		// Submit on template two
		response = this.client.execute(new HttpGet(this.client.url("/two+process")));
		assertEquals("Submit should be successful", 200, response.getStatusLine().getStatusCode());
		assertTrue("Should submit template two", EntityUtils.toString(response.getEntity()).contains("Page One"));
	}
}
// END SNIPPET: test
