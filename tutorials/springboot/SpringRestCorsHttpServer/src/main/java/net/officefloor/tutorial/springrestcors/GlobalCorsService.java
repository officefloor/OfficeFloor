package net.officefloor.tutorial.springrestcors;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class GlobalCorsService {

	public void service(ObjectResponse<String> response) {
		response.send("Hello from global CORS endpoint");
	}
}
// END SNIPPET: tutorial
