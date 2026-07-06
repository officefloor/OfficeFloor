package net.officefloor.tutorial.springresthttpserver;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
public class GetStyledGreetingLogic {

	public void service(
			@PathVariable(name = "style") String style,
			@PathVariable(name = "name") String name,
			GreetingService greetingService,
			ObjectResponse<GreetingResponse> response) {
		response.send(new GreetingResponse(style + ": " + greetingService.greet(name)));
	}
}
// END SNIPPET: tutorial
