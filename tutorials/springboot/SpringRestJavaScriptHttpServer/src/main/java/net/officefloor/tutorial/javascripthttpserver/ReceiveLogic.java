package net.officefloor.tutorial.javascripthttpserver;

import org.springframework.web.bind.annotation.RequestBody;

// START SNIPPET: tutorial
public class ReceiveLogic {

	public Request receive(@RequestBody Request request) {
		return request;
	}
}
// END SNIPPET: tutorial
