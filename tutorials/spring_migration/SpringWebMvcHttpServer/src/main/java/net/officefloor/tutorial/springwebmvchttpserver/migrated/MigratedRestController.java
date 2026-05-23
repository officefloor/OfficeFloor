package net.officefloor.tutorial.springwebmvchttpserver.migrated;

import net.officefloor.tutorial.springwebmvchttpserver.RequestModel;
import net.officefloor.tutorial.springwebmvchttpserver.ResponseModel;
import net.officefloor.tutorial.springwebmvchttpserver.SpringDependency;
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