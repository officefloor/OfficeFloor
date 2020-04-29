package net.officefloor.tutorial.jaxrsapp;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * JAX-RS resource.
 * 
 * @author Daniel Sagenschneider
 */
@Path("/jaxrs")
public class JaxRsResource {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String get() {
		return "GET";
	}

	@POST
	@Path("/json")
	public JsonResponse json(JsonRequest request) {
		return new JsonResponse(request.getInput());
	}

}