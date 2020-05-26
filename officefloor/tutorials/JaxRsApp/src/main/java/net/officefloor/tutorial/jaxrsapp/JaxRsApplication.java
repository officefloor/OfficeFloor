package net.officefloor.tutorial.jaxrsapp;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

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