package net.officefloor.tutorial.jaxrshttpserver;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * JAX-RS resource.
 * 
 * @author Daniel Sagenschneider
 */
@Path("/jaxrs")
public class JaxRsResource {

	private @Inject JaxRsDependency dependency;

	@GET
	public String get() {
		return "GET " + this.dependency.getMessage();
	}

	@GET
	@Path("/path/{param}")
	public ResponseModel path(@PathParam("param") String param) {
		return new ResponseModel(param);
	}

	@POST
	@Path("/update")
	@Consumes("application/json")
	@Produces("application/json")
	public ResponseModel post(RequestModel request) {
		return new ResponseModel(request.getInput());
	}
}