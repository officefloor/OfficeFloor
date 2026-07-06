package net.officefloor.tutorial.springrestcors;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class AllMethodsCorsService {

	public void service(ObjectResponse<String> response) {
		response.send("Hello from all-methods CORS endpoint");
	}
}
// END SNIPPET: tutorial
