package net.officefloor.tutorial.jaxrsapp;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the JAX-RS application.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsAppIT {

	protected static int PORT = 8081;

	protected WebTarget webTarget;

	@Before
	public void createClient() {
		this.webTarget = ClientBuilder.newClient().target("http://localhost:" + PORT);
	}

	@Test
	public void get() throws Exception {
		String entity = this.webTarget.path("/jaxrs").request().get(String.class);
		assertEquals("Incorrect entity", "GET", entity);
	}

	@Test
	public void pathParam() throws Exception {
		String parameter = this.webTarget.path("/jaxrs/path/parameter").request().get(String.class);
		assertEquals("Incorrect path parameter", "parameter", parameter);
	}

	@Test
	public void queryParam() throws Exception {
		String parameter = this.webTarget.path("/jaxrs/query").queryParam("param", "parameter").request()
				.get(String.class);
		assertEquals("Incorrect query parameter", "parameter", parameter);
	}

	@Test
	public void defaultQueryParam() throws Exception {
		String parameter = this.webTarget.path("/jaxrs/query").request().get(String.class);
		assertEquals("Incorrect default query parameter", "default", parameter);
	}

	@Test
	public void headerParam() throws Exception {
		String parameter = this.webTarget.path("/jaxrs/header").request().header("param", "header").get(String.class);
		assertEquals("Incorrect header", "header", parameter);
	}

	@Test
	public void cookieParam() throws Exception {
		String parameter = this.webTarget.path("/jaxrs/cookie").request().cookie("param", "cookie").get(String.class);
		assertEquals("Incorrect cookie", "cookie", parameter);
	}

	@Test
	public void formParam() throws Exception {
		String parameter = this.webTarget.path("/jaxrs/form").request().post(Entity.form(new Form("param", "form")),
				String.class);
		assertEquals("Incorrect form parameter", "form", parameter);
	}

	@Test
	public void uriInfo() throws Exception {
		String response = this.webTarget.path("/jaxrs/uriInfo/path").queryParam("param", "query").request()
				.get(String.class);
		assertEquals("Incorrect URI info", "PATH=jaxrs/uriInfo/path, PATH PARAM=path, QUERY PARAM=query", response);
	}

	@Test
	public void httpHeaders() throws Exception {
		String response = this.webTarget.path("/jaxrs/headers").request().header("param", "header")
				.cookie("param", "cookie").get(String.class);
		assertEquals("Incorrect headers", "HEADER=header, COOKIE=cookie", response);
	}

	@Test
	public void json() throws Exception {
		JsonResponse response = this.webTarget.path("/jaxrs/json").request()
				.post(Entity.entity(new JsonRequest("JSON"), MediaType.APPLICATION_JSON), JsonResponse.class);
		assertEquals("Incorrect response", "JSON", response.getMessage());
	}

	@Test
	public void inject() throws Exception {
		String response = this.webTarget.path("/jaxrs/inject").request().get(String.class);
		assertEquals("Incorrect injection", "Inject Dependency", response);
	}

	@Test
	public void context() throws Exception {
		String response = this.webTarget.path("/jaxrs/context").request().get(String.class);
		assertEquals("Incorrect injection", "Context Dependency", response);
	}

	@Test
	public void subResource() throws Exception {
		String response = this.webTarget.path("/jaxrs/sub/resource").request().get(String.class);
		assertEquals("Incorrect sub resource", "sub-resource", response);
	}

	@Test
	public void asyncSynchronous() throws Exception {
		String response = this.webTarget.path("/jaxrs/async/synchronous").request().get(String.class);
		assertEquals("Incorrect async", "Sync Dependency", response);
	}

	@Test
	public void asyncAsynchronous() throws Exception {
		String response = this.webTarget.path("/jaxrs/async/asynchronous").request().get(String.class);
		assertEquals("Incorrect async", "Async Dependency", response);
	}

}