package net.officefloor.tutorial.springwebfluxhttpserver.migrated;

import net.officefloor.tutorial.springwebfluxhttpserver.RequestModel;
import net.officefloor.tutorial.springwebfluxhttpserver.ResponseModel;
import net.officefloor.tutorial.springwebfluxhttpserver.SpringDependency;
import net.officefloor.web.HttpPathParameter;
import reactor.core.publisher.Mono;

/**
 * Migrated Spring REST controller.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class MigratedRestController {

	public Mono<ResponseModel> get(SpringDependency dependency) {
		return Mono.just(new ResponseModel("GET " + dependency.getMessage()));
	}

	public Mono<ResponseModel> path(@HttpPathParameter("param") String param) {
		return Mono.just(new ResponseModel(param));
	}

	public Mono<ResponseModel> post(RequestModel request) {
		return Mono.just(new ResponseModel(request.getInput()));
	}

}
// END SNIPPET: tutorial