package net.officefloor.tutorial.springwebmvchttpserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public ResponseModel get() {
		return new ResponseModel("GET " + this.dependency.getMessage());
	}

	@GetMapping("/path/{param}")
	public ResponseModel path(@PathVariable String param) {
		return new ResponseModel(param);
	}

	@PostMapping("/update")
	public ResponseModel post(@RequestBody RequestModel request) {
		return new ResponseModel(request.getInput());
	}

}
// END SNIPPET: tutorial