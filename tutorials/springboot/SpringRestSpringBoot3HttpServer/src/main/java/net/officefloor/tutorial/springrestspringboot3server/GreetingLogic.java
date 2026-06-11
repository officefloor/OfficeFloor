package net.officefloor.tutorial.springrestspringboot3server;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class GreetingLogic {

	public void service(GreetingService greetingService, ObjectResponse<GreetingResponse> response) {
		response.send(new GreetingResponse(greetingService.greet("World")));
	}
}
// END SNIPPET: tutorial
