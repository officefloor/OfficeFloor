package net.officefloor.tutorial.springresthttpserver;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
public class GetGreetingEntityLogic {

	public void service(
			@PathVariable(name = "name") String name,
			GreetingService greetingService,
			ObjectResponse<ResponseEntity<GreetingResponse>> response) {
		response.send(ResponseEntity.ok()
				.header("X-Greeting-Name", name)
				.body(new GreetingResponse(greetingService.greet(name))));
	}
}
// END SNIPPET: tutorial
