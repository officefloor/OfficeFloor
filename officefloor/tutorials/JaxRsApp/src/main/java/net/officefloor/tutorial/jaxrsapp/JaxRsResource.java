package net.officefloor.tutorial.jaxrsapp;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

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

	@GET
	@Path("/path/{param}")
	public String pathParam(@PathParam("param") String param) {
		return param;
	}

	@GET
	@Path("/query")
	public String queryParam(@DefaultValue("default") @QueryParam("param") String param) {
		return param;
	}

	@GET
	@Path("/header")
	public String headerParam(@HeaderParam("param") String param) {
		return param;
	}

	@GET
	@Path("/cookie")
	public String cookieParam(@CookieParam("param") String param) {
		return param;
	}

	@POST
	@Path("/form")
	@Consumes("application/x-www-form-urlencoded")
	public String formParam(@FormParam("param") String param) {
		return param;
	}

	@GET
	@Path("/uriInfo/{param}")
	public String uriInfo(@Context UriInfo info) {
		return "PATH=" + info.getPath() + ", PATH PARAM=" + info.getPathParameters().getFirst("param")
				+ ", QUERY PARAM=" + info.getQueryParameters().getFirst("param");
	}

	@GET
	@Path("/headers")
	public String httpHeaders(@Context HttpHeaders headers) {
		return "HEADER=" + headers.getHeaderString("param") + ", COOKIE="
				+ headers.getCookies().get("param").getValue();
	}

	@POST
	@Path("/json")
	@Consumes("application/json")
	@Produces("application/json")
	public JsonResponse json(JsonRequest request) {
		return new JsonResponse(request.getInput());
	}

	private @Inject JaxRsDependency dependency;

	@GET
	@Path("/inject")
	public String inject() {
		return "Inject " + this.dependency.getMessage();
	}

	@Path("/sub")
	public JaxRsSubResource subResource() {
		return new JaxRsSubResource();
	}

	public static class JaxRsSubResource {

		@GET
		@Path("/resource")
		public String subResource() {
			return "sub-resource";
		}
	}

}