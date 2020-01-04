package net.officefloor.tutorial.javascripthttpserver;

import net.officefloor.web.ObjectResponse;

/**
 * Sends the response.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ResponseLogic {

	public void send(ObjectResponse<Response> response) {
		response.send(new Response("successful"));
	}
}
// END SNIPPET: tutorial