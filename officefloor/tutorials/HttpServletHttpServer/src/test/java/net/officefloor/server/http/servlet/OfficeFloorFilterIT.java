package net.officefloor.server.http.servlet;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

/**
 * Integration tests the {@link OfficeFloorFilter}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorFilterIT {

	/**
	 * Ensure loads the {@link OfficeFloorFilter} via <code>web-fragment.xml</code>.
	 */
	@Test
	public void ensureWoOFServicing() throws Exception {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet post = new HttpGet("http://localhost:8999/test.txt");
			HttpResponse response = client.execute(post);
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Incorrect status: " + entity, 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect content type", "plain/text", response.getFirstHeader("Content-Type").getValue());
			assertEquals("Incorrect content", "TEST", entity);
		}
	}

}