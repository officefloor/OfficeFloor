package net.officefloor.tutorial.jaxrsapp;

import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;

/**
 * JAX-RS application.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@ApplicationPath("/")
public class JaxRsApplication extends ResourceConfig {

	public JaxRsApplication() {
		this.register(JaxRsResource.class);
		this.register(new JaxRsBinder());
	}
}
// END SNIPPET: tutorial