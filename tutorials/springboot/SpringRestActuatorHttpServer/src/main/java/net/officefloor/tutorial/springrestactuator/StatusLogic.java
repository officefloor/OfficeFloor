package net.officefloor.tutorial.springrestactuator;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class StatusLogic {

	public void service(ObjectResponse<StatusResponse> response) {
		response.send(new StatusResponse("running"));
	}
}
// END SNIPPET: tutorial
