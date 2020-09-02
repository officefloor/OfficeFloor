package your.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpClientExtension;

/**
 * <p>
 * Integration tests the application.
 * <p>
 * TODO consider using Integration Test tools.
 */
public class RunApplicationIT {

	@RegisterExtension
	public final HttpClientExtension httpClient = new HttpClientExtension();

	@Test
	public void ensureApplicationAvailable() throws Exception {
		
		// Connect to application and obtain page
		HttpGet get = new HttpGet("http://localhost:7878/hi/Integration");
		get.addHeader("accept", "application/json");
		HttpResponse response = this.httpClient.execute(get);

		// Ensure correct response
		assertEquals(200, response.getStatusLine().getStatusCode(), "Incorrect status");
		assertEquals("application/json", response.getFirstHeader("content-type").getValue(), "Incorrect content type");
		assertEquals("{\"message\":\"Hello Integration\"}", EntityUtils.toString(response.getEntity()),
				"Incorrect response");
	}

}