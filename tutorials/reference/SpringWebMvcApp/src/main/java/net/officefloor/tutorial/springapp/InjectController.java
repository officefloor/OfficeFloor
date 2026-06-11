package net.officefloor.tutorial.springapp;

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

/**
 * Inject {@link RestController}.
 * 
 * @author Daniel Sagenschneider
 */
@RestController
@RequestMapping("/complex")
public class InjectController {

	@Autowired
	private InjectDependency dependency;

	@GetMapping("/inject")
	public String inject() {
		return "Inject " + this.dependency.getMessage();
	}

	@GetMapping("/status")
	@ResponseStatus(HttpStatus.CREATED)
	public String status() {
		return "Status";
	}

	@GetMapping("/path/{param}")
	public String pathParam(@PathVariable("param") String parameter) {
		return "Parameter " + parameter;
	}

	@GetMapping(path = "/query", params = "param")
	public String requestParam(@RequestParam("param") String parameter) {
		return "Parameter " + parameter;
	}

	@GetMapping(path = "/header", headers = "header")
	public String header(@RequestHeader("header") String header) {
		return "Header " + header;
	}

	@PostMapping
	public String post(@RequestBody String requestBody) {
		return "Body " + requestBody;
	}

}