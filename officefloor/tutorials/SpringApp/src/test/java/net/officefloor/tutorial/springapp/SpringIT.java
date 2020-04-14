package net.officefloor.tutorial.springapp;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

/**
 * Ensure can run as WAR application.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringIT {

	@Test
	public void simpleController() throws Exception {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpResponse response = client.execute(new HttpGet("http://localhost:8081/simple"));
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Should be successful: " + entity, 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect entity", "Simple Spring", entity);
		}
	}

	@Test
	public void injectController() throws Exception {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpResponse response = client.execute(new HttpGet("http://localhost:8081/inject"));
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Should be successful: " + entity, 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect entity", "Inject Dependency", entity);
		}
	}

}