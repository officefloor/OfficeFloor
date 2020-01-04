package net.officefloor.tutorial.navigatehttpserver;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests the {@link NavigateHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class NavigateHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: test
	@Rule
	public OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Rule
	public HttpClientRule client = new HttpClientRule();

	@Test
	public void testNavigate() throws Exception {

		// Request template one
		HttpResponse response = this.client.execute(new HttpGet(this.client.url("/one")));
		assertEquals("Should obtain first page", 200, response.getStatusLine().getStatusCode());
		response.getEntity().writeTo(System.out);

		// Click on link on template one
		response = this.client.execute(new HttpGet(this.client.url("/one+navigate")));
		assertEquals("Should navigate to second page", 200, response.getStatusLine().getStatusCode());
		response.getEntity().writeTo(System.out);

		// Submit on template two
		response = this.client.execute(new HttpGet(this.client.url("/two+process")));
		assertEquals("Should submit template two", 200, response.getStatusLine().getStatusCode());
		response.getEntity().writeTo(System.out);
	}
	// END SNIPPET: test

}