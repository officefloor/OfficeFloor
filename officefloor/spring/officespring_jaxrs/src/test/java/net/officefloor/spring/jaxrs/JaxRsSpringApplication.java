package net.officefloor.spring.jaxrs;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

/**
 * JAX-RS Spring {@link Application}.
 * 
 * @author Daniel Sagenschneider
 */
@Component
public class JaxRsSpringApplication extends ResourceConfig {

	public JaxRsSpringApplication() {
		this.register(SpringResource.class);
		this.register(JaxRsResource.class);
	}
}