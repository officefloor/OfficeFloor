package net.officefloor.tutorial.springresthttpserver;

import org.springframework.stereotype.Service;

// START SNIPPET: tutorial
@Service
public class GreetingService {

	public String greet(String name) {
		return "Hello, " + name + "!";
	}
}
// END SNIPPET: tutorial
