package net.officefloor.tutorial.springcontrollerhttpserver.migrated;

import net.officefloor.tutorial.springcontrollerhttpserver.RequestModel;
import net.officefloor.tutorial.springcontrollerhttpserver.ResponseModel;
import net.officefloor.tutorial.springcontrollerhttpserver.SpringDependency;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * Migrated Spring REST controller.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class MigratedRestController {

	public void get(SpringDependency dependency, ObjectResponse<ResponseModel> response) {
		response.send(new ResponseModel("GET " + dependency.getMessage()));
	}

	public void path(@HttpPathParameter("param") String param, ObjectResponse<ResponseModel> response) {
		response.send(new ResponseModel(param));
	}

	public void post(RequestModel request, ObjectResponse<ResponseModel> response) {
		response.send(new ResponseModel(request.getInput()));
	}

}
// END SNIPPET: tutorial