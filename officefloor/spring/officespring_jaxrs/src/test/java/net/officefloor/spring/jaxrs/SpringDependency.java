package net.officefloor.spring.jaxrs;

import org.springframework.stereotype.Component;

/**
 * Spring dependendency.
 * 
 * @author Daniel Sagenschneider
 */
@Component
public class SpringDependency {

	public String getMessage() {
		return "Spring";
	}
}