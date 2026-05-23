package net.officefloor.tutorial.httpservlet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServlet;
import net.officefloor.server.http.HttpClientExtension;
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

	@RegisterExtension
	public HttpClientExtension client = new HttpClientExtension();

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
		assertEquals(200, response.getStatusLine().getStatusCode(), "Incorrect status: " + entity);
		assertEquals("application/json", response.getFirstHeader("Content-Type").getValue(), "Incorrect content type");
		Increment.Response entityResponse = this.mapper.readValue(entity, Increment.Response.class);
		assertEquals("2", entityResponse.getValue(), "Incorrect response");
	}
	// END SNIPPET: tutorial

	/**
	 * Ensure WoOF resource available.
	 */
	@Test
	public void woofResource() throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(SERVER_URL + "/woof.txt"));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Incorrect status: " + entity);
		assertEquals("text/plain", response.getFirstHeader("Content-Type").getValue(), "Incorrect content type");
		assertEquals("WOOF RESOURCE", entity, "Incorrect content");
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
		assertEquals(200, response.getStatusLine().getStatusCode(), "Incorrect status: " + entity);
		assertEquals("1", entity, "Incorrect response");
	}

	/**
	 * Ensure if {@link HttpMethod} not supported that falls back to
	 * {@link HttpServlet}.
	 */
	@Test
	public void fallbackToServlet() throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(SERVER_URL + "/increment"));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Incorrect status: " + entity);
		assertEquals("Fallback to Servlet", entity, "Incorrect content");
	}

	/**
	 * Ensure {@link HttpServlet} resource available.
	 */
	@Test
	public void servletResource() throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(SERVER_URL + "/servlet.txt"));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Incorrect status: " + entity);
		assertEquals("text/plain", response.getFirstHeader("Content-Type").getValue(), "Incorrect content type");
		assertEquals("SERVLET RESOURCE", entity, "Incorrect content");
	}

}