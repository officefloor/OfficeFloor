package net.officefloor.tutorial.jaxrshttpserver.migrated;

import java.io.IOException;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.tutorial.jaxrshttpserver.JaxRsDependency;
import net.officefloor.tutorial.jaxrshttpserver.RequestModel;
import net.officefloor.tutorial.jaxrshttpserver.ResponseModel;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * Migrated JAX-RS resource.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class MigratedResource {

	public void get(JaxRsDependency dependency, ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write("GET " + dependency.getMessage());
	}

	public void path(@HttpPathParameter("param") String param, ObjectResponse<ResponseModel> response) {
		response.send(new ResponseModel(param));
	}

	public void post(RequestModel request, ObjectResponse<ResponseModel> response) {
		response.send(new ResponseModel(request.getInput()));
	}
}
// END SNIPPET: tutorial