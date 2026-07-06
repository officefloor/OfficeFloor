package net.officefloor.tutorial.springrestcors;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class CompositionCorsService {

	public void service(ObjectResponse<String> response) {
		response.send("Hello from composition CORS endpoint");
	}
}
// END SNIPPET: tutorial
