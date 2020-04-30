package net.officefloor.tutorial.jaxrsapp;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the JAX-RS application.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsAppIT {

	protected static int PORT = 8081;

	protected WebTarget webTarget;

	@Before
	public void createClient() {
		this.webTarget = ClientBuilder.newClient().target("http://localhost:" + PORT);
	}

	@Test
	public void get() throws Exception {
		String entity = this.webTarget.path("/jaxrs").request().get(String.class);
		assertEquals("Incorrect entity", "GET", entity);
	}

	@Test
	public void json() throws Exception {
		JsonResponse response = this.webTarget.path("/jaxrs/json").request()
				.post(Entity.entity(new JsonRequest("JSON"), MediaType.APPLICATION_JSON), JsonResponse.class);
		assertEquals("Incorrect response", "JSON", response.getMessage());
	}

}