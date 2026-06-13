package net.officefloor.tutorial.reactorhttpserver;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class ServerLogic {

	public void service(ObjectResponse<ServerResponse> response) {
		response.send(new ServerResponse("TEST"));
	}
}
// END SNIPPET: tutorial