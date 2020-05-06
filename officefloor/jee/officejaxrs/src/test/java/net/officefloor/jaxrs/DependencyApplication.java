package net.officefloor.jaxrs;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;

import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * {@link Application} with {@link Dependency}.
 * 
 * @author Daniel Sagenschneider
 */
@ApplicationPath("/")
public class DependencyApplication extends ResourceConfig {

	/**
	 * Instantiate.
	 */
	public DependencyApplication() {
		this.register(DependencyResource.class);
	}
}