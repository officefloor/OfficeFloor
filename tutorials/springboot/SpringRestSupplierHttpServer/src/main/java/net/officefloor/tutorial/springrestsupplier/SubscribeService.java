package net.officefloor.tutorial.springrestsupplier;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class SubscribeService {

	public void service(MessageSubscriber subscriber, ObjectResponse<String> response) {
		String content = subscriber.receive();
		response.send(content != null ? content : "");
	}
}
// END SNIPPET: tutorial
