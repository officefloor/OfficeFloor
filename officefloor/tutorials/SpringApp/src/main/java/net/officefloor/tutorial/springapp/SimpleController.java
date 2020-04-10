package net.officefloor.tutorial.springapp;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple {@link RestController}.
 * 
 * @author Daniel Sagenschneider
 */
@RestController
public class SimpleController {

	@RequestMapping("/simple")
	public String simple() {
		return "Simple Spring";
	}
}