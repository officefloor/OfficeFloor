package net.officefloor.tutorial.springfluxapp;

import org.springframework.stereotype.Component;

@Component
public class InjectDependency {

	public String getMessage() {
		return "Dependency";
	}
}