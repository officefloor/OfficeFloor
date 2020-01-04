package ${package};

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.HttpClientRule;

/**
 * <p>
 * Integration tests the application.
 * <p>
 * TODO consider using Integration Test tools.
 */
public class RunApplicationIT {

	@Rule
	public HttpClientRule httpClient = new HttpClientRule();

	@Test
	public void ensureApplicationAvailable() throws Exception {

		// Connect to application and obtain page
		HttpGet get = new HttpGet("http://localhost:7878/hi/Integration");
		get.addHeader("accept", "application/json");
		HttpResponse response = this.httpClient.execute(get);

		// Ensure correct response
		assertEquals("Incorrect status", 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect content type", "application/json", response.getFirstHeader("content-type").getValue());
		assertEquals("Incorrect response", "{\"message\":\"Hello Integration\"}",
				EntityUtils.toString(response.getEntity()));
	}

}