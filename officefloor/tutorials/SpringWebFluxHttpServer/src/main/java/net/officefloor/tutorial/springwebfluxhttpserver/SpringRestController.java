package net.officefloor.tutorial.springwebfluxhttpserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring {@link RestController}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@RestController
@RequestMapping("/rest")
public class SpringRestController {

	@Autowired
	private SpringDependency dependency;

	@GetMapping
	public Mono<ResponseModel> get() {
		return Mono.just(new ResponseModel("GET " + this.dependency.getMessage()));
	}

	@GetMapping("/path/{param}")
	public Mono<ResponseModel> path(@PathVariable String param) {
		return Mono.just(new ResponseModel(param));
	}

	@PostMapping("/update")
	public Flux<ResponseModel> post(@RequestBody RequestModel request) {
		return Flux.just(new ResponseModel(request.getInput()), new ResponseModel("ANOTHER"));
	}

}
// END SNIPPET: tutorial