package net.officefloor.tutorial.warhttpserver;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

/**
 * Ensure {@link SimpleServlet} loaded via WAR.
 * 
 * @author Daniel Sagenschneider
 */
public class WarIT {

	@Test
	public void simpleServlet() throws Exception {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpResponse response = client.execute(new HttpGet("http://localhost:8081/simple"));
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Should be successful: " + entity, 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect entity", "SIMPLE", entity);
		}
	}

	@Test
	public void injectServlet() throws Exception {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpResponse response = client.execute(new HttpGet("http://localhost:8081/inject"));
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Should be successful: " + entity, 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect entity", "NO DEPENDENCY", entity);
		}
	}

}
