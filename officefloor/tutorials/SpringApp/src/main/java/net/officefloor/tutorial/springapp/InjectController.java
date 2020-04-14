package net.officefloor.tutorial.springapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inject {@link RestController}.
 * 
 * @author Daniel Sagenschneider
 */
@RestController
public class InjectController {

	@Autowired
	private InjectDependency dependency;

	@GetMapping("/inject")
	public String inject() {
		return "Inject " + this.dependency.getMessage();
	}
}