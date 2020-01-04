package net.officefloor.tutorial.httpservlet;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServlet;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.HttpClientRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.servlet.OfficeFloorFilter;

/**
 * Integration tests the {@link OfficeFloorFilter}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorFilterIT {

	// START SNIPPET: tutorial
	private static final String SERVER_URL = "http://localhost:8999";

	@Rule
	public HttpClientRule client = new HttpClientRule();

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void woofInput() throws Exception {
		
		// Create request
		HttpPost post = new HttpPost(SERVER_URL + "/increment");
		post.addHeader("Content-Type", "application/json");
		post.setEntity(new StringEntity(this.mapper.writeValueAsString(new Increment.Request("1"))));
		
		// Execute request
		HttpResponse response = this.client.execute(post);
		
		// Confirm expected response
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals("Incorrect status: " + entity, 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect content type", "application/json", response.getFirstHeader("Content-Type").getValue());
		Increment.Response entityResponse = this.mapper.readValue(entity, Increment.Response.class);
		assertEquals("Incorrect response", "2", entityResponse.getValue());
	}
	// END SNIPPET: tutorial

	/**
	 * Ensure WoOF resource available.
	 */
	@Test
	public void woofResource() throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(SERVER_URL + "/woof.txt"));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals("Incorrect status: " + entity, 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect content type", "text/plain", response.getFirstHeader("Content-Type").getValue());
		assertEquals("Incorrect content", "WOOF RESOURCE", entity);
	}

	/**
	 * Ensure can invoke {@link HttpServlet}.
	 */
	@Test
	public void servlet() throws Exception {
		HttpPost post = new HttpPost(SERVER_URL + "/servlet");
		post.setEntity(new StringEntity("2"));
		HttpResponse response = this.client.execute(post);
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals("Incorrect status: " + entity, 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect response", "1", entity);
	}

	/**
	 * Ensure if {@link HttpMethod} not supported that falls back to
	 * {@link HttpServlet}.
	 */
	@Test
	public void fallbackToServlet() throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(SERVER_URL + "/increment"));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals("Incorrect status: " + entity, 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect content", "Fallback to Servlet", entity);
	}

	/**
	 * Ensure {@link HttpServlet} resource available.
	 */
	@Test
	public void servletResource() throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(SERVER_URL + "/servlet.txt"));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals("Incorrect status: " + entity, 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect content type", "text/plain", response.getFirstHeader("Content-Type").getValue());
		assertEquals("Incorrect content", "SERVLET RESOURCE", entity);
	}

}