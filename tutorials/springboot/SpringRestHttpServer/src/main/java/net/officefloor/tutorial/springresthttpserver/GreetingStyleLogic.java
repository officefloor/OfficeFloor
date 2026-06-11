package net.officefloor.tutorial.springresthttpserver;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
public class GreetingStyleLogic {

	public void formal(@PathVariable(name = "name") String name,
			ObjectResponse<GreetingResponse> response) {
		response.send(new GreetingResponse("Good day, " + name + "."));
	}

	public void casual(@PathVariable(name = "name") String name,
			ObjectResponse<GreetingResponse> response) {
		response.send(new GreetingResponse("Hey, " + name + "!"));
	}
}
// END SNIPPET: tutorial
