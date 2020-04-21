package net.officefloor.tutorial.springcontrollerhttpserver;

import org.springframework.stereotype.Component;

/**
 * Spring dependency.
 * 
 * @author Daniel Sagenschneider
 */
@Component
public class SpringDependency {

	public String getMessage() {
		return "Spring Dependency";
	}
}