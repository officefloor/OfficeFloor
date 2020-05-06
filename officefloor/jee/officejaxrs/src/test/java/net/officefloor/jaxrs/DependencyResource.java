package net.officefloor.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * JAX-RS resource using {@link Dependency}.
 * 
 * @author Daniel Sagenschneider
 */
@Path("/dependency")
public class DependencyResource {

	private @Dependency ResourceDependency dependency;

	@GET
	public String get() {
		return "Dependency " + this.dependency.getMessage();
	}
}