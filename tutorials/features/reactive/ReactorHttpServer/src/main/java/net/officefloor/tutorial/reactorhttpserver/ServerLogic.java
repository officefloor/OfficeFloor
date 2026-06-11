package net.officefloor.tutorial.reactorhttpserver;

import net.officefloor.web.ObjectResponse;

/**
 * Server logic to be called by {@link ReactorLogic}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ServerLogic {

	public void service(ObjectResponse<ServerResponse> response) {
		response.send(new ServerResponse("TEST"));
	}
}
// END SNIPPET: tutorial