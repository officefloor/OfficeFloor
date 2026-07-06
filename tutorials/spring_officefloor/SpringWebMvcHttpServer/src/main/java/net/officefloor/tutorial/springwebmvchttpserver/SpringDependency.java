package net.officefloor.tutorial.springwebmvchttpserver;

import org.springframework.stereotype.Component;

@Component
public class SpringDependency {

	public String getMessage() {
		return "Spring Dependency";
	}
}