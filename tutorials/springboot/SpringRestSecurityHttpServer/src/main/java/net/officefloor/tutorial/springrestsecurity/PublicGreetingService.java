package net.officefloor.tutorial.springrestsecurity;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class PublicGreetingService {

	public void service(ObjectResponse<String> response) {
		response.send("Hello, World!");
	}
}
// END SNIPPET: tutorial
