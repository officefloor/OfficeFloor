package net.officefloor.tutorial.jaxrsapp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Tests the JAX-RS App.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsAppTest extends JaxRsAppIT {

	private static Server server;

	@BeforeClass
	public static void startServer() throws Exception {

		// Start the server
		server = new Server(PORT);
		WebAppContext context = new WebAppContext();
		context.setResourceBase("src/main/webapp");
		server.setHandler(context);
		server.start();
	}

	@AfterClass
	public static void stopServer() throws Exception {
		server.stop();
	}

	// Tests inherited
}