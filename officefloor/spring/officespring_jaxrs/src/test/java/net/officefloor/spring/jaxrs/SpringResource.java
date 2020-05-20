package net.officefloor.spring.jaxrs;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Spring Resource.
 * 
 * @author Daniel Sagenschneider
 */
@Component
@Path("/")
public class SpringResource {

	@GET
	@Path("/spring")
	public String get() {
		return "SPRING";
	}

	private @Autowired SpringDependency springAutowired;

	@GET
	@Path("/spring/autowired/spring")
	public String getSpringAutowired() {
		return "Autowired " + this.springAutowired.getMessage();
	}

	private @Autowired OfficeFloorDependency officeFloorAutowired;

	@GET
	@Path("/spring/autowired/officefloor")
	public String getOfficeFloorAutowired() {
		return "Autowired " + this.officeFloorAutowired.getMessage();
	}

	private @Inject SpringDependency springInject;

	@GET
	@Path("/spring/inject/spring")
	public String getSpringInject() {
		return "Inject " + this.springInject.getMessage();
	}

	private @Inject OfficeFloorDependency officeFloorInject;

	@GET
	@Path("/spring/inject/officefloor")
	public String getOfficeFloorInject() {
		return "Inject " + this.officeFloorInject.getMessage();
	}

}