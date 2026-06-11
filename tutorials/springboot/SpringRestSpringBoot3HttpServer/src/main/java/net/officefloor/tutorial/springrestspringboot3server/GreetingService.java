package net.officefloor.tutorial.springrestspringboot3server;

import org.springframework.stereotype.Service;

// START SNIPPET: tutorial
@Service
public class GreetingService {

	public String greet(String name) {
		return "Hello from Spring Boot 3, " + name + "!";
	}
}
// END SNIPPET: tutorial
