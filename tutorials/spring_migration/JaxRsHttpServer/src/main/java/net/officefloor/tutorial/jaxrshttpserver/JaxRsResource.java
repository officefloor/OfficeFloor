package net.officefloor.tutorial.jaxrshttpserver;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

/**
 * JAX-RS resource.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
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
// END SNIPPET: tutorial