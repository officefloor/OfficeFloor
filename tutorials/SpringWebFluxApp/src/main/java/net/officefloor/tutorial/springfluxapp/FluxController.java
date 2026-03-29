package net.officefloor.tutorial.springfluxapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Flux {@link RestController}.
 * 
 * @author Daniel Sagenschneider
 */
@RestController
@RequestMapping("/complex")
public class FluxController {

	@Autowired
	private InjectDependency dependency;

	@GetMapping("/inject")
	public Mono<String> inject() {
		return Mono.just("Inject " + this.dependency.getMessage());
	}

	@GetMapping("/status")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<String> status() {
		return Mono.just("Status");
	}

	@GetMapping("/path/{param}")
	public Mono<String> pathParam(@PathVariable("param") String parameter) {
		return Mono.just("Parameter " + parameter);
	}

	@GetMapping(path = "/query", params = "param")
	public Mono<String> requestParam(@RequestParam("param") String parameter) {
		return Mono.just("Parameter " + parameter);
	}

	@GetMapping(path = "/header", headers = "header")
	public Mono<String> header(@RequestHeader("header") String header) {
		return Mono.just("Header " + header);
	}

	@PostMapping
	public Mono<String> post(@RequestBody String requestBody) {
		return Mono.just("Body " + requestBody);
	}

}