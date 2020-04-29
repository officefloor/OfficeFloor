package net.officefloor.tutorial.jaxrsapp;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the JAX-RS App.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsAppTest {

	private static Server server;

	private static WebTarget webTarget;

	@BeforeClass
	public static void startServer() throws Exception {

		// Start the server
		server = new Server(8081);
		ServletContextHandler handler = new ServletContextHandler();
		ServletHolder holder = handler.addServlet(ServletContainer.class, "/*");
		holder.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, JaxRsResource.class.getName());
		server.setHandler(handler);
		server.start();

		// Create the client
		webTarget = ClientBuilder.newClient().target("http://localhost:8081");
	}

	@AfterClass
	public static void stopServer() throws Exception {
		server.stop();
	}

	@Test
	public void get() throws Exception {
		String entity = webTarget.path("/jaxrs").request().get(String.class);
		assertEquals("Incorrect entity", "GET", entity);
	}

	@Test
	public void json() throws Exception {
		JsonResponse response = webTarget.path("/jaxrs/json").request()
				.post(Entity.entity(new JsonRequest("JSON"), MediaType.APPLICATION_JSON), JsonResponse.class);
		assertEquals("Incorrect response", "JSON", response.getMessage());
	}

}