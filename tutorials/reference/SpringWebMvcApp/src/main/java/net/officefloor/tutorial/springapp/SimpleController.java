package net.officefloor.tutorial.springapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple {@link RestController}.
 * 
 * @author Daniel Sagenschneider
 */
@RestController
public class SimpleController {

	@GetMapping("/simple")
	public String simple() {
		return "Simple Spring";
	}
}