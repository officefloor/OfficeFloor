package net.officefloor.tutorial.jaxrsapp;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * JAX-RS application.
 * 
 * @author Daniel Sagenschneider
 */
@ApplicationPath("/")
public class JaxRsApplication extends ResourceConfig {

	public JaxRsApplication() {
		this.register(JaxRsResource.class);
		this.register(JaxRsRemainingResource.class);
		this.register(new JaxRsBinder());
	}

}