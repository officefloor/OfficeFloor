package net.officefloor.tutorial.springapp;

import org.springframework.stereotype.Component;

@Component
public class InjectDependency {

	public String getMessage() {
		return "Dependency";
	}
}