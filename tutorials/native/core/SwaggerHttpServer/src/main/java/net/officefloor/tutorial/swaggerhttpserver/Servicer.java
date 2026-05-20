package net.officefloor.tutorial.swaggerhttpserver;

import io.swagger.v3.oas.annotations.tags.Tag;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.security.HttpAccess;
import net.officefloor.web.security.HttpAccessControl;

/**
 * Servicer.
 * 
 * @author Daniel Sagenschneider
 */
public class Servicer {

	public void get(@HttpQueryParameter("parameter") String parameter, ObjectResponse<Response> response) {
		response.send(new Response("GET " + parameter));
	}

	@Tag(name = "Update", description = "Example tag")
	public void post(Request request, ObjectResponse<Response> response) {
		response.send(new Response("POST " + request.getId()));
	}

	@HttpAccess
	public void secure(HttpAccessControl control, ObjectResponse<Response> response) {
		response.send(new Response("SECURE " + control.getPrincipal().getName()));
	}

}