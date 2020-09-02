package net.officefloor.tutorial.navigatehttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * Tests the {@link NavigateHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: test
@ExtendWith(OfficeFloorExtension.class)
public class NavigateHttpServerTest {

	@RegisterExtension
	public final HttpClientExtension client = new HttpClientExtension();

	@Test
	public void testNavigate() throws Exception {

		// Request template one
		HttpResponse response = this.client.execute(new HttpGet(this.client.url("/one")));
		assertEquals(200, response.getStatusLine().getStatusCode(), "First page should be successful");
		assertTrue(EntityUtils.toString(response.getEntity()).contains("Page One"), "Should obtain first page");

		// Click on link on template one
		response = this.client.execute(new HttpGet(this.client.url("/one+navigate")));
		assertEquals(200, response.getStatusLine().getStatusCode(), "Second page should be successful");
		assertTrue(EntityUtils.toString(response.getEntity()).contains("Page Two"), "Should navigate to second page");

		// Submit on template two
		response = this.client.execute(new HttpGet(this.client.url("/two+process")));
		assertEquals(200, response.getStatusLine().getStatusCode(), "Submit should be successful");
		assertTrue(EntityUtils.toString(response.getEntity()).contains("Page One"), "Should submit template two");
	}
}
// END SNIPPET: test