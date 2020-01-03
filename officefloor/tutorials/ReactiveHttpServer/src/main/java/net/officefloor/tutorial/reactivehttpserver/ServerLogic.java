package net.officefloor.tutorial.reactivehttpserver;

import net.officefloor.web.ObjectResponse;

/**
 * Server logic to be called by {@link ReactiveLogic}.
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