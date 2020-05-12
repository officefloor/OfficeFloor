package net.officefloor.spring.jaxrs;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;

import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * JAX-RS resource with dependency injection.
 * 
 * @author Daniel Sagenschneider
 */
@Path("/")
public class JaxRsResource {

	@GET
	@Path("/jaxrs")
	public String get() {
		return "JAX-RS";
	}

	private @Autowired SpringDependency springAutowired;

	@GET
	@Path("/jaxrs/autowired/spring")
	public String getSpringAutowired() {
		return "Autowired " + this.springAutowired.getMessage();
	}

	private @Autowired OfficeFloorDependency officeFloorAutowired;

	@GET
	@Path("/jaxrs/autowired/officefloor")
	public String getOfficeFloorAutowired() {
		return "Autowired " + this.officeFloorAutowired.getMessage();
	}
	
	private @Inject SpringDependency springInject;

	@GET
	@Path("/jaxrs/inject/spring")
	public String getSpringInject() {
		return "Inject " + this.springInject.getMessage();
	}

	private @Inject OfficeFloorDependency officeFloorInject;

	@GET
	@Path("/jaxrs/inject/officefloor")
	public String getOfficeFloorInject() {
		return "Inject " + this.officeFloorInject.getMessage();
	}

	private @Dependency OfficeFloorDependency officeFloorDependency;

	@GET
	@Path("/jaxrs/dependency")
	public String getOfficeFloorDependency() {
		return "Dependency " + this.officeFloorInject.getMessage();
	}

}