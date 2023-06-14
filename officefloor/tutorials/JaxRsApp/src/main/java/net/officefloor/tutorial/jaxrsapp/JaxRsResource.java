package net.officefloor.tutorial.jaxrsapp;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

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
	@Path("/inject")
	public String inject() {
		return "Inject " + this.dependency.getMessage();
	}

	@GET
	@Path("/path/{param}")
	public String pathParam(@PathParam("param") String param) {
		return param;
	}

	@POST
	@Path("/json")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonResponse json(JsonRequest request) {
		return new JsonResponse(request.getInput());
	}

// END SNIPPET: tutorial

	@GET
	public String get() {
		return "GET";
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

	@GET
	@Path("/context")
	public String context(@Context JaxRsDependency dependency) {
		return "Context " + dependency.getMessage();
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

	@GET
	@Path("/async/synchronous")
	public void asyncSynchronous(@Suspended AsyncResponse async) {
		this.ensureSuspended(async);
		async.resume("Sync " + this.dependency.getMessage());
	}

	private @Inject ExecutorService executor;

	@GET
	@Path("/async/asynchronous")
	public void asyncAsynchronous(@Suspended AsyncResponse async) {
		this.ensureSuspended(async);
		this.executor.execute(() -> async.resume("Async " + this.dependency.getMessage()));
	}

	private void ensureSuspended(AsyncResponse async) {
		if (!async.isSuspended()) {
			throw new IllegalStateException(AsyncResponse.class.getSimpleName() + " should be injected suspended");
		}
	}

	@GET
	@Path("/exception/checked")
	public String checkedException() throws Exception {
		throw new IOException("TEST");
	}

	@GET
	@Path("/exception/unchecked")
	public String uncheckedException() {
		throw new RuntimeException("TEST");
	}

	@GET
	@Path("/exception/async")
	public void asyncException(@Suspended AsyncResponse async) {
		this.executor.execute(() -> async.resume(new Exception("TEST")));
	}

}