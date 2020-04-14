package net.officefloor.tutorial.springapp;

import org.springframework.stereotype.Component;

/**
 * Inject dependency.
 * 
 * @author Daniel Sagenschneider
 */
@Component
public class InjectDependency {

	public String getMessage() {
		return "Dependency";
	}
}