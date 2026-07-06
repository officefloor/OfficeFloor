package net.officefloor.tutorial.springrestgettingstarted;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class GetGreetingLogic {

	public void service(GreetingService greetingService, ObjectResponse<GreetingResponse> response) {
		response.send(new GreetingResponse(greetingService.greet("World")));
	}
}
// END SNIPPET: tutorial
