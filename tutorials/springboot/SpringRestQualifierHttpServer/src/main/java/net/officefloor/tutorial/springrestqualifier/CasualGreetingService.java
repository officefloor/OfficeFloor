package net.officefloor.tutorial.springrestqualifier;

import net.officefloor.web.ObjectResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestParam;

// START SNIPPET: tutorial
public class CasualGreetingService {

	public void service(
			@RequestParam(name = "name", required = false, defaultValue = "World") String name,
			@Qualifier("casual") Greeter greeter,
			ObjectResponse<String> response) {
		response.send(greeter.greet(name));
	}
}
// END SNIPPET: tutorial
