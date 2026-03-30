package net.officefloor.tutorial.springresthttpserver;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
public class GetNamedGreetingLogic {

	public void service(
			@PathVariable("name") String name,
			GreetingService greetingService,
			ObjectResponse<GreetingResponse> response) {
		response.send(new GreetingResponse(greetingService.greet(name)));
	}
}
// END SNIPPET: tutorial
